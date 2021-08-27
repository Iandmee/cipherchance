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
    public boolean ours;
    public int id;

    public Message(String text, boolean ourMessage) {
        super();
        setText(text);
        txt = text;
        setEditable(false);
        setWrapStyleWord(true);
        setLineWrap(true);

        ours = ourMessage;

        //System.out.println(text);
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

    public int getMinimalHeight(int width) {
        FontMetrics metric = getFontMetrics(getFont());
        int result = 0;
        StringBuilder curStr = new StringBuilder();
        for (char c : txt.toCharArray()) {
            if (c == '\n') {
                result += getPixelHeightForLine(curStr.toString(), metric, width);
                curStr.delete(0, curStr.length() - 1);
            } else {
                curStr.append(c);
            }
        }
        if (curStr.length() != 0) {
            result += getPixelHeightForLine(curStr.toString(), metric, width);
        }
        return result;
    }

    private int getPixelHeightForLine(String line, FontMetrics metric, int width) {
        int curW = 0;
        int curH = 1;
        for (char c : line.toCharArray()) {
            if (curW + metric.charWidth(c) > width) {
                curW = metric.charWidth(c);
                curH++;
            } else {
                curW += metric.charWidth(c);
            }
        }
        return curH * metric.getHeight();
    }
}
