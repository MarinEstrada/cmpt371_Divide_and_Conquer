package main.server.controller;

import main.shared.messaging.*;
import main.shared.model.*;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

// This class is for controlling the sockets and connections between the server and clients
public class Server1 {
    private ServerSocket serverSocket;

    private ArrayList<ObjectOutputStream> outputStreams = new ArrayList<ObjectOutputStream>();
    
    private Game game;
    private int numClients;

    public void initServer() {
        numClients = 0;
        game = new Game(Settings.NUM_CELLS, Settings.MAX_PLAYERS);

        try {
            // Creates a server socket that listens on port 7070 that clients can connect to
            ServerSocket serverSocket = new ServerSocket(7070);
            System.out.println("Server listening on port 7070...");

            while (true) {
                // Creates an individual socket for communication between the server and an individual client
                Socket clientSocket = serverSocket.accept();
                numClients++;

                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());

                // Create streams for communication with the client
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());

                Player player = new Player(numClients - 1, Settings.NUM_CELLS, Settings.BOARD_SIZE);
                game.addPlayer(player);

                GamePacket initialGameState = new GamePacket(numClients - 1, game);
                out.writeObject(initialGameState);

                outputStreams.add(out);

                new Thread(new SyncClients(numClients, in)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void broadcastUpdate(UpdatePacket packet) {
        try {
            // Sending the information received from a single client to all clients
            for (int i = 0; i < Settings.MAX_PLAYERS; i++) {
                outputStreams.get(i).writeObject(packet);
            }

            // Check if there is a winner
            int winner = checkWinner();
            int[] winnerInfo = {winner};
            UpdatePacket winnerPacket = new UpdatePacket(2, 1, winnerInfo);
            for (int i = 0; i < Settings.MAX_PLAYERS; i++) {
                outputStreams.get(i).writeObject(winnerPacket);
            }

            // Flush the output streams
            for (int i = 0; i < Settings.MAX_PLAYERS; i++) {
                outputStreams.get(i).flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int checkWinner() {
        // check if the board is full
        for (int row = 0; row < Settings.NUM_CELLS; row++) {
            for (int col = 0; col < Settings.NUM_CELLS; col++) {
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
        for (int row = 0; row < Settings.NUM_CELLS; row++) {
            for (int col = 0; col < Settings.NUM_CELLS; col++) {
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

    private void closeSocket() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class SyncClients implements Runnable {
        private ObjectInputStream in;

        public SyncClients(int clientID, ObjectInputStream in) {
            this.in = in;
        }

        public void run() {
            try {
                while (true) {
                    // Receive the information from the client
                    UpdatePacket packet = (UpdatePacket) in.readObject();
                    
                    // Send it back out to all clients immediately
                    broadcastUpdate(packet);
                }
            } catch (IOException ex) {
                System.out.println("IO exception");
                ex.printStackTrace();
            } catch (ClassNotFoundException ex) {
                System.out.println("Class not found exception");
                ex.printStackTrace();
            } finally {
                
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Server1 server = new Server1();
        server.initServer();
        server.closeSocket();
    }
}