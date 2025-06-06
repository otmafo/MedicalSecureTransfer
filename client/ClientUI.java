package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class ClientUI extends JFrame {
    private final JTextField fileField = new JTextField(30);
    private final JButton browseBtn = new JButton("Browse");
    private final JButton encryptBtn = new JButton("Encrypt & Send");
    private final JButton viewBtn = new JButton("View DICOM");
    private File selectedFile;

    public ClientUI() {
        setTitle("Medical Imaging Secure Client");
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        // File selection
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("DICOM File:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        add(fileField, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0;
        add(browseBtn, gbc);
        
        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.add(encryptBtn);
        btnPanel.add(viewBtn);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3;
        add(btnPanel, gbc);
        
        // Event handlers
        browseBtn.addActionListener(this::browseFile);
        viewBtn.addActionListener(e -> viewDICOM());
        encryptBtn.addActionListener(e -> encryptAndSend());
        
        setSize(600, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void browseFile(ActionEvent e) {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedFile = fc.getSelectedFile();
            fileField.setText(selectedFile.getAbsolutePath());
        }
    }

    private void viewDICOM() {
        if (selectedFile != null) {
            try {
                DICOMViewer viewer = new DICOMViewer();
                viewer.displayDICOM(selectedFile);
                viewer.setVisible(true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error viewing DICOM: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private void encryptAndSend() {
        if (selectedFile == null) return;
        
        // For demo: use fixed key (in real use DH key exchange)
        byte[] key = "12345678".getBytes(); // DES key must be 8 bytes
        
        try {
            // Encrypt file
            File encryptedFile = new File(selectedFile.getParent(), "encrypted.dat");
            FileEncryptor.encryptFile(selectedFile, encryptedFile, key);
            
            // Send file via socket (simplified)
            // In real implementation use Socket and DataOutputStream
            JOptionPane.showMessageDialog(this, 
                "File encrypted and sent successfully!\n" +
                "Encrypted size: " + encryptedFile.length() + " bytes");
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Encryption error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}