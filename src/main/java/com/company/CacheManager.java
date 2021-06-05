package com.company;

import java.io.*;
import java.nio.file.Path;
import java.util.Scanner;

public class CacheManager {
    /*
    * files with tokens contain data in this way:
    * n b1 b2 ... bn
    * id
    * */
    CipherMachine cipher;

    public CacheManager(String password) {
        cipher = new CipherMachine("AES", "ECB", "ISO10126Padding", password);
    }

    private BufferedReader getFileReader(String fileName) {
        File file = new File(System.getProperty("user.dir") + fileName);
        if (!file.exists()) {
            return null;
        }
        try {
            return new BufferedReader(new FileReader(file));
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("An error while creating file reader");
            System.exit(1);
        }
        return null;
    }

    private PrintWriter getFileWriter(String fileName) {
        File file = new File(System.getProperty("user.dir") + fileName);
        //System.out.println(file.getAbsolutePath());
        try {
            file.createNewFile();
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("An error occurred while creating a new file");
            System.exit(1);
        }
        try {
            return new PrintWriter(file);
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("An error while creating file writer");
            System.exit(1);
        }
        return null;
    }

    private void writeByteArrayLine(PrintWriter out, byte[] arr) {
        out.print(arr.length + " ");
        for (int i = 0; i < arr.length; i++) {
            out.print(arr[i] + " ");
        }
        out.println();
    }

    private byte[] getByteArrayLine(BufferedReader fin) {
        Scanner in = new Scanner(fin);
        int n = in.nextInt();
        byte[] arr = new byte[n];
        for (int i = 0; i < n; i++) {
            arr[i] = in.nextByte();
        }
        return arr;
    }

    public void setToken(Integer id, String accessToken, boolean isUser) {
        PrintWriter fout = null;
        if (isUser) {
            fout = getFileWriter("\\cache\\userToken");
        } else {
            fout = getFileWriter("\\cache\\groupToken");
        }
        writeByteArrayLine(fout, cipher.encrypt(accessToken));
        fout.println(id);
        fout.close();
    }

    public String getToken(boolean isUser) {
        BufferedReader fin = null;
        if (isUser) {
            fin = getFileReader("\\cache\\userToken");
        } else {
            fin = getFileReader("\\cache\\groupToken");
        }
        if (fin == null) {
            return null;
        }
        byte[] encryptedToken;
        try {
            encryptedToken = getByteArrayLine(fin);
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("An error while reading token");
            return null;
        }
        try {
            byte[] rawToken = cipher.decrypt(encryptedToken);
            if (rawToken == null) {
                return null;
            }
            return new String(rawToken);
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("An error while decrypting token");
            return null;
        }
    }

    public Integer getId(boolean isUser) {
        BufferedReader fin = null;
        if (isUser) {
            fin = getFileReader("\\cache\\userToken");
        } else {
            fin = getFileReader("\\cache\\groupToken");
        }
        if (fin == null) {
            return null;
        }
        try {
            fin.readLine();
            return Integer.parseInt(fin.readLine());
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("An error while reading id");
            return null;
        }
    }
}
