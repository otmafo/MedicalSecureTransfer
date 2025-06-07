package server;

import common.CryptoUtils;
import common.DICOMUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class ServerGUI extends JFrame {
    private JTextArea logArea;
    private final SecretKey secretKey;
    private final String savePath = "received_files/";
    
    public ServerGUI() {
        // 使用与客户端相同的密钥
        byte[] keyBytes = "mysctKey".getBytes();
        secretKey = new SecretKeySpec(keyBytes, "DES");
        
        // 创建保存目录
        new File(savePath).mkdirs();
        
        setTitle("DICOM解密服务器");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        logArea = new JTextArea();
        logArea.setEditable(false);
        add(new JScrollPane(logArea), BorderLayout.CENTER);
        
        JButton startBtn = new JButton("启动服务器");
        startBtn.addActionListener(e -> startServer());
        add(startBtn, BorderLayout.SOUTH);
        
        setVisible(true);
    }
    
    private void startServer() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(8888)) {
                log("服务器已启动，监听端口: 8888");
                while (true) {
                    try (Socket socket = serverSocket.accept();
                         InputStream is = socket.getInputStream()) {
                         
                        new FileReceiver(secretKey, savePath, logArea).process(socket, is);
                    } catch (Exception ex) {
                        log("连接错误: " + ex.getMessage());
                    }
                }
            } catch (Exception ex) {
                log("服务器启动失败: " + ex.getMessage());
            }
        }).start();
    }
    
    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ServerGUI::new);
    }
}