package main.shared.model;

import java.io.Serializable;

// This class is responsible for managing the game board/all the cells collectively
public class GameBoard implements Serializable {
    private int NUM_CELLS;
    private Cell[][] board;
    
    public GameBoard(int numCells) {
        this.NUM_CELLS = numCells;

        // Initialize the game board
        board = new Cell[NUM_CELLS][NUM_CELLS];
        for (int row = 0; row < NUM_CELLS; row++) {
            for (int col = 0; col < NUM_CELLS; col++) {
                board[row][col] = new Cell(row, col, -1);
            }
        }
    }

    public boolean checkWin(Player player) {
        // Check if the player has won
        int playerCount = 0;
        for (int row = 0; row < NUM_CELLS; row++) {
            for (int col = 0; col < NUM_CELLS; col++) {
                if (board[row][col].getOwnerID() == player.getId()) {
                    playerCount++;
                }
            }
        }

        return playerCount >= NUM_CELLS;
    }

    // Accessor Functions
    public int getCellValue() {
        return 0;
    }

    public Cell getCell(int row, int col) {
        return board[row][col];
    }

    // Setting Functions
    // if the cell is owned, return the currently owned player's ownerID, else return the ownerID passed in
    public int setCell(int row, int col, int ownerID) {
        if (board[row][col].isOwned()) {
            return board[row][col].getOwnerID();
        } else {
            board[row][col].setOwnerID(ownerID);
            return ownerID;
        }
    }
}
