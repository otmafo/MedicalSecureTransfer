package server;

import javax.swing.SwingUtilities;
public class ServerMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ServerUI server = new ServerUI();
            server.setVisible(true);
        });
    }
}