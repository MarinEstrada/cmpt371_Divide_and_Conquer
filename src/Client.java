import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Client extends JFrame {
    private Socket client;
    private int clientID;

    private JPanel boardPanel;
    private int[][] board;

    DataInputStream in;
    DataOutputStream out;

    public void clientGUI(int clientID) {
        board = new int[4][4];
        initializeBoard();

        setTitle("Deny and Conquer - Client #" + clientID);
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        boardPanel = new JPanel(new GridLayout(4, 4, 2, 2));
        add(boardPanel);

        updateBoard();

        setVisible(true);
    }

    public void initializeBoard() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                board[i][j] = 0;
            }
        }
    }

    public void updateBoard() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                JPanel cell = new JPanel();
                cell.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
                final int row = i;
                final int col = j;
                cell.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (board[row][col] == 0) {
                            board[row][col] = clientID;

                            // Send the move to the server
                            try {
                                out.writeInt(row);
                                out.writeInt(col);
                                out.writeInt(clientID);
                                out.flush();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }

                            if (clientID == 1) {
                                cell.setBackground(Color.RED);
                            } else {
                                cell.setBackground(Color.BLUE);
                            }
                        }
                    }
                });

                boardPanel.add(cell);
            }
        }
    }

    public void connectServer() {
        try {
            client = new Socket("localhost", 7070);

            in = new DataInputStream(client.getInputStream());
            out = new DataOutputStream(client.getOutputStream());

            clientID = in.readInt();
            System.out.println("You are client #" + clientID + ", you are connected to the server!");
            if (clientID == 1) {
                System.out.println("Waiting for another client to connect...");
            }
        } catch (IOException e) {
            System.out.println("Could not connect to server");
            System.exit(-1);
        }
    }

    private class SyncServer implements Runnable {
        public void run() {
            try {
                while (true) {
                    int row = in.readInt();
                    int col = in.readInt();
                    int clientID = in.readInt();

                    board[row][col] = clientID;

                    SwingUtilities.invokeLater(() -> {
                        Component[] components = boardPanel.getComponents();
                        int index = row * 4 + col;
                        JPanel cell = (JPanel) components[index];
                        cell.setBackground(clientID == 1 ? Color.RED : Color.BLUE);
                    });
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Client client = new Client();
        client.connectServer();
        client.clientGUI(client.clientID);

        new Thread(client.new SyncServer()).start();
    }
}


