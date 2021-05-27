from flask import Flask, render_template, request, make_response, flash, redirect
from api import api
from generators import *
import random
import re
from base64 import b64decode, b64encode
from vk_connect import get_connect, get_friends
import pickle
app = Flask(__name__)

random.seed()

def check_cookie(cookies:dict)->MessagesAPI:
    if cookies:
        if 'methods1' in cookies.keys() and 'methods2' in cookies.keys() and 'methods3' in cookies.keys() and 'methods4' in cookies.keys() and 'methods5' in cookies.keys():
            try:
                temp = ""
                for i in range(1, 6):
                    temp += cookies['methods' + str(i)]
                methods = pickle.loads(b64decode(temp))
            except Exception:
                methods = None
            if methods != None:
                check = get_friends(methods, 1)
                if check != None:
                    return methods
    else:
        return None

def twofactor(methods:MessagesAPI,login,password):
    '''
    :param login:
    :param password:
    :return: generated html
    '''
    methods.main_session.post(f'https://login.vk.com/?act=login',
                           data = f"act=login&role= al_frame&expire=&recaptcha=&captcha_sid=&captcha_key=&_origin=https%3A%2F%2Fvk.com" + \
                                f"&ip_h={methods.ip_h}&lg_h={methods.lg_h}&ul=&email={login}&pass={password}",
                           cookies = methods.main_session.cookies.get_dict(), timeout=5)

    methods.q_hash = None
    for i in methods.main_session.cookies.get_dict():
        if 'remixq_' in i:
            methods.q_hash = i.split('remixq_')[1]
            break

    methods.main_session.get(f'https://vk.com/login.php?act=slogin&to=&s=1&__q_hash={methods.q_hash}',
                          cookies = methods.main_session.cookies.get_dict(), timeout=5)
    response = methods.main_session.get(f'https://vk.com/login?act=authcheck',
                                     cookies = methods.main_session.cookies.get_dict(), timeout=5)

    hash_url = re.findall(r"window.Authcheck.init\('(\S*)',", str(response.text))
    if hash_url == []:
        raise methods.Exception_MessagesAPI('No cookies found. Auth first.', 'AuthError')
    methods.hash_url = hash_url[0]
    return methods

@app.route('/code',methods=['POST'])
def code():
    '''
    :return: generated html if login successfull or error page in other situations
    '''
    if request.cookies:
       methods = check_cookie(request.cookies)
       code = request.form['code']
       if methods == None:
           return render_template("Ooops.html")
       response = methods.main_session.post(f'https://vk.com/al_login.php',
                                            data=f'act=a_authcheck_code&al=1&code={code}&hash={methods.hash_url}&remember=1',
                                            cookies=methods.main_session.cookies.get_dict(), timeout=5)
       print(response.text)
       response_json = json.loads(response.text[4:])['payload']
       if 'Неверный код' in response_json[1][0]:
           return render_template("Ooops.html")

       response = methods.main_session.get(f'https://vk.com/login.php?act=slogin&to=&s=1&__q_hash={methods.q_hash}&fast=1',
                                           cookies=methods.main_session.cookies.get_dict(), timeout=5)

       methods.main_session.get(f'https://vk.com/feed',
                                cookies=methods.main_session.cookies.get_dict(), timeout=5)

       methods.cookies_final = methods.main_session.cookies.get_dict()
       friends = get_friends(methods)
       try:
           friends = friends['items']
       except Exception:
           friends = None
       resp = make_response(generate_html_messages(methods, friends))
       temp = b64encode(pickle.dumps(methods))
       for i in range(1, 6):
           resp.set_cookie('methods'+str(i), temp[(i-1)*len(temp)//5:i*len(temp)//5])
       return resp
    return render_template("Ooops.html")
@app.route('/comingsoon')
def show_comingsoon():
    '''
    :return: comingsoon page
    '''
    return render_template('comingsoon.html')

@app.route('/search_friends',methods=['GET'])
def search():
    '''
    :return: message page with  friends have found
    '''
    try:
        search = request.args.get('search')
    except:
        search = None
    if request.cookies:
        methods = check_cookie(request.cookies)
        if methods == None:
            return render_template('Ooops.html')
        friends = get_friends(methods)
        if friends == None:
            return generate_html_messages(methods)
        try:
            friends = friends['items']
        except Exception:
            friends = None
        if search == None:
            return generate_html_messages(methods, friends)
        selected_friends = []
        for i in range(len(friends)):
            temp = friends[i]['first_name']+friends[i]['last_name']
            temp = temp.lower()
            search = search.lower()
            for j in range(len(temp)):
                if search == temp[j:min(j+len(search), len(temp))]:
                    selected_friends.append(friends[i])
                    break
        return generate_html_messages(methods, selected_friends)
    else:
        return render_template("Ooops.html")



@app.route("/messages")
def messages():
    '''
    :return: page with friends and messages
    '''

@app.route('/Download')
def show_Downloads():
    '''
    :return: Download page
    '''
    return render_template('Download.html')

@app.route('/logout')
def logout():
    '''
    :return: home page without cookies
    '''

@app.route('/')
@app.route('/index')
@app.route('/index.html')
def show_index():
    '''
    :return: return index page
    '''
    return render_template("index.html")

@app.route('/login', methods=['POST','GET'])
def login_check():
    '''
    :return: code verification page if (twofactor) and login and password correct or api page in other situations
    '''
    if request.method == 'GET':
        if request.cookies:
            methods = check_cookie(request.cookies)
            if methods!=None:
                friends = get_friends(methods)
                try:
                    friends = friends['items']
                except:
                    friends = None
                resp = make_response(generate_html_messages(methods, friends))
                resp.set_cookie("time", str(random.randint(1, 100000)))
                return resp
        resp = make_response(render_template('login.html'))
        resp.set_cookie("time", str(random.randint(1, 100000)))
        return resp

    if request.method == 'POST':
        try:
            login = request.form['login']
        except Exception as e:
            login = None
            print(e)

        try:
            password = request.form['password']
        except Exception as e:
            password = None
            print(e)

        try:
            check = request.form['check']
        except Exception as e:
            check = None
            print(e)

        if check == None:
            methods = get_connect(login=login, password=password, twofactor=False, cookies=None, auth=True)
            if methods == None:
                return render_template("failed_login.html")
            friends = get_friends(methods)
            try:
                friends = friends['items']
            except Exception:
                friends=None
            resp = make_response(generate_html_messages(methods,friends))
            temp = b64encode(pickle.dumps(methods)).decode()
            for i in range(1,6):
                resp.set_cookie("methods"+str(i), temp[(i-1)*len(temp)//5:i*len(temp)//5])
            resp.set_cookie("time", str(random.randint(1, 100000)))
            return resp
        else:
            try:
                methods = get_connect(login=login, password=password, twofactor=True, cookies=None, auth=False)
                methods = twofactor(methods, login, password)
            except Exception:
                return render_template("failed_login.html")
            resp = make_response(render_template("code.html"))
            temp = b64encode(pickle.dumps(methods)).decode()
            for i in range(1, 6):
                resp.set_cookie("methods"+str(i), temp[(i-1)*len(temp)//5:i*len(temp)//5])
            return resp
if __name__ == '__main__':
    app.run()
