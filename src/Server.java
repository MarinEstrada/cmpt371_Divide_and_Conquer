import java.io.*;
import java.net.*;

public class Server {
    private ServerSocket server;
    private int numClients;
    private int maxClients;

    private Socket client1;
    private Socket client2;
    private DataInputStream in1;
    private DataInputStream in2;
    private DataOutputStream out1;
    private DataOutputStream out2;

    private int[][] board;

    public void newServer() {
        numClients = 0;
        maxClients = 2;
        board = new int[4][4];

        try {
            server = new ServerSocket(7070);
            System.out.println("Listening on port 7070...");
        } catch (IOException e) {
            System.out.println("Could not listen on port 7070");
            System.exit(-1);
        }
    }

    public void connectClients() {
        try {
            while (numClients < maxClients) {
                Socket client = server.accept();
                numClients++;

                DataInputStream in = new DataInputStream(client.getInputStream());
                DataOutputStream out = new DataOutputStream(client.getOutputStream());
                out.writeInt(numClients);
                System.out.println("Client #" + numClients + " has connected to the server!");

                if (numClients == 1) {
                    client1 = client;
                    in1 = in;
                    out1 = out;
                } else {
                    client2 = client;
                    in2 = in;
                    out2 = out;
                }

                new Thread(new SyncClients(numClients, in)).start();
            }
        } catch (IOException e) {
            System.out.println("Accept failed: 7070");
            System.exit(-1);
        }
    }

    private void broadcastUpdate(int row, int col, int clientID) {
        if (client1 != null && client2 != null) {
            try {
                // board
                board[row][col] = clientID;
                out1.writeInt(row);
                out1.writeInt(col);
                out1.writeInt(clientID);
//                out1.flush();
                out2.writeInt(row);
                out2.writeInt(col);
                out2.writeInt(clientID);
//                out2.flush();

                // Check if there is a winner
                int winner = checkWinner();
                System.out.println("Winner: " + winner);
                out1.writeInt(winner);
                out2.writeInt(winner);

                out1.flush();
                out2.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private int checkWinner() {
        // check if the board is full
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col ++) {
                if (board[row][col] == 0) { // the board is not full, no winner yet
                    return 0;
                }
            }
        }

        // check if there is a winner
        int client1Count = 0;
        int client2Count = 0;
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                if (board[row][col] == 1) {
                    client1Count++;
                } else if (board[row][col] == 2) {
                    client2Count++;
                }
            }
        }

        if (client1Count > client2Count) {
            return 1;
        } else if (client1Count < client2Count) {
            return 2;
        } else { // draw
            return -1;
        }
    }

    private class SyncClients implements Runnable {
        private int clientID;
        private DataInputStream in;

        public SyncClients(int clientID, DataInputStream in) {
            this.clientID = clientID;
            this.in = in;
        }

        public void run() {
            try {
                while (true) {
                    int row = in.readInt();
                    int col = in.readInt();
                    int clientID = in.readInt();

                    broadcastUpdate(row, col, clientID);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.newServer();
        server.connectClients();
    }
}
