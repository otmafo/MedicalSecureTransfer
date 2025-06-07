package common;

import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class CryptoUtils {
    private static final String ALGORITHM = "DES";
    private static final String TRANSFORMATION = "DES/ECB/PKCS5Padding";
    private static final int BUFFER_SIZE = 8192; // 8KB缓冲区

    public static void encryptFile(SecretKey key, File inputFile, OutputStream outputStream) 
            throws CryptoException {
        doCrypto(Cipher.ENCRYPT_MODE, key, inputFile, outputStream);
    }

    public static void decryptFile(SecretKey key, InputStream inputStream, File outputFile) 
            throws CryptoException {
        doCrypto(Cipher.DECRYPT_MODE, key, inputStream, outputFile);
    }

    private static void doCrypto(int mode, SecretKey key, Object input, Object output) 
            throws CryptoException {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(mode, key);

            if (mode == Cipher.ENCRYPT_MODE) {
                try (InputStream in = new FileInputStream((File) input);
                     CipherOutputStream out = new CipherOutputStream((OutputStream) output, cipher)) {
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
            } else {
                try (CipherInputStream in = new CipherInputStream((InputStream) input, cipher);
                     OutputStream out = new FileOutputStream((File) output)) {
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
            }
        } catch (InvalidKeyException e) {
            throw new CryptoException("无效密钥: " + e.getMessage() + 
                " (密钥长度: " + key.getEncoded().length + " bytes)", e);
        } catch (Exception ex) {
            throw new CryptoException("加解密错误: " + ex.getClass().getName() + 
                " - " + ex.getMessage(), ex);
        }
    }

    public static SecretKey generateKey(String keyString) 
            throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keyBytes = keyString.getBytes();
        if (keyBytes.length != 8) {
            // 如果长度不够，填充到8字节
            byte[] paddedKey = new byte[8];
            System.arraycopy(keyBytes, 0, paddedKey, 0, Math.min(keyBytes.length, 8));
            keyBytes = paddedKey;
        }        
        DESKeySpec keySpec = new DESKeySpec(keyString.getBytes());
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
        return keyFactory.generateSecret(keySpec);
    }

    public static class CryptoException extends Exception {
        public CryptoException(String message, Throwable throwable) {
            super(message, throwable);
        }
    }
}