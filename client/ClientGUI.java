package client;

import common.CryptoUtils;
import common.DICOMUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class ClientGUI extends JFrame {
    private JTextField filePathField;
    private JTextArea infoArea;
    private File selectedFile;
    private final SecretKey secretKey;
    private JLabel statusLabel;
    private JButton parseBtn; // 手动解析按钮

    public ClientGUI() {
        // 使用固定密钥
        byte[] keyBytes = "mysctKey".getBytes();
        secretKey = new SecretKeySpec(keyBytes, "DES");
        
        setTitle("DICOM加密客户端");
        setSize(1000, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // 顶部面板
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        filePathField = new JTextField(30);
        filePathField.setEditable(false);
        JButton browseBtn = new JButton("浏览");
        JButton sendBtn = new JButton("加密并发送");
        parseBtn = new JButton("解析DICOM"); // 手动解析按钮
        
        browseBtn.addActionListener(this::browseFile);
        sendBtn.addActionListener(this::sendFile);
        parseBtn.addActionListener(this::parseDicom); // 添加手动解析监听
        parseBtn.setEnabled(false); // 初始禁用
        
        JPanel filePanel = new JPanel();
        filePanel.add(new JLabel("选择DICOM文件:"));
        filePanel.add(filePathField);
        filePanel.add(browseBtn);
        filePanel.add(parseBtn); // 添加手动解析按钮
        
        topPanel.add(filePanel, BorderLayout.CENTER);
        topPanel.add(sendBtn, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);
        
        // 信息显示区域
        infoArea = new JTextArea();
        infoArea.setEditable(false);
        add(new JScrollPane(infoArea), BorderLayout.CENTER);
        
        // 添加状态标签
        statusLabel = new JLabel("就绪");
        add(statusLabel, BorderLayout.SOUTH);
        
        setVisible(true);
    }
    
    private void browseFile(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            filePathField.setText(selectedFile.getAbsolutePath());
            parseBtn.setEnabled(true); // 启用解析按钮
            
            // 自动解析
            autoParseDicom();
        }
    }
    
    // 自动解析DICOM
    private void autoParseDicom() {
        if (selectedFile == null) return;
        
        try {
            String dicomInfo = DICOMUtils.parseDICOM(selectedFile);
            infoArea.setText("DICOM文件信息:\n" + dicomInfo);
        } catch (IOException ex) {
            infoArea.setText("自动解析失败: " + ex.getMessage());
        }
    }
    
    // 手动解析按钮事件
    private void parseDicom(ActionEvent e) {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this, "请先选择文件");
            return;
        }
        
        try {
            String dicomInfo = DICOMUtils.parseDICOM(selectedFile);
            infoArea.setText("DICOM文件信息:\n" + dicomInfo);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, 
                "解析DICOM文件失败: " + ex.getMessage(),
                "错误",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void sendFile(ActionEvent e) {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this, "请先选择文件");
            return;
        }
        
        new Thread(() -> {
            statusLabel.setText("连接服务器...");
            try (Socket socket = new Socket("localhost", 8888);
                 OutputStream os = socket.getOutputStream();
                 DataOutputStream dos = new DataOutputStream(os)) {
                
                statusLabel.setText("发送文件信息...");
                // 发送原始文件名
                dos.writeUTF(selectedFile.getName());
                // 发送文件大小
                dos.writeLong(selectedFile.length());
                dos.flush(); // 确保文件信息已发送
                
                statusLabel.setText("加密并发送文件...");
                // 加密并发送文件
                CryptoUtils.encryptFile(secretKey, selectedFile, os);
                os.flush(); // 确保所有数据已刷新
                
                statusLabel.setText("文件发送完成");
                JOptionPane.showMessageDialog(this, "文件加密发送成功!");
            } catch (Exception ex) {
                statusLabel.setText("发送失败");
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, 
                    "发送失败: " + ex.getMessage(), 
                    "错误", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientGUI::new);
    }
}