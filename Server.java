import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Server {

    public JPanel Server;
    public JTextField port;
    private JButton setButton;
    private JList eventList;
    private JButton quitButton;
    private JButton startButton;

    static Server server;
    static Service service;
    static JFrame frame = new JFrame("JLiteChat Server");
    public void start() throws IOException {
        frame.setContentPane(this.Server);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        int portNum = 20000;
        service = new Service(portNum, frame);
    }

    public static void main(String[] args) throws IOException {
        server = new Server();
        server.start();
    }

    static DefaultListModel defaultListModel = new DefaultListModel();

    public Server() {
        setButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int portNum = 20000;
                try {
                    portNum = Integer.parseInt(server.port.getText());
                    service = new Service(portNum, frame);
                    server.port.setEnabled(false);
                    server.setButton.setEnabled(false);
                } catch (Exception inputErr) {JOptionPane.showMessageDialog(frame, "Warning: Port number must be int! Running on 20000 instead.\n" + inputErr);}
            }
        });
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                server.port.setEnabled(false);
                server.setButton.setEnabled(false);
                service.start();
            }
        });
        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
    }


    private static class Service extends Thread {
        private int port = 20000;

        public Service(int port, JFrame frame) throws IOException {
            this.port = port;
        }
        @Override
        public void run() {
            try {
                defaultListModel.add(defaultListModel.size(), "Server Started at %d. %s".formatted(port, LocalTime.now()));
                server.eventList.setModel(defaultListModel);
                ServerSocket serverSocket = new ServerSocket(port);
                while (true) {
                    Socket clientsocket = serverSocket.accept();
                    defaultListModel.add(defaultListModel.size(), "%s connected at %d. %s".formatted(clientsocket.getInetAddress().toString(), port, LocalTime.now()));
                    ClientHandler clientHandler = new ClientHandler(clientsocket);
                    clients.add(clientHandler);
                    clientHandler.start();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static List<ClientHandler> clients = new ArrayList<>();

    private static class ClientHandler extends Thread {
        private Socket socket;
        private DataInputStream dis;
        private DataOutputStream dos;

        public ClientHandler(Socket socket) throws IOException {
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
        }
        @Override
        public void run() {
            try {
                while (true) {
                    String msg = dis.readUTF();
                    defaultListModel.add(defaultListModel.size(), msg);
                    broadcastMessage(msg);
                }
            } catch (IOException e) {
                defaultListModel.add(defaultListModel.size(), "Error handling client: %s".formatted(e.getMessage()));
            } finally {
                try {
                    dis.close();
                    dos.close();
                    socket.close();
                } catch (IOException e) {
                    defaultListModel.add(defaultListModel.size(), "Error closing client socket: %s".formatted(e.getMessage()));
                }
            }
        }

        private void broadcastMessage(String message) {
            for (ClientHandler client : clients) {
                try {
                    client.dos.writeUTF(message);
                    client.dos.flush();
                } catch (IOException e) {
                    defaultListModel.add(defaultListModel.size(), "Error sending message to client: %s".formatted(e.getMessage()));
                }
            }
        }
    }
}
