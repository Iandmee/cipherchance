from cipherme.vk_messages import MessagesAPI
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

def get_messages(methods:MessagesAPI, user_id:str, count:int)->json:
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
def get_friends(methods:MessagesAPI, order:str = "hints")->dict:
    '''
    :param methods:
    :param return_system:
    :return: friendslist
    '''
    try:
        temp =  methods.method("friends.get",order = order,fields = "nickname,photo_50,online")
    except Exception:
        return None
    return temp
def get_connect(login:str, password:str, twofactor:bool, cookies:dict, auth:bool)->MessagesAPI:

    '''
    :param login:
    :param password:
    :param twofactor:
    :return: methods for interacting with vk
    '''

    methods = None
    try:
        methods = MessagesAPI(login = login, password = password, two_factor = twofactor, cookies = cookies, auth = auth)
    except Exception:
        print(Exception)
    return methods

def getConversations(methods:MessagesAPI)->dict:

    '''
    :param count: count of dialogs
    :param start_message_id: start message of the dialog from you count other dialogs
    :return: dialogs
    '''
    conversations = []
    count = 0
    while 1:
        try:
            temp =  methods.method("messages.getConversations", extended='1', count='200', fields="id,first_name,last_name,online,photo_50",offset = count)
            count += 200
            if len(temp['items']) == 0:
                break
            temp_conversation = {}
            for conversation in temp['items']:
                if conversation['conversation']['peer']['type'] == 'chat':
                    continue
                temp_conversation['id'] = conversation['conversation']['peer']['id']
                temp_conversation['last_message'] = conversation['last_message']['text']
                temp_conversation['date_of_last_message'] = conversation['last_message']['date']
                if 'unread_count' in conversation['conversation'].keys():
                    temp_conversation['unread_count'] = conversation['conversation']['unread_count']
                else:
                    temp_conversation['unread_count'] = '0'
                for profile in temp['profiles']:
                    if profile['id'] == temp_conversation['id']:
                        temp_conversation['first_name'] = profile['first_name']
                        temp_conversation['last_name'] = profile['last_name']
                        temp_conversation['photo_50'] = profile['photo_50']
                        temp_conversation['online'] = profile['online']
                        break
                conversations.append(temp_conversation)
                temp_conversation = {}

        except Exception:
            return conversations
    return conversations

if __name__ == '__main__':
    login, password = map(str, input().split())
    get_connect(login, password, 1)
