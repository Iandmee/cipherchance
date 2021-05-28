package com.company;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class ConnectMachine {
    ConnectMachine() {

    }

    public boolean init(String login, String password) {
        // TODO: tries to log in and returns true if succeed
        //System.out.println("Login = " + login);
        //System.out.println("Password = " + password);
        return true;
    }

    public Dialogue[] getDialogues() {
        int have = 2;
        Dialogue[] res = new Dialogue[have];
        for (int i = 0; i < have; i++) {
            res[i] = new Dialogue(i, this);
        }
        return res;
    }

    public Message getMessage(int dialogueId, int messageId) {
        Message res = new Message("dialogue " + dialogueId + " and message number " + messageId, messageId % 2 == 0);
        return res;
    }

    public int getPreviousMessageId(int dialogueId, int messageId) {
        return messageId - 1;
    }

    public int getNextMessageId(int dialogueId, int messageId) {
        if (messageId > 10) {
            return -1;
        }
        return messageId + 1;
    }

    public int getLastMessageId(int dialogueId) {
        return dialogueId + 5;
    }

    public String getNameByDialogueId(int dialogueId) {
        return "User " + dialogueId;
    }

    public String getDialogueImagePath(int dialogueId) {
        return "\\images\\dial" + dialogueId + ".png";
    }

    public void sendTextMessage(int dialogueId, String msg) {

    }
}
