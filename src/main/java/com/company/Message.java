package com.company;

import javax.swing.*;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

public class Message extends JTextArea {
    String txt;

    public Message(String text, boolean ourMessage) {
        super();
        setText(text);
        txt = text;
        setEditable(false);
        setLineWrap(true);

        System.out.println(text);
    }

    public Message(URI link, String shortName, boolean ourMessage) {
        super();
        setText(shortName);
//        if (ourMessage) {
//            setHorizontalAlignment(JLabel.RIGHT);
//        } else {
//            setHorizontalAlignment(JLabel.LEFT);
//        }
        setForeground(Color.BLUE.darker());
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(link);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setText(txt);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                setText("<html><a href=''>" + txt + "</a></html>");
            }
        });
    }
}
