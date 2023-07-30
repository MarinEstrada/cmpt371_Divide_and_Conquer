package main.server.model;

public class Player {
    private final int clientID;

    public Player(int clientID, String color) {
        this.clientID = clientID;
    }

    public int getId() {
        return this.clientID;
    }
}
