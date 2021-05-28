package com.company;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.util.Scanner;


public class Main {
    public static Scanner in = new Scanner(System.in);
    public static PrintStream out = System.out;

    public static void main(String[] args) throws Exception {
        BetterLoginForm window1 = new BetterLoginForm();
        MessengerWindow window2 = new MessengerWindow();

        window1.setMethod(window2.getClass().getMethod("init", ConnectMachine.class), window2);
        window1.init();
    }

    public static ImageIcon createIcon(String path) {
        URL imgURL = Main.class.getResource(path);
        //System.out.println(imgURL);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("File not found " + path);
            return null;
        }
    }
}
