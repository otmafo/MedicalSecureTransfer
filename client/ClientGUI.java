package client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.awt.event.ActionEvent;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import common.CryptoUtils;
import common.DICOMUtils;

public class ClientGUI extends JFrame {
    private JTextField filePathField;
    private JTextArea infoArea;
    private File selectedFile;
    private final SecretKey secretKey;

    public ClientGUI() {
        // 使用固定密钥（实际应用中应从安全存储获取）
        byte[] keyBytes = "mysctKey".getBytes();
        secretKey = new SecretKeySpec(keyBytes, "DES");
        
        setTitle("DICOM加密客户端");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // 顶部面板
        JPanel topPanel = new JPanel(new BorderLayout(70, 10));
        filePathField = new JTextField(20);
        filePathField.setEditable(false);
        JButton browseBtn = new JButton("浏览");
        JButton sendBtn = new JButton("加密并发送");
        
        browseBtn.addActionListener(this::browseFile);
        sendBtn.addActionListener(this::sendFile);
        
        JPanel filePanel = new JPanel();
        filePanel.add(new JLabel("选择DICOM文件:"));
        filePanel.add(filePathField);
        filePanel.add(browseBtn);
        
        topPanel.add(filePanel, BorderLayout.CENTER);
        topPanel.add(sendBtn, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);
        
        // 信息显示区域
        infoArea = new JTextArea();
        infoArea.setEditable(false);
        add(new JScrollPane(infoArea), BorderLayout.CENTER);
        
        setVisible(true);
    }
    
    private void browseFile(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            filePathField.setText(selectedFile.getAbsolutePath());
            try {
                String dicomInfo = DICOMUtils.parseDICOM(selectedFile);
                infoArea.setText("DICOM文件信息:\n" + dicomInfo);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "解析DICOM文件失败: " + ex.getMessage());
            }
        }
    }
    
    private void sendFile(ActionEvent e) {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this, "请先选择文件");
            return;
        }
        
        new Thread(() -> {
            try (Socket socket = new Socket("localhost", 8888);
                 OutputStream os = socket.getOutputStream()) {
                
                // 发送原始文件名
                DataOutputStream dos = new DataOutputStream(os);
                dos.writeUTF(selectedFile.getName());
                
                // 加密并发送文件
                CryptoUtils.encryptFile(secretKey, selectedFile, os);
                JOptionPane.showMessageDialog(this, "文件加密发送成功!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "发送失败: " + ex.getMessage());
            }
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientGUI::new);
    }
}