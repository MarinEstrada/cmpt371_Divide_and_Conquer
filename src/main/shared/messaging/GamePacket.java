package main.shared.messaging;

import main.shared.model.Game;
import java.io.Serializable;

public class GamePacket implements Serializable {
    
    private final Game game;
    private final int clientID;

    // For sending the ENTIRE gameboard to the client for the first time and telling it what client number it is
    public GamePacket(int clientID, Game game) {
        this.game = game;
        this.clientID = clientID;
    }

    public Game getGame() {
        return this.game;
    }

    public int getClientID() {
        return this.clientID;
    }
}