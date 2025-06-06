package shared;

import javax.crypto.KeyAgreement;
import javax.crypto.spec.DESKeySpec;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;

public class KeyExchange {
    public static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DH");
        kpg.initialize(512);
        return kpg.generateKeyPair();
    }

    public static byte[] generateSharedSecret(PrivateKey privateKey, byte[] publicKeyBytes) throws Exception {
        KeyFactory kf = KeyFactory.getInstance("DH");
        X509EncodedKeySpec x509Spec = new X509EncodedKeySpec(publicKeyBytes);
        PublicKey publicKey = kf.generatePublic(x509Spec);

        KeyAgreement ka = KeyAgreement.getInstance("DH");
        ka.init(privateKey);
        ka.doPhase(publicKey, true);
        
        // 生成DES密钥（取前8字节）
        byte[] sharedSecret = ka.generateSecret();
        return java.util.Arrays.copyOf(sharedSecret, 8);
    }

    public static boolean validateDESKey(byte[] key) {
        try {
            new DESKeySpec(key);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}