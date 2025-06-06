package server;

import shared.CryptoUtils;
import java.io.*;
import java.util.Arrays;

public class FileDecryptor {
    public static void decryptFile(File inputFile, File outputFile, byte[] key) 
            throws Exception {
        
        try (InputStream in = new FileInputStream(inputFile);
             OutputStream out = new FileOutputStream(outputFile)) {
            
            byte[] sizeBuffer = new byte[4];
            
            while (in.read(sizeBuffer) != -1) {
                int chunkSize = byteArrayToInt(sizeBuffer);
                byte[] encrypted = new byte[chunkSize];
                in.read(encrypted);
                
                byte[] decrypted = CryptoUtils.decrypt(encrypted, key);
                out.write(decrypted);
            }
        }
    }
    
    private static int byteArrayToInt(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
               ((bytes[1] & 0xFF) << 16) |
               ((bytes[2] & 0xFF) << 8) |
               (bytes[3] & 0xFF);
    }
}