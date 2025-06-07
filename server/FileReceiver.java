package server;

import common.CryptoUtils;
import common.DICOMUtils;
import common.CryptoUtils.CryptoException;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.crypto.SecretKey;

public class FileReceiver {
    private final SecretKey secretKey;
    private final String savePath;
    private final JTextArea logArea;
    
    public FileReceiver(SecretKey secretKey, String savePath, JTextArea logArea) {
        this.secretKey = secretKey;
        this.savePath = savePath;
        this.logArea = logArea;
    }
    
    public void process(Socket socket, InputStream is) throws Exception {
        DataInputStream dis = new DataInputStream(is);
        
        // 读取原始文件名
        String originalFileName = dis.readUTF();
        Path outputPath = Paths.get(savePath, originalFileName);
        
        log("收到文件: " + originalFileName);
        log("开始解密...");
        
        try {
            // 解密文件
            CryptoUtils.decryptFile(secretKey, is, outputPath.toFile());
            log("解密完成: " + outputPath);
            
            // 显示DICOM信息
            String dicomInfo = DICOMUtils.parseDICOM(outputPath.toFile());
            log("DICOM信息:\n" + dicomInfo);
            
            // 显示解密后的文件
            JOptionPane.showMessageDialog(null, 
                "文件接收并解密成功!\n" + dicomInfo, 
                "完成", 
                JOptionPane.INFORMATION_MESSAGE);
        } catch (CryptoException | IOException ex) {
            log("解密失败: " + ex.getMessage());
            // 清理不完整的文件
            Files.deleteIfExists(outputPath);
        }
    }
    
    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append("[Server] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
}