import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalTime;

public class Client {
    private JTextField Username;
    private JTextField Address;
    private JTextField Port;
    private JButton startChattingButton;
    private JTextArea messageInput;
    private JButton sendButton;
    private JPanel Client;
    static DefaultListModel<Object> defaultListModel = new DefaultListModel<>();
    private JList chatField;

    private static String username;
    private static String address;
    private static int port;

    private static Socket socket;
    private static DataInputStream dis;
    private static DataOutputStream dos;

    private static JFrame frame = new JFrame("JLiteChat Client");

    public Client() {
        chatField.setModel(defaultListModel);
        startChattingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    username = Username.getText();
                    address = Address.getText();
                    port = Integer.parseInt(Port.getText());
                    socket = new Socket(address, port);
                    dis = new DataInputStream(socket.getInputStream());
                    dos = new DataOutputStream(socket.getOutputStream());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Input error.\n" + ex);
                    return;
                }
                Username.setEnabled(false);
                Address.setEnabled(false);
                Port.setEnabled(false);
                startChattingButton.setEnabled(false);
                Refresh refresh = new Refresh(dis);
                refresh.start();
            }
        });
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    dos.writeUTF(LocalTime.now() + " | " + username + ": " + messageInput.getText());
                    messageInput.setText("");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    protected void start () {
        frame.setContentPane(new Client().Client);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
    private static Client client;
    public static void main(String[] args) {
        client = new Client();
        client.start();
    }

    private static class Refresh extends Thread {
        private final DataInputStream dis;
        public Refresh(DataInputStream dis) {
            this.dis = dis;
        }

        @Override
        public void run() throws RuntimeException {
            try {
                String msg;
                while(true) {
                    msg = dis.readUTF();
                    String finalMsg = msg;
                    SwingUtilities.invokeLater(() -> {
                        defaultListModel.add(defaultListModel.size(), finalMsg);
                        defaultListModel.addElement("--------------------");
                        client.chatField.setModel(defaultListModel);
                    });
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}