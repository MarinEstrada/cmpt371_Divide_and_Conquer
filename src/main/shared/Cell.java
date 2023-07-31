package main.shared;

public class Cell {
    
    private final int row;
    private final int col;

    // ownerID = 0 if empty
    // ownerID = 1 if player 1
    // ownerID = 2 if player 2
    public int ownerID;

    public Cell(int row, int col, int ownerID) {
        this.row = row;
        this.col = col;
        this.ownerID = ownerID;
    }

    // For setting the owner of the cell after a player passes threshold
    public void setOwnerID(int ownerID) {
        this.ownerID = ownerID;
    }

    // Accessor Functions
    public int getRow() {
        return this.row;
    }

    public int getCol() {
        return this.col;
    }

    public int getOwnerID() {
        return this.ownerID;
    }

    // Checks to see if the cell has been claimed for stealing purposes
    public boolean isOwned() {
        return ownerID != 0;
    }
}