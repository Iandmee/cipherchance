package com.company;

import javax.swing.*;
import java.util.ArrayList;

public class Dialogue {
    ConnectMachine session = null;
    ArrayList<CipherMachine> cryptoManagers = null;
    private int dialogueId, cryptoManagerId = -1;
    public JLabel renderLabel;
    private ImageIcon imageIcon;
    private int lastMessageId = -1;



    public Dialogue(int dId, ConnectMachine curSession, String name, String iconPath) {
        dialogueId = dId;
        session = curSession;
        cryptoManagers = new ArrayList<>();

        imageIcon = Main.createIcon(iconPath);
        renderLabel = new JLabel(name, imageIcon, JLabel.LEFT);
    }

    public JLabel getRenderObject() {
        //System.out.println("Asked " + name);
        return renderLabel;
    }

    public Message[] getAllMessages() {
        return session.getAllMessagesFromDialogue(dialogueId);
    }

    public void sendMessage(String msg) {
        StringBuilder sendString = null;

        if (cryptoManagerId != -1) {
            byte[] encryptedMsg = cryptoManagers.get(cryptoManagerId).encrypt(msg);
            sendString = new StringBuilder("$%$ " + cryptoManagerId + " " + encryptedMsg.length + " ");
            sendString.append("[");
            for (int i = 0; i < encryptedMsg.length; i++) {
                sendString.append(encryptedMsg[i]);
                if (i + 1 != encryptedMsg.length) {
                    sendString.append(" ");
                }
            }
            sendString.append("]");
        } else {
            sendString = new StringBuilder(msg);
        }
        //System.out.println("Sent message: " + sendString);

        session.sendTextMessage(dialogueId, sendString.toString());
    }

    public void addNewCipher(String alg, String mode, String padding, int keyLength) {
        cryptoManagers.add(new CipherMachine(alg, mode, padding, keyLength));
        cryptoManagerId = cryptoManagers.size() - 1;
        //System.out.println(cryptoManagerId);
    }

    public void abortCipher() {
        cryptoManagerId = -1;
    }

    public boolean isEncryptionEnabled() {
        return cryptoManagerId != -1;
    }
}
