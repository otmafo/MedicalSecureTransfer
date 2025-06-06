package server;

import shared.KeyExchange;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PublicKey;

public class NetworkServer {
    public static void start(int port, ServerGUI gui) {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                gui.log("服务器启动，监听端口 " + port);
                
                while (true) {
                    try (Socket clientSocket = serverSocket.accept();
                         ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
                         ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream())) {
                        
                        gui.log("客户端连接: " + clientSocket.getInetAddress());
                        
                        // 1. 接收客户端公钥
                        byte[] clientPubKeyBytes = (byte[]) ois.readObject();
                        
                        // 2. 生成服务器密钥对
                        KeyPair serverKeyPair = KeyExchange.generateKeyPair();
                        
                        // 3. 发送服务器公钥
                        oos.writeObject(serverKeyPair.getPublic().getEncoded());
                        oos.flush();
                        
                        // 4. 接收共享密钥
                        byte[] secretKey = (byte[]) ois.readObject();
                        
                        // 5. 接收文件大小
                        long fileSize = ois.readLong();
                        gui.log("接收文件大小: " + fileSize + " bytes");
                        
                        // 6. 接收文件内容
                        File encryptedFile = File.createTempFile("encrypted_", ".dat");
                        try (FileOutputStream fos = new FileOutputStream(encryptedFile)) {
                            byte[] buffer = new byte[8192];
                            long totalRead = 0;
                            int count;
                            
                            while (totalRead < fileSize) {
                                count = ois.read(buffer);
                                if (count == -1) break;
                                fos.write(buffer, 0, count);
                                totalRead += count;
                            }
                        }
                        
                        gui.log("文件接收完成，开始解密...");
                        
                        // 7. 解密文件
                        File decryptedFile = File.createTempFile("decrypted_", ".dcm");
                        FileDecryptor.decryptFile(encryptedFile, decryptedFile, secretKey);
                        
                        // 8. 解析DICOM信息
                        String dicomInfo = DicomParser.parseDicomInfo(decryptedFile);
                        gui.log("解密文件信息:\n" + dicomInfo);
                        
                        // 9. 清理临时文件
                        encryptedFile.delete();
                        decryptedFile.delete();
                        
                    } catch (Exception e) {
                        e.printStackTrace();
                        gui.log("错误: " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                gui.log("服务器错误: " + e.getMessage());
            }
        }).start();
    }
}