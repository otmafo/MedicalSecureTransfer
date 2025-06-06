package shared;

import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class CryptoUtils {
    private static final String ALGORITHM = "DES";
    private static final String TRANSFORMATION = "DES/ECB/PKCS5Padding";

    public static byte[] encrypt(byte[] data, byte[] key) throws Exception {
        return process(Cipher.ENCRYPT_MODE, data, key);
    }

    public static byte[] decrypt(byte[] data, byte[] key) throws Exception {
        return process(Cipher.DECRYPT_MODE, data, key);
    }

    private static byte[] process(int mode, byte[] data, byte[] key) 
            throws NoSuchAlgorithmException, InvalidKeyException, 
            InvalidKeySpecException, NoSuchPaddingException, 
            BadPaddingException, IllegalBlockSizeException {
        
        DESKeySpec keySpec = new DESKeySpec(key);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
        SecretKey secretKey = keyFactory.generateSecret(keySpec);
        
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(mode, secretKey);
        
        return cipher.doFinal(data);
    }
}