package main.server.controller;

import main.shared.messaging.*;
import main.shared.model.Game;
import main.shared.model.Settings;

import java.io.*;
import java.net.*;

// This class is for controlling the sockets and connections between the server and clients
public class Server1 {
    private ServerSocket server;

    private Socket[] clients;
    private Socket client1;
    private Socket client2;

    private ObjectOutputStream[] objectOutputStreams;
    private ObjectOutputStream objectOut1;
    private ObjectOutputStream objectOut2;
    
    private Game game;
    private int numClients;

    public void newServer() {
        numClients = 0;
        game = new Game(Settings.NUM_CELLS, Settings.MAX_PLAYERS);
        clients = new Socket[Settings.MAX_PLAYERS];
        objectOutputStreams = new ObjectOutputStream[Settings.MAX_PLAYERS];

        try {
            server = new ServerSocket(7070);
            System.out.println("Listening on port 7070...");
        } catch (IOException e) {
            System.out.println("Could not listen on port 7070");
            System.exit(-1);
        }
    }

    public void connectClients() {
        // Connects the clients to the server and starts a new thread for each client, supports reconnection
        try {
            while(true) {
                Socket client = server.accept();
                numClients++;
                
                ObjectInputStream objectIn = new ObjectInputStream(client.getInputStream());
                ObjectOutputStream objectOut = new ObjectOutputStream(client.getOutputStream());

                // Give the client the current game state and the client's ID
                objectOut.writeObject(game);

                // Tell the client what their ID is
                System.out.println("Client #" + numClients + " has connected to the server!");

                new Thread(new SyncClients(numClients, objectIn)).start();
            }
        } catch (IOException e) {
            System.out.println("Accept failed: 7070");
            System.exit(-1);
        }
    }

    private void broadcastUpdate(UpdatePacket packet) {
        if (client1 != null && client2 != null) {
            try {
                // Sending the information received from a single client to all clients
                for (int i = 0; i < Settings.MAX_PLAYERS; i++) {
                    objectOutputStreams[i].writeObject(packet);
                }

                // Check if there is a winner
                int winner = checkWinner();
                int[] winnerInfo = {winner};
                UpdatePacket winnerPacket = new UpdatePacket(2, 1, winnerInfo);
                for (int i = 0; i < Settings.MAX_PLAYERS; i++) {
                    objectOutputStreams[i].writeObject(winnerPacket);
                }

                // Flush the output streams
                for (int i = 0; i < Settings.MAX_PLAYERS; i++) {
                    objectOutputStreams[i].flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
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
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.newServer();
        server.connectClients();
    }
}