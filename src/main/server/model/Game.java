package main.server.model;

public class Game {
    public static final int MAX_PLAYERS = 2;
    private final Player[] players;
    private int[][] gameBoard;

    // 0 = no winner
    // 1 = player 1
    // 2 = player 2
    // n = player n
    private int winner;

    private static final int NUM_CELLS = 4; // the number of cells in a row/column

    public Game(Player player1, Player player2) {
        this.gameBoard = new int[NUM_CELLS][NUM_CELLS]; // Assume this is your game board class
        this.winner = 0;
    }

    public Player getPlayer(int playerID) {
        if (playerID < MAX_PLAYERS && playerID >= 0) {
            return players[playerID];
        } else {
            return NULL;
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

    public Player getCurrentPlayer() {
        return this.currentPlayer;
    }

    public void switchTurns() {
        if (currentPlayer.equals(player1)) {
            currentPlayer = player2;
        } else {
            currentPlayer = player1;
        }
    }
    
    // Update the game state based on a player's move
    public void makeMove(int row, int col, Player player) {
        if (currentPlayer.equals(player) && gameBoard.isValidMove(row, col)) {
            gameBoard.setCell(row, col, player.getId());
            switchTurns();
            
            // Check if the game is over
            if (gameBoard.checkWin(player)) {
                setWinner(player.getId());
            }
        } else {
            throw new InvalidMoveException(); // Create this exception to handle invalid moves
        }
    }
}
