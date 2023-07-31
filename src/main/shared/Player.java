package main.shared;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private final int clientID;
    
    // The list of pixels that every player is known to have colored
    private final List<int[]> pixelInfoList = new ArrayList<>();

    private int[][] coloredArea;
    private boolean[][][][] coloredPixels;

    public Player(int clientID) {
        this.clientID = clientID;
    }

    public int getId() {
        return this.clientID;
    }
}
