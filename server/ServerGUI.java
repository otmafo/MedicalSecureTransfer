package server;

import javax.swing.*;
import java.awt.*;

public class ServerGUI extends JFrame {
    private final JTextArea logArea = new JTextArea(20, 60);
    
    public ServerGUI() {
        setTitle("医疗影像安全传输服务器");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        add(scrollPane, BorderLayout.CENTER);
        
        JButton startBtn = new JButton("启动服务器");
        add(startBtn, BorderLayout.SOUTH);
        
        startBtn.addActionListener(e -> {
            startBtn.setEnabled(false);
            NetworkServer.start(9090, this);
        });
        
        pack();
        setLocationRelativeTo(null);
    }
    
    public void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ServerGUI().setVisible(true));
    }
}