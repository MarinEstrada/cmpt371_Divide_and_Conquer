package main.client.model;

import java.io.IOException;

public class Game {
    public static final int MAX_PLAYERS = 2;
    private final Player[] players;
    private GameBoard gameBoard;

    // 0 = no winner
    // 1 = player 1
    // 2 = player 2
    // n = player n
    private int winner;

    private static final int NUM_CELLS = 4; // the number of cells in a row/column

    // Given no parameters, initialize the game with default max players = 2 and default grid size = 4
    public Game() {
        this.gameBoard = new GameBoard(NUM_CELLS); // Assume this is your game board class
        this.winner = 0;
        this.players = new Player[MAX_PLAYERS];
    }

    // Given a grid size, initialize the game with default max players = 2
    public Game(int num_cells) {
        this.gameBoard = new GameBoard(num_cells); // Assume this is your game board class
        this.winner = 0;
        this.players = new Player[MAX_PLAYERS];
    }

    // Given a max number of players an grid size, initialize the game
    public Game(int num_cells, int max_players) {
        this.gameBoard = new GameBoard(num_cells); // Assume this is your game board class
        this.winner = 0;
        this.players = new Player[max_players];
    }

    public Player getPlayer(int playerID) {
        if (playerID < MAX_PLAYERS && playerID >= 0) {
            return players[playerID];
        } else {
            return null;
        }
    }

    public GameBoard getGameBoard() {
        return this.gameBoard;
    }

    public int getWinner() {
        return this.winner;
    }

    public void setWinner(int winner) {
        this.winner = winner;
    }

    public boolean isValidMove(int row, int col) {
        return gameBoard.getCell(row, col).isOwned();
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
