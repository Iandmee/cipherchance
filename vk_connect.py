from vk_messages import MessagesAPI
import os
import json


def send_message(methods:MessagesAPI, message:str, id:str, chat:bool)->bool:
    '''
    :param methods:
    :param message:
    :param id:
    :param chat: if it is identifies by chat_id (not user_id)
    :return: true or false depends on sending state
    '''
    if chat:
        try:
            methods.method('messages.send', chat_id=id, message=message)
            return True
        except Exception:
            return False
    else:
        try:
            methods.method('messages.send', user_id=id, message=message)
            return True
        except Exception:
            return False

def get_account_info(methods:MessagesAPI)->json:
    '''
    :param methods:
    :return:
    '''
    try:
        temp =  json.dumps(methods.method('account.getProfileInfo'))
    except Exception:
        return None
    return temp

def get_messages(methods:MessagesAPI,user_id:str,count:int)->json:
    '''
    :param methods:
    :param user_id:
    :return:
    '''
    try:
        temp = json.dumps(methods.method('messages.getHistory', user_id=user_id, count=count))
    except Exception:
        return None
    return temp
def get_friends(methods:MessagesAPI,order:str="hints")->dict:
    '''
    :param methods:
    :param return_system:
    :return: friendslist
    '''
    try:
        temp =  methods.method("friends.get",order=order,fields="nickname,photo_50,online")
    except Exception:
        return None
    return temp
def get_connect(login:str,password:str,twofactor:bool,cookies:dict,auth:bool)->MessagesAPI:
    '''
    :param login:
    :param password:
    :param twofactor:
    :return: methods for interacting with vk
    '''
    methods = None
    try:
        methods = MessagesAPI(login=login, password=password, two_factor=twofactor, cookies=cookies, auth=auth)
    except Exception:
        print(Exception)
    return methods




if __name__ == '__main__':
    login, password = map(str, input().split())
    get_connect(login, password, 1)
