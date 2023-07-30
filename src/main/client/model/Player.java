package main.client.model;

public class Player {
    private final int clientID;

    public Player(int clientID) {
        this.clientID = clientID;
    }

    public int getId() {
        return this.clientID;
    }
}
