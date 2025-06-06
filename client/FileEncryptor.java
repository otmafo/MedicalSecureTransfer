package client;

import shared.CryptoUtils;
import java.io.*;
import java.util.Arrays;

public class FileEncryptor {
    private static final int CHUNK_SIZE = 8192; // 8KB chunks
    
    public static void encryptFile(File inputFile, File outputFile, byte[] key) 
            throws Exception {
        
        try (InputStream in = new FileInputStream(inputFile);
             OutputStream out = new FileOutputStream(outputFile)) {
            
            byte[] buffer = new byte[CHUNK_SIZE];
            int bytesRead;
            
            while ((bytesRead = in.read(buffer)) != -1) {
                byte[] chunk = bytesRead == CHUNK_SIZE ? 
                    buffer : Arrays.copyOf(buffer, bytesRead);
                
                byte[] encrypted = CryptoUtils.encrypt(chunk, key);
                out.write(intToByteArray(encrypted.length));
                out.write(encrypted);
            }
        }
    }
    
    private static byte[] intToByteArray(int value) {
        return new byte[] {
            (byte)(value >> 24),
            (byte)(value >> 16),
            (byte)(value >> 8),
            (byte)value
        };
    }
}