package main.server.controller;

import main.shared.messaging.*;
import main.shared.model.*;
import main.server.model.*;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

// This class is for controlling the sockets and connections between the server and clients
public class Server1 {
    private ServerSocket serverSocket;

    private ArrayList<ClientAddress> clients = new ArrayList<ClientAddress>();
    
    private Game game;
    private int numClients;

    // Game initialization checklist
    // 1. Create the game and gameboard. The gameboard is created inside of the game
    // 2. Create a server socket that listens on port 7070 that clients can connect to
    // 3. Accept connection from client
    // 4. Store the player's clientID, socket, and input/output streams inside of a special server player
    //     used to identify which socket belongs to which player
    // 5. Create the player only giving it the clientID and the gameboard size
    // 6. Send the initial game state to the client
    // 7. The client will then initialize itself as a player storing the game state and it's socket
    // access to the server inside its player object information
    // 8. Start a thread to receive information from the client and send it to all clients

    public void initServer() {
        // Step 1
        numClients = 0;
        game = new Game(Settings.NUM_CELLS, Settings.MAX_PLAYERS);

        try {
            // Step 2
            // Creates a server socket that listens on port 7070 that clients can connect to
            serverSocket = new ServerSocket(7070);
            System.out.println("Server listening on port 7070...");

            while (true) {
                // Step 3
                // Creates an individual socket for communication between the server and an individual client
                Socket clientSocket = serverSocket.accept();

                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());

                // Step 4
                // Create streams for communication with the client
                // Need to save this one because it is used to send information to the client
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                // This one is stored inside of SyncClients and is used to receive information from the client
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                ClientAddress clientAddress = new ClientAddress(numClients, clientSocket, in, out);
                clients.add(numClients, clientAddress);

                // Step 5
                // The player is created and added to the game
                // More initialization needs to happen when the game is received on the client side
                Player player = new Player(numClients, Settings.NUM_CELLS, Settings.BOARD_SIZE);
                game.addPlayer(player);

                GamePacket initialGameState = new GamePacket(numClients, game);
                out.writeObject(initialGameState);

                new Thread(new SyncClients(clientAddress)).start();
                numClients++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeSocket();
        }
    }

    private void broadcastUpdate(UpdatePacket packet) {
        try {
            // Sending the information received from a single client to all clients
            // We don't want to send the information back to the client that sent it
            for (int i = 0; i < Settings.MAX_PLAYERS; i++) {
                if (clients.get(i).getClientID() != packet.getData(2)) {
                    clients.get(i).getObjectOutputStream().writeObject(packet);
                }
            }

            // Check if there is a winner
            int winner = game.checkWinner();
            int[] winnerInfo = {winner};
            UpdatePacket winnerPacket = new UpdatePacket(2, 1, winnerInfo);
            for (int i = 0; i < Settings.MAX_PLAYERS; i++) {
                clients.get(i).getObjectOutputStream().writeObject(winnerPacket);
            }

            // Flush the output streams
            for (int i = 0; i < Settings.MAX_PLAYERS; i++) {
                clients.get(i).getObjectOutputStream().flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeSocket() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Linear search of the clients
    private ClientAddress getClientAddress(int clientID) {
        for (int i = 0; i < Settings.MAX_PLAYERS; i++) {
            if (clients.get(i).getClientID() == clientID) {
                return clients.get(i);
            }
        }
        return null;
    }

    private class SyncClients implements Runnable {
        private ClientAddress clientAddress;

        public SyncClients(ClientAddress clientAddress) {
            this.clientAddress = clientAddress;
        }

        // Game update checklist
        // 1. Receive the information from the client
        // 2. Update the server's tracked version of the game state
        // 3. Send it out to all clients immediately

        public void run() {
            try {
                while (true) {
                    // Receive the information from the client
                    UpdatePacket packet = (UpdatePacket) clientAddress.getObjectInputStream().readObject();
                    
                    // This is pixel information from the sendPixelInfoListToServer function
                    // Update the server's tracked game state
                    int row = packet.getData(0);
                    int col = packet.getData(1);
                    int newOwnerID = packet.getData(2);
                    int currentX = packet.getData(3);
                    int currentY = packet.getData(4);
                    int currentIsFilled = packet.getData(5);

                    // Update the gameboard to reflect new ownership
                    game.getGameBoard().setCell(row, col, newOwnerID);

                    // Update the corresponding player's coloredPixels 4D array (The pixels that the player has colored)
                    // Update the corresponding player's coloredArea 2D array (An array showing which cells it owns)
                    // Update the corresponding player's numFilledCells (increment by 1)
                    game.getPlayer(newOwnerID).updateColoredPixels(row, col, currentX, currentY, currentIsFilled);
                    game.getPlayer(newOwnerID).updateColoredArea(row, col, currentIsFilled);
                    game.getPlayer(newOwnerID).incNumFilledCells();

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
    }
}