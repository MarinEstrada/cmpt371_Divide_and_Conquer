package main.server.model;

public class Player {
    private final int clientID;
    private final String color;

    public Player(int clientID, String color) {
        this.clientID = clientID;
        this.color = color;
    }

    public int getId() {
        return this.clientID;
    }

    public String getColor() {
        return this.color;
    }
}
