package server;

import common.CryptoUtils;
import common.DICOMUtils;

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

    public void process(Socket socket) {
        try (InputStream is = socket.getInputStream();
             DataInputStream dis = new DataInputStream(is)) {

            // 读取文件名和文件大小
            String originalFileName = dis.readUTF();
            long expectedSize = dis.readLong();
            Path outputPath = Paths.get(savePath, originalFileName);
            Path encryptedPath = Paths.get(savePath, "encrypted_" + originalFileName);

            log("收到文件: " + originalFileName + " (" + expectedSize + " bytes)");

            // 创建目录
            Files.createDirectories(outputPath.getParent());

            // 保存加密文件
            try (OutputStream encryptedOut = new FileOutputStream(encryptedPath.toFile())) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = dis.read(buffer)) != -1) {
                    encryptedOut.write(buffer, 0, bytesRead);
                }
            }

            // 重新打开加密文件进行解密
            try (InputStream encryptedIn = new FileInputStream(encryptedPath.toFile())) {
                // 解密文件
                CryptoUtils.decryptFile(secretKey, encryptedIn, outputPath.toFile());
            }

            // 验证文件大小
            long actualSize = Files.size(outputPath);
            if (actualSize != expectedSize) {
                throw new IOException("文件大小不匹配: 期望 " + expectedSize + " 字节, 实际 " + actualSize + " 字节");
            }

            log("解密完成: " + outputPath);

            // 自动解析并显示DICOM信息
            parseAndShowDicomInfo(outputPath.toFile());

        } catch (Exception ex) {
            log("处理失败: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // 自动解析并显示DICOM信息
    private void parseAndShowDicomInfo(File dicomFile) {
        new Thread(() -> {
            try {
                // 等待文件完全写入
                Thread.sleep(500);

                if (!DICOMUtils.isDICOMFile(dicomFile)) {
                    log("警告: 文件不是有效的DICOM格式");
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(null,
                                    "文件解密成功，但不是有效的DICOM格式",
                                    "警告",
                                    JOptionPane.WARNING_MESSAGE)
                    );
                    return;
                }

                String dicomInfo = DICOMUtils.parseDICOM(dicomFile);
                log("自动解析结果:\n" + dicomInfo);

                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(null,
                                "文件接收并解密成功!\n" + dicomInfo,
                                "完成",
                                JOptionPane.INFORMATION_MESSAGE)
                );
            } catch (Exception ex) {
                log("自动解析失败: " + ex.getMessage());
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(null,
                                "文件解密成功，但解析DICOM信息失败\n" + ex.getMessage(),
                                "警告",
                                JOptionPane.WARNING_MESSAGE)
                );
            }
        }).start();
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append("[Receiver] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
}
