package server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class ServerUI extends JFrame {
    private final JTextField fileField = new JTextField(30);
    private final JButton browseBtn = new JButton("Browse");
    private final JButton decryptBtn = new JButton("Decrypt");
    private final JButton viewBtn = new JButton("View DICOM");
    private File encryptedFile;

    public ServerUI() {
        setTitle("Medical Imaging Secure Server");
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        // File selection
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Encrypted File:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        add(fileField, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0;
        add(browseBtn, gbc);
        
        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.add(decryptBtn);
        btnPanel.add(viewBtn);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3;
        add(btnPanel, gbc);
        
        // Event handlers
        browseBtn.addActionListener(this::browseFile);
        decryptBtn.addActionListener(e -> decryptFile());
        viewBtn.addActionListener(e -> viewDICOM());
        
        setSize(600, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void browseFile(ActionEvent e) {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            encryptedFile = fc.getSelectedFile();
            fileField.setText(encryptedFile.getAbsolutePath());
        }
    }

    private void decryptFile() {
        if (encryptedFile == null) return;
        
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save Decrypted DICOM");
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File outputFile = fc.getSelectedFile();
            
            // For demo: use fixed key (same as client)
            byte[] key = "12345678".getBytes();
            
            try {
                FileDecryptor.decryptFile(encryptedFile, outputFile, key);
                JOptionPane.showMessageDialog(this, "File decrypted successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Decryption error: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private void viewDICOM() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File dicomFile = fc.getSelectedFile();
            try {
                client.DICOMViewer viewer = new client.DICOMViewer();
                viewer.displayDICOM(dicomFile);
                viewer.setVisible(true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error viewing DICOM: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
}