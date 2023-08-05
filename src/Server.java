import java.io.*;
import java.net.*;

public class Server {
    private ServerSocket server;

    private Socket client1;
    private Socket client2;
    private DataOutputStream out1;
    private DataOutputStream out2;

    private int numClients;
    private static final int MAX_CLIENTS = 2; // the maximum number of clients

    public void newServer() {
        numClients = 0;

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
                Socket client = server.accept();
                numClients++;

                DataOutputStream out = new DataOutputStream(client.getOutputStream());
                DataInputStream in = new DataInputStream(client.getInputStream());

                out.writeInt(numClients);
                System.out.println("Client #" + numClients + " has connected to the server!");

                if (numClients == 1) {
                    client1 = client;
                    out1 = out;
                } else {
                    client2 = client;
                    out2 = out;

                    sendGameStartMessage();
                }

                new Thread(new SyncClients(in)).start();
            }
        } catch (IOException e) {
            System.out.println("Accept failed: 7070");
            System.exit(-1);
        }
    }

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

    private void broadcastUpdate(String str) {
        if (client1 != null && client2 != null) {
            try {
                //write back
                out1.writeUTF(str);
                out2.writeUTF(str);

                out1.flush();
                out2.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

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
        Server server = new Server();
        server.newServer();
        server.connectClients();

        // close the server
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.closeServer();
        }));
    }
}
