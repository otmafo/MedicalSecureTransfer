package server;

import common.CryptoUtils;
import common.DICOMUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;

public class ServerGUI extends JFrame {
    private JTextArea logArea;
    private final SecretKey secretKey;
    private final String savePath = "received_files/";
    private JButton browseBtn, parseBtn; // 手动解析按钮
    private JTextField filePathField;

    public ServerGUI() {
        // 使用固定密钥
        byte[] keyBytes = "mysctKey".getBytes();
        secretKey = new SecretKeySpec(keyBytes, "DES");

        // 创建保存目录
        new File(savePath).mkdirs();

        setTitle("DICOM解密服务器");
        setSize(1000, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 顶部面板 - 手动解析区域
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 40, 10));
        filePathField = new JTextField(20);
        filePathField.setEditable(false);
        browseBtn = new JButton("浏览接收的文件");
        parseBtn = new JButton("解析DICOM");

        browseBtn.addActionListener(this::browseReceivedFile);
        parseBtn.addActionListener(this::parseDicom);
        parseBtn.setEnabled(false);

        topPanel.add(new JLabel("选择DICOM文件:"));
        topPanel.add(filePathField);
        topPanel.add(browseBtn);
        topPanel.add(parseBtn);
        add(topPanel, BorderLayout.NORTH);

        // 日志区域
        logArea = new JTextArea();
        logArea.setEditable(false);
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        JButton startBtn = new JButton("启动服务器");
        startBtn.addActionListener(e -> startServer());
        add(startBtn, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void browseReceivedFile(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser(savePath);
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            filePathField.setText(selectedFile.getAbsolutePath());
            parseBtn.setEnabled(true);
        }
    }

    private void parseDicom(ActionEvent e) {
    String filePath = filePathField.getText();
    if (filePath.isEmpty()) {
        JOptionPane.showMessageDialog(this, "请先选择文件");
        return;
    }

    File dicomFile = new File(filePath);
    if (!dicomFile.exists()) {
        JOptionPane.showMessageDialog(this, "文件不存在");
        return;
    }

    // 在新线程中执行解析操作，避免阻塞事件处理线程
    new Thread(() -> {
        try {
            String dicomInfo = DICOMUtils.parseDICOM(dicomFile);
            log("手动解析结果:\n" + dicomInfo);
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this,
                        "DICOM信息:\n" + dicomInfo,
                        "解析成功",
                        JOptionPane.INFORMATION_MESSAGE);
            });
        } catch (IOException ex) {
            log("手动解析失败: " + ex.getMessage());
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this,
                        "解析失败: " + ex.getMessage(),
                        "错误",
                        JOptionPane.ERROR_MESSAGE);
            });
        }
    }).start();
}

    private void startServer() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(8888)) {
                log("服务器已启动，监听端口: 8888");
                while (true) {
                    try (Socket socket = serverSocket.accept()) {
                        new FileReceiver(secretKey, savePath, logArea).process(socket);
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
            logArea.append("[Server] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ServerGUI::new);
    }
}
