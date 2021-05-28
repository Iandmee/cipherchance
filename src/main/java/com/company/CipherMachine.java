package com.company;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.Security;

public class CipherMachine {
    private Cipher encryptor, decryptor;
    private final SecureRandom rnd;
    private SecretKey key;

    CipherMachine(String alg, String mode, String padding, int keyBitLen) {
        // Used when we want to init new random cipher
        Security.addProvider(new BouncyCastleProvider());
        Security.setProperty("crypto.policy", "unlimited");

        rnd = new SecureRandom();
        try {
            encryptor = Cipher.getInstance(alg + "/" + mode + "/" + padding);
            decryptor = Cipher.getInstance(alg + "/" + mode + "/" + padding);

            KeyGenerator gen = KeyGenerator.getInstance(alg);
            gen.init(keyBitLen, rnd);
            key = gen.generateKey();

            byte[] iv = new byte[encryptor.getBlockSize()];
            rnd.nextBytes(iv);

            encryptor.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
            decryptor.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        } catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        }
    }

    CipherMachine(String alg, String mode, String padding, String textKey) {
        // Used when have user's text key. Key length is maximum
        Security.addProvider(new BouncyCastleProvider());
        Security.setProperty("crypto.policy", "unlimited");

        rnd = new SecureRandom();
        try {
            encryptor = Cipher.getInstance(alg + "/" + mode + "/" + padding);
            decryptor = Cipher.getInstance(alg + "/" + mode + "/" + padding);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(textKey.getBytes(StandardCharsets.UTF_8));
            key = new SecretKeySpec(hash, alg);

            byte[] iv = new byte[encryptor.getBlockSize()];
            rnd.nextBytes(iv);

            encryptor.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
            decryptor.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        } catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        }
    }

    CipherMachine(String alg, String mode, String padding, SecretKey _key, byte[] iv) {
        // Used when want to get old state
        Security.addProvider(new BouncyCastleProvider());
        Security.setProperty("crypto.policy", "unlimited");

        rnd = new SecureRandom();
        try {
            encryptor = Cipher.getInstance(alg + "/" + mode + "/" + padding);
            decryptor = Cipher.getInstance(alg + "/" + mode + "/" + padding);

            key = _key;

            encryptor.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
            decryptor.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        } catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        }
    }

    private byte[] feed(byte[] input, Cipher cipher) {
        try {
            return cipher.doFinal(input);
        } catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        }
        return null;
    }

    public byte[] encrypt(byte[] text) {
        return feed(text, encryptor);
    }

    public byte[] encrypt(String text) {
        return encrypt(text.getBytes(StandardCharsets.UTF_8));
    }

    public byte[] decrypt(byte[] cipherText) {
        return feed(cipherText, decryptor);
    }

    public byte[] getIV() {
        return encryptor.getIV();
    }

    public SecretKey getKey() {
        return key;
    }
}
