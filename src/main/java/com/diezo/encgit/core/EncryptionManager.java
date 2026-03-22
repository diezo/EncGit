package com.diezo.encgit.core;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class EncryptionManager {
    public static String generateKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            SecretKey secretKey = keyGen.generateKey();

            String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());

            String userHome = System.getProperty("user.home");

            Path dirPath = Paths.get(userHome, ".encgit-keys");
            Files.createDirectories(dirPath);

            File keyFile = null;
            int maxAttempts = 10;

            for (int i = 0; i <= maxAttempts; i++) {
                String filename = generateRandomFilename() + ".key";
                Path filePath = dirPath.resolve(filename);

                if (!Files.exists(filePath)) {
                    keyFile = filePath.toFile();
                    break;
                }
            }

            if (keyFile == null) {
                throw new IOException("Unable to generate unique key file!");
            }

            try (FileOutputStream fos = new FileOutputStream(keyFile)) {
                fos.write(encodedKey.getBytes());
            }

            return "ref: " + keyFile.getAbsolutePath();

        } catch (NoSuchAlgorithmException | IOException e) {
            System.out.println("Unable to generate secure key!");
            System.exit(-1);
        }

        return null;
    }

    public static SecretKeySpec loadSecureKey(Path encgitDir) {
        try {
            Path refKeyPath = encgitDir.resolve("ref.key");
            String keyPath = Files.readString(refKeyPath).trim();

            if (!keyPath.startsWith("ref: ")) throw new IOException();

            keyPath =  keyPath.substring("ref: ".length());

            String encodedKey = Files.readString(Paths.get(keyPath)).trim();
            byte[] decodedKey = Base64.getDecoder().decode(encodedKey);

            return new SecretKeySpec(decodedKey, "AES");

        } catch (IOException e) {
            System.out.println("Unable to load secure key!");
            System.exit(-1);
        }

        return null;
    }

    private static String generateRandomFilename() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);

        StringBuilder hex =  new StringBuilder(bytes.length * 2);

        for (byte b : bytes) {
            hex.append(String.format("%02x", b));
        }

        return hex.toString();
    }

    public static byte[] encrypt(byte[] data, SecretKey key) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(data);

        } catch (Exception e) {
            System.out.println("Unable to encrypt data!");
            System.exit(-1);
        }

        return null;
    }

    public static byte[] decrypt(byte[] data, SecretKey key) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(data);

        } catch (Exception e) {
            System.out.println("Unable to encrypt data!");
            System.exit(-1);
        }

        return null;
    }
}
