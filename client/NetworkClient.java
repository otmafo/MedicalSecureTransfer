package client;

import java.io.*;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PublicKey;

public class NetworkClient {
    public static void sendFile(File file, byte[] secretKey, PublicKey serverPublicKey, KeyPair clientKeyPair) {
        try (Socket socket = new Socket("localhost", 9090);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
             FileInputStream fis = new FileInputStream(file)) {
            
            // 1. 发送客户端公钥
            oos.writeObject(clientKeyPair.getPublic().getEncoded());
            oos.flush();
            
            // 2. 接收服务器公钥
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            byte[] serverPubKeyBytes = (byte[]) ois.readObject();
            
            // 3. 发送加密文件
            oos.writeObject(secretKey);
            oos.flush();
            
            // 4. 发送文件大小
            oos.writeLong(file.length());
            oos.flush();
            
            // 5. 发送文件内容
            byte[] buffer = new byte[8192];
            int count;
            while ((count = fis.read(buffer)) > 0) {
                oos.write(buffer, 0, count);
            }
            oos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}