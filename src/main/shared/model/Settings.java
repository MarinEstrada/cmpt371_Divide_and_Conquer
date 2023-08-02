package main.shared.model;

import java.awt.*;

public class Settings {
    // Common Settings
    public static final int NUM_CELLS = 4; // the number of cells in a row/column

    // Client Settings
    public static final int BOARD_SIZE = 600; // the width/height of the board
    public static final double COLOR_THRESHOLD = 0.3; // the threshold: filled if >= COLOR_THRESHOLD% of the cell is colored
    public static final int BRUSH_SIZE = 10; // the size of the brush
    public static final Color CLIENT1_COLOR = Color.PINK; // the color for client 1
    public static final Color CLIENT2_COLOR = Color.GRAY; // the color for client 2

    // Server Settings
    public static final int MAX_PLAYERS = 2; // the maximum number of clients
}