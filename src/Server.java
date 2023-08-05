import java.io.*;
import java.net.*;

public class Server {
    private ServerSocket server;

    private Socket client1;
    private Socket client2;
    private DataOutputStream out1;
    private DataOutputStream out2;

    private int numClients; // the number of clients connected to the server
    private static final int MAX_CLIENTS = 2; // the maximum number of clients

    public void newServer() {
        numClients = 0; // no clients connected yet

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
            while (numClients < MAX_CLIENTS) {
                // accept the client connection
                Socket client = server.accept();

                // increment the number of clients
                numClients++;

                // create the input and output streams
                DataOutputStream out = new DataOutputStream(client.getOutputStream());
                DataInputStream in = new DataInputStream(client.getInputStream());

                // send the client number to the client
                out.writeInt(numClients);
                System.out.println("Client #" + numClients + " has connected to the server!");

                if (numClients == 1) { // first client
                    client1 = client;
                    out1 = out;
                } else { // second client
                    client2 = client;
                    out2 = out;

                    // send the game start message to both clients: initialize the start screen
                    sendGameStartMessage();
                }

                // create a new thread to handle the client
                new Thread(new SyncClients(in)).start();
            }
        } catch (IOException e) {
            System.out.println("Accept failed: 7070");
            System.exit(-1);
        }
    }

    // send the game start message to both clients: initialize the start screen
    private void sendGameStartMessage() {
        if (client1 != null && client2 != null) {
            try {
                out1.writeUTF("start");
                out2.writeUTF("start");

                out1.flush();
                out2.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // broadcast the update to both clients
    private void broadcastUpdate(String str) {
        if (client1 != null && client2 != null) {
            try {
                out1.writeUTF(str); // send the update to client 1
                out2.writeUTF(str); // send the update to client 2

                out1.flush(); // flush the output stream
                out2.flush(); // flush the output stream
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // a thread to handle the client
    private class SyncClients implements Runnable {
        private DataInputStream in;

        public SyncClients(DataInputStream in) {
            this.in = in;
        }

        public void run() {
            try {
                while (true) {
                    // receive the string
                    String str = in.readUTF();
                    // if string is empty
                    if (!str.equals("")) {
                        System.out.println("Received: " + str);
                        broadcastUpdate(str);
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    // close the server
    private void closeServer() {
        try {
            if (server != null) {
                out1.close();
                out2.close();
                client1.close();
                client2.close();
                server.close();
                System.out.println("Server closed.");
            }
        } catch (IOException e) {
            System.out.println("Could not close server.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server(); // create a new server
        server.newServer(); // start the server
        server.connectClients(); // connect the clients

        // close the server
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.closeServer();
        }));
    }
}
