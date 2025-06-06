package server;

import shared.KeyExchange;
import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class FileDecryptor {
    private static final int BUFFER_SIZE = 8200; // 加密后数据可能稍大

    public static void decryptFile(File inputFile, File outputFile, byte[] key) 
            throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, 
                   InvalidKeyException, BadPaddingException, IllegalBlockSizeException, 
                   InvalidKeySpecException {
        
        if (!KeyExchange.validateDESKey(key)) {
            throw new InvalidKeyException("Invalid DES key");
        }

        try (InputStream in = new BufferedInputStream(new FileInputStream(inputFile));
             OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile))) {
            
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            SecretKey secretKey = SecretKeyFactory.getInstance("DES").generateSecret(new DESKeySpec(key));
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            
            // 分块解密
            while ((bytesRead = in.read(buffer)) != -1) {
                byte[] decryptedBlock = cipher.update(buffer, 0, bytesRead);
                if (decryptedBlock != null) {
                    out.write(decryptedBlock);
                }
            }
            
            byte[] finalBlock = cipher.doFinal();
            if (finalBlock != null) {
                out.write(finalBlock);
            }
        }
    }
}