package com.company;

import javax.swing.*;
import java.util.ArrayList;

public class Dialogue {
    ConnectMachine session = null;
    ArrayList<CipherMachine> cryptoManagers = null;
    private int dialogueId, cryptoManagerId = -1;
    private String name;
    public JLabel renderLabel;
    private ImageIcon imageIcon;


    public Dialogue(int dId, ConnectMachine curSession) {
        dialogueId = dId;
        session = curSession;
        name = session.getNameByDialogueId(dId);
        cryptoManagers = new ArrayList<>();

        imageIcon = Main.createIcon(session.getDialogueImagePath(dialogueId));
        renderLabel = new JLabel(name, imageIcon, JLabel.LEFT);
    }


    public String getName() {
        return name;
    }

    public JLabel getRenderObject() {
        //System.out.println("Asked " + name);
        return renderLabel;
    }

    public Message[] getAllMessages() {
        int lastId = session.getLastMessageId(dialogueId);
        ArrayList<Message> res = new ArrayList<>();
        while (lastId != -1) {
            res.add(session.getMessage(dialogueId, lastId));
            lastId = session.getPreviousMessageId(dialogueId, lastId);
        }
        Message[] finalResult = new Message[res.size()];
        for (int i = res.size() - 1, x = 0; i >= 0; i--, x++) {
            finalResult[x] = res.get(i);
        }
        return finalResult;
    }

    public void sendMessage(String msg) {
        String sendString = null;

        if (cryptoManagerId != -1) {
            sendString = "$%$cipId=" + cryptoManagerId + "&message=";
            sendString += new String(cryptoManagers.get(cryptoManagerId).encrypt(msg));
        } else {
            sendString = msg;
        }
        System.out.println("Sent message: " + sendString);

        session.sendTextMessage(dialogueId, sendString);
    }

    public void addNewCipher(String alg, String mode, String padding, int keyLength) {
        cryptoManagers.add(new CipherMachine(alg, mode, padding, keyLength));
        cryptoManagerId = cryptoManagers.size() - 1;
        System.out.println(cryptoManagerId);
    }

    public void abortCipher() {
        cryptoManagerId = -1;
    }

    public boolean isEncryptionEnabled() {
        return cryptoManagerId != -1;
    }
}
