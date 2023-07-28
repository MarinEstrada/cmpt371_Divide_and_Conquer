package main.server.model;

public class Player {
    private final int id;
    private final String color;

    public Player(int id, String color) {
        this.id = id;
        this.color = color;
    }

    public int getId() {
        return this.id;
    }

    public String getColor() {
        return this.color;
    }
}
