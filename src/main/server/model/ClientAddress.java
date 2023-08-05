package main.server.model;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientAddress {
    private int clientID;
    private final Socket clientSocket;
    private final ObjectInputStream objectIn;
    private ObjectOutputStream objectOut;

    public ClientAddress(int clientID, Socket clientSocket, ObjectInputStream objectIn, ObjectOutputStream objectOut) {
        this.clientID = clientID;
        this.clientSocket = clientSocket;
        this.objectIn = objectIn;
        this.objectOut = objectOut;
    }

    public int getClientID() {
        return this.clientID;
    }

    public Socket getClientSocket() {
        return this.clientSocket;
    }

    public ObjectInputStream getObjectInputStream() {
        return this.objectIn;
    }

    public ObjectOutputStream getObjectOutputStream() {
        return this.objectOut;
    }
}
