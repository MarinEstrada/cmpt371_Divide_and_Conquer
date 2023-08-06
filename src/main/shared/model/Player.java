package main.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.*;

// This class if for managing the player state and client to client information
// - What the client knows about the game
// - What the server knows about the client
public class Player implements Serializable {
    
    private final int clientID;
    
    // Networking variables
    private Socket clientSocket;
    private ObjectInputStream objectIn;
    private ObjectOutputStream objectOut;

    // The list of pixels that every player is known to have colored
    private final List<int[]> pixelInfoList;

    // The area that the player has colored
    private int[][] coloredArea;
    private int numFilledCells;

    // The pixels that the player has colored
    // [Cell row][Cell col][Pixel X][Pixel Y]
    private boolean[][][][] coloredPixels;

    public Player(int clientID, int num_cells, int BOARD_SIZE) {
        this.clientID = clientID;
        this.pixelInfoList = new ArrayList<>();

        // Initialize the colored area in a cell
        coloredArea = new int[Settings.NUM_CELLS][Settings.NUM_CELLS];
        for (int i = 0; i < Settings.NUM_CELLS; i++) {
            for (int j = 0; j < Settings.NUM_CELLS; j++) {
                coloredArea[i][j] = 0;
            }
        }

        // Initialize the colored pixels in a cell
        coloredPixels = new boolean[Settings.NUM_CELLS][Settings.NUM_CELLS][BOARD_SIZE][BOARD_SIZE];
    }

    // *********************
    // Accessor Functions
    // *********************

    // The player's ID
    public int getId() {
        return this.clientID;
    }

    public int getNumFilledCells() {
        for (int row = 0; row < this.coloredArea.length; row++) {
            for (int col = 0; col < this.coloredArea.length; col++) {
                if (this.coloredArea[row][col] != 0) {
                    this.numFilledCells++;
                }
            }
        }
        return this.numFilledCells;
    }

    // The socket or connection that the server uses to communicate with the client
    public Socket getServerAccessSocket() {
        return this.clientSocket;
    }

    // The communication line that the server sends messages to the client on
    public ObjectInputStream getObjectInputStream() {
        return this.objectIn;
    }

    // The communication line that the client sends messages to the server on
    public ObjectOutputStream getObjectOutputStream() {
        return this.objectOut;
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

    // *********************
    // Accessor Functions
    // *********************

    // *********************
    // Setting Functions
    // *********************

    public void setColoredPixels(boolean[][][][] coloredPixels) {
        this.coloredPixels = coloredPixels;
    }

    public void updateColoredPixels(int row, int col, int currentX, int currentY, int currentIsFilled) {
        this.coloredPixels[row][col][currentX][currentY] = currentIsFilled == 1;
    }

    public void updateColoredArea(int row, int col, int currentIsFilled) {
        this.coloredArea[row][col] = currentIsFilled;
    }

    public void setColoredArea(int[][] coloredArea) {
        this.coloredArea = coloredArea;
    }

    public void incNumFilledCells() {
        this.numFilledCells++;
    }

    public void setServerAccessSocket(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void setObjectInputStream(ObjectInputStream objectIn) {
        this.objectIn = objectIn;
    }

    public void setObjectOutputStream(ObjectOutputStream objectOut) {
        this.objectOut = objectOut;
    }

    // *********************
    // Setting Functions
    // *********************
}
