package main.shared;

import java.util.ArrayList;
import java.util.List;

// This class if for managing the player state and client to client information
// - What the client knows about the game
// - What the server knows about the client
public class Player {
    private final int clientID;
    
    // The list of pixels that every player is known to have colored
    private final List<int[]> pixelInfoList = new ArrayList<>();

    private int[][] coloredArea;
    private boolean[][][][] coloredPixels;

    public Player(int clientID) {
        this.clientID = clientID;
    }


    // Accessor Functions
    public int getId() {
        return this.clientID;
    }

    public List<int[]> getPixelInfoList() {
        return this.pixelInfoList;
    }

    public int[][] getColoredArea() {
        return this.coloredArea;
    }

    public boolean[][][][] getColoredPixels() {
        return this.coloredPixels;
    }

    // Setting Functions
    public void setColoredPixels(boolean[][][][] coloredPixels) {
        this.coloredPixels = coloredPixels;
    }

    public void setColoredArea(int[][] coloredArea) {
        this.coloredArea = coloredArea;
    }
}
