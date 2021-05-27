# coding=utf-8
from vk_connect import *
from flask import render_template

def generate_html_messages(methods:MessagesAPI,friends:dict=None):
    '''
    :param methods:
    :param friends: friends list which we need to insert to webpage
    :return: main html page after login
    '''
    return render_template("messages.html",friends=friends)
