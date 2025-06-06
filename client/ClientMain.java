package client;

import javax.swing.SwingUtilities;
public class ClientMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClientUI client = new ClientUI();
            client.setVisible(true);
        });
    }
}