package client;

import shared.DicomParser;
import shared.KeyExchange;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.security.KeyPair;

public class ClientGUI extends JFrame {
    private File selectedFile;
    private final JTextArea infoArea = new JTextArea(15, 50);
    private final JButton selectBtn = new JButton("选择DICOM文件");
    private final JButton sendBtn = new JButton("加密并发送");
    private final JLabel statusLabel = new JLabel("就绪");
    
    public ClientGUI() {
        setTitle("医疗影像安全传输客户端");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // 顶部面板
        JPanel topPanel = new JPanel();
        topPanel.add(selectBtn);
        topPanel.add(sendBtn);
        add(topPanel, BorderLayout.NORTH);
        
        // 中央面板
        infoArea.setEditable(false);
        add(new JScrollPane(infoArea), BorderLayout.CENTER);
        
        // 底部状态栏
        statusLabel.setBorder(BorderFactory.createEtchedBorder());
        add(statusLabel, BorderLayout.SOUTH);
        
        // 按钮事件
        selectBtn.addActionListener(this::selectFile);
        sendBtn.addActionListener(this::sendFile);
        
        sendBtn.setEnabled(false);
        pack();
        setLocationRelativeTo(null);
    }
    
    private void selectFile(ActionEvent e) {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedFile = fc.getSelectedFile();
            infoArea.setText(DicomParser.parseDicomInfo(selectedFile));
            sendBtn.setEnabled(true);
            statusLabel.setText("已选择: " + selectedFile.getName());
        }
    }
    
    private void sendFile(ActionEvent e) {
        if (selectedFile == null) return;
        
        new Thread(() -> {
            try {
                statusLabel.setText("正在加密...");
                
                // 生成密钥对
                KeyPair clientKeyPair = KeyExchange.generateKeyPair();
                
                // 生成临时加密文件
                File encryptedFile = File.createTempFile("encrypted_", ".dat");
                
                // 使用固定密钥或DH协商密钥（此处简化）
                byte[] secretKey = "defaultKey".getBytes(); // 实际应用中使用DH协商
                
                // 加密文件
                FileEncryptor.encryptFile(selectedFile, encryptedFile, secretKey);
                
                statusLabel.setText("正在发送...");
                
                // 发送文件
                NetworkClient.sendFile(encryptedFile, secretKey, null, clientKeyPair);
                
                statusLabel.setText("发送完成");
                encryptedFile.delete();
                
            } catch (Exception ex) {
                ex.printStackTrace();
                statusLabel.setText("错误: " + ex.getMessage());
            }
        }).start();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientGUI().setVisible(true));
    }
}