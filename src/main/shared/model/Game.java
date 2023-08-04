package main.shared.model;

import java.io.IOException;
import java.io.Serializable;

import javax.swing.JPanel;


// This class is responsible for managing the game state
public class Game implements Serializable{
    private final int MAX_PLAYERS;

    private final Player[] players;
    private GameBoard gameBoard;

    // 0 = no winner
    // 1 = player 1
    // 2 = player 2
    // n = player n
    private int winner;

    // Given a max number of players an grid size, initialize the game
    public Game(int num_cells, int max_players) {
        this.MAX_PLAYERS = max_players;

        this.gameBoard = new GameBoard(num_cells); // Assume this is your game board class
        this.winner = 0;
        this.players = new Player[max_players];
    }
    
    // Accessor Functions
    public Player getPlayer(int playerID) {
        if (playerID < MAX_PLAYERS && playerID >= 0) {
            return players[playerID];
        } else {
            return null;
        }
    }

    // Checks to see whether or not the cell is filled >= threshold
    private void checkThreshold(JPanel cell, int row, int col, int x, int y) {
        int cellWidth = cell.getWidth();
        int cellHeight = cell.getHeight();
        int cellArea = cellWidth * cellHeight;
        int currOwnerID = game.getGameBoard().getCell(row, col).getOwnerID();

        // Check if the cell is filled >= threshold
        if (currOwnerID == 0 || currOwnerID == -1) {
            int isFilled = 0;
            if (clientPlayer.getColoredArea()[row][col] >= cellArea * Settings.COLOR_THRESHOLD) { // if filled
                isFilled = clientID;
            } else { // if not filled, clear cell
                clientPlayer.getColoredArea()[row][col] = 0;

                cell.removeAll();
                cell.revalidate();
                cell.repaint();
                isFilled = -1;
                for (int i = 0; i < cellWidth; i++) { // flush coloredPixels
                    for (int j = 0; j < cellHeight; j++) {
                        clientPlayer.getColoredPixels()[row][col][i][j] = false;
                    }
                }
            }
            clientPlayer.getPixelInfoList().add(new int[]{row, col, 0, x, y, isFilled});
        }
    }

    public GameBoard getGameBoard() {
        return this.gameBoard;
    }

    public int getWinner() {
        return this.winner;
    }

    public int getNumPlayers() {
        return players.length;
    }

    // Setting Functions
    public void setWinner(int winner) {
        this.winner = winner;
    }

    // Game logic
    public boolean isValidMove(int row, int col) {
        return gameBoard.getCell(row, col).isOwned();
    }

    public void addPlayer(Player player) {
        for (int i = 0; i < MAX_PLAYERS; i++) {
            if (players[i] == null) {
                players[i] = player;
                break;
            }
        }
    }

    // Update the game state based on a player's move
    public void makeMove(int row, int col, Player player) throws IOException {
        if (isValidMove(row, col)) {
            gameBoard.setCell(row, col, player.getId());
            
            // Check if the game is over
            if (gameBoard.checkWin(player)) {
                setWinner(player.getId());
            }
        } else {
            throw new IOException(); // Create this exception to handle invalid moves
        }
    }
}
