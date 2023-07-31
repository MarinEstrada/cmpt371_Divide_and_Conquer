package main.server.controller;

import main.shared.Settings;
import java.io.*;
import java.net.*;

import main.shared.Game;

// This class is for controlling the sockets and connections between the server and clients
public class Server1 {
    private ServerSocket server;

    private Socket[] clients;
    private Socket client1;
    private Socket client2;

    private DataOutputStream[] outputStreams;
    private DataOutputStream out1;
    private DataOutputStream out2;
    
    private Game game;
    private int numClients;

    private static final int NUM_CELLS = 4; // the number of cells in a row/column
    private static final int MAX_CLIENTS = 2; // the maximum number of clients

    public void newServer() {
        numClients = 0;
        game = new Game(NUM_CELLS, MAX_CLIENTS);
        clients = new Socket[MAX_CLIENTS];

        try {
            server = new ServerSocket(7070);
            System.out.println("Listening on port 7070...");
        } catch (IOException e) {
            System.out.println("Could not listen on port 7070");
            System.exit(-1);
        }
    }

    public void connectClientsNew() {
        // Connects the clients to the server and starts a new thread for each client, supports reconnection
        try {
            while(true) {
                Socket client = server.accept();
                numClients++;

                // Setup and in and output stream connection to the client
                DataInputStream in = new DataInputStream(client.getInputStream());
                DataOutputStream out = new DataOutputStream(client.getOutputStream());
                
                // Tell the client what their ID is
                out.writeInt(numClients);
                System.out.println("Client #" + numClients + " has connected to the server!");

                new Thread(new SyncClients(numClients, in)).start();
            }
        } catch (IOException e) {
            System.out.println("Accept failed: 7070");
            System.exit(-1);
        }
    }

    public void connectClients() {
        try {
            while (numClients < MAX_CLIENTS) {
                // Blocking wait for a client to connect
                Socket client = server.accept();
                numClients++;
                
                // Setup and in and output stream connection to the client
                DataInputStream in = new DataInputStream(client.getInputStream());
                DataOutputStream out = new DataOutputStream(client.getOutputStream());
                
                // Tell the client what their ID is
                out.writeInt(numClients);
                System.out.println("Client #" + numClients + " has connected to the server!");

                if (numClients == 1) {
                    client1 = client;
                    out1 = out;
                } else {
                    client2 = client;
                    out2 = out;
                }

                // Start a new thread to handle the client's synced connection with all clients and the server
                new Thread(new SyncClients(numClients, in)).start();
            }
        } catch (IOException e) {
            System.out.println("Accept failed: 7070");
            System.exit(-1);
        }
    }

    private void broadcastUpdate(int row, int col, int clientID, int x, int y, int isFilled) {
        if (client1 != null && client2 != null) {
            try {
                // board
                game.getGameBoard().setCell(row, col, isFilled);

                // Sending the information received from a single client to all clients
                out1.writeInt(row);
                out1.writeInt(col);
                out1.writeInt(clientID);
                out1.writeInt(x);
                out1.writeInt(y);
                out1.writeInt(isFilled);

                out2.writeInt(row);
                out2.writeInt(col);
                out2.writeInt(clientID);
                out2.writeInt(x);
                out2.writeInt(y);
                out2.writeInt(isFilled);

                // Check if there is a winner
                int winner = checkWinner();
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
        for (int row = 0; row < NUM_CELLS; row++) {
            for (int col = 0; col < NUM_CELLS; col++) {
                int currOwnerID = game.getGameBoard().getCell(row, col).getOwnerID();
                if (currOwnerID == 0 || currOwnerID == -1) { 
                    // the board is not full, no winner yet
                    return 0;
                }
            }
        }

        // count up all of client 1's cells and client 2's cells
        int client1Count = 0;
        int client2Count = 0;
        for (int row = 0; row < NUM_CELLS; row++) {
            for (int col = 0; col < NUM_CELLS; col++) {
                int currOwnerID = game.getGameBoard().getCell(row, col).getOwnerID();
                if (currOwnerID == 1) {
                    client1Count++;
                } else if (currOwnerID == 2) {
                    client2Count++;
                }
            }
        }

        // If client1 has more cells than client2, client1 wins
        if (client1Count > client2Count) {
            return 1;
        } else if (client1Count < client2Count) {
            return 2;
        } else { // draw
            return -1;
        }
    }

    private class SyncClients implements Runnable {
        private DataInputStream in;

        public SyncClients(int clientID, DataInputStream in) {
            this.in = in;
        }

        public void run() {
            try {
                while (true) {
                    // Read the information from the client
                    int row = in.readInt();
                    int col = in.readInt();
                    int clientID = in.readInt();
                    int x = in.readInt();
                    int y = in.readInt();
                    int isFilled = in.readInt();
                    
                    // Broadcast the information to all clients
                    broadcastUpdate(row, col, clientID, x, y, isFilled);
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