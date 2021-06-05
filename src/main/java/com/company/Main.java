package com.company;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.util.Scanner;


public class Main {
    public static Scanner in = new Scanner(System.in);
    public static PrintStream out = System.out;
    public static CacheManager cache;
    public static String pathPrefix;

    public static void main(String[] args) throws Exception {
        pathPrefix = Main.class.getResource("").getPath();
        String cachePassword = JOptionPane.showInputDialog(null, "Введите пароль для доступа к кэшу");
        cache = new CacheManager(cachePassword);

        ConnectMachine session = new ConnectMachine();
        MessengerWindow window = new MessengerWindow();
        window.init(session);
    }

    public static ImageIcon createIcon(String path) {
        //System.out.println(Main.class.getResource(""));
        URL imgURL = Main.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.out.println("File not found " + path);
            return null;
        }
    }
}
