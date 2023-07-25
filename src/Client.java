import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Client extends JFrame {
    private Socket client;
    private int clientID;
    DataInputStream in;
    DataOutputStream out;
    private final List<int[]> pixelInfoList = new ArrayList<>();

    private JPanel boardPanel;
    private int[][] board;
    private int[][] coloredArea;
    private boolean[][][][] coloredPixels;

    // constants
    private static final int NUM_CELLS = 4; // the number of cells in a row/column
    private static final int BOARD_SIZE = 400; // the width/height of the board
    private static final double COLOR_THRESHOLD = 0.3; // the threshold: filled if >= COLOR_THRESHOLD% of the cell is colored
    private static final int BRUSH_SIZE = 10; // the size of the brush
    private static final Color CLIENT1_COLOR = Color.PINK; // the color for client 1
    private static final Color CLIENT2_COLOR = Color.GRAY; // the color for client 2

    // Add a button to clear/reset the board
    private JButton clearButton;

    private void clientGUI(int clientID) {
        // Initialize the board
        board = new int[NUM_CELLS][NUM_CELLS];
        for (int i = 0; i < NUM_CELLS; i++) {
            for (int j = 0; j < NUM_CELLS; j++) {
                board[i][j] = 0;
            }
        }

        // Initialize the colored area in a cell
        coloredArea = new int[NUM_CELLS][NUM_CELLS];
        for (int i = 0; i < NUM_CELLS; i++) {
            for (int j = 0; j < NUM_CELLS; j++) {
                coloredArea[i][j] = 0;
            }
        }

        // Initialize the colored pixels in a cell
        coloredPixels = new boolean[NUM_CELLS][NUM_CELLS][BOARD_SIZE][BOARD_SIZE];

        // Initialize the GUI
        setTitle("Deny and Conquer - Client #" + clientID);
        setSize(BOARD_SIZE, BOARD_SIZE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        boardPanel = new JPanel(new GridLayout(NUM_CELLS, NUM_CELLS, 2, 2));
        add(boardPanel);
    
        // Add a clear button to the GUI
        clearButton = new JButton("Clear Board");
        clearButton.addActionListener(e -> {
            clearBoard();
            clearBoardPanel();
            updateBoard();
        });
        add(clearButton, BorderLayout.SOUTH);

        updateBoard();

        setVisible(true);
    }

    private void updateBoard() {
        for (int i = 0; i < NUM_CELLS; i++) {
            for (int j = 0; j < NUM_CELLS; j++) {
                // Create a JPanel for each cell
                JPanel cell = new JPanel();
                cell.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
                final int row = i;
                final int col = j;

                // Add a MouseListener to the JPanel (for the first click)
                cell.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        paintCell(cell, row, col, e.getX(), e.getY());
                    }
                });

                // Add a MouseMotionListener to the JPanel (for dragging)
                cell.addMouseMotionListener(new MouseMotionAdapter() {
                    @Override
                    public void mouseDragged(MouseEvent e) {
                        paintCell(cell, row, col, e.getX(), e.getY());
                    }
                });

                // Add the JPanel to the board
                boardPanel.add(cell);
            }
        }
    }

        private void clearBoard() {
        for (int i = 0; i < NUM_CELLS; i++) {
            for (int j = 0; j < NUM_CELLS; j++) {
                board[i][j] = 0;
                coloredArea[i][j] = 0;
                for (int x = 0; x < BOARD_SIZE; x++) {
                    for (int y = 0; y < BOARD_SIZE; y++) {
                        coloredPixels[i][j][x][y] = false;
                    }
                }
            }
        }
    }

     private void clearBoardPanel() {
        boardPanel.removeAll();
        boardPanel.revalidate();
        boardPanel.repaint();
    }


    private void paintCell(JPanel cell, int row, int col, int x, int y) {
        if (board[row][col] == 0 || board[row][col] == clientID) {
            int cellWidth = cell.getWidth();
            int cellHeight = cell.getHeight();
            int cellArea = cellWidth * cellHeight;

            Color brushColor = clientID == 1 ? CLIENT1_COLOR : CLIENT2_COLOR;

            // Draw on the JPanel (for display purposes)
            Graphics boardImage = cell.getGraphics();
            boardImage.setColor(brushColor);
            boardImage.fillRect(x, y, BRUSH_SIZE, BRUSH_SIZE);

            // Create a BufferedImage to store the drawing (for tracking purposes)
            BufferedImage virtualImage = new BufferedImage(cellWidth, cellHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D virtualImageCanvas = virtualImage.createGraphics();
            cell.paint(virtualImageCanvas);
            virtualImageCanvas.setColor(brushColor);
            virtualImageCanvas.fillRect(x, y, cellWidth / 10, cellHeight / 10);

            // Check if the pixel is already colored, if not, color it
            for (int i = 0; i < cellWidth; i++) {
                for (int j = 0; j < cellHeight; j++) {
                    if (!coloredPixels[row][col][i][j]) {
                        if (virtualImage.getRGB(i, j) == brushColor.getRGB()) {
                            coloredPixels[row][col][i][j] = true;
                            coloredArea[row][col]++;
                        }
                    }
                }
            }

            System.out.println("coloredArea: " + coloredArea[row][col] + ", cellArea: " + cellArea);

            // Check if the cell is filled >= threshold
            int isFilled = 0;
            if (coloredArea[row][col] >= cellArea * COLOR_THRESHOLD) {
                isFilled = clientID;
            }

            // Add pixel information to the list, to be sent to the server
            pixelInfoList.add(new int[]{row, col, clientID, x, y, isFilled});
        }
    }

    private void printGameResult(String result) {
        JOptionPane.showMessageDialog(this, result);
        System.exit(0);
    }

    public void connectServer() {
        try {
            client = new Socket("localhost", 7070);

            in = new DataInputStream(client.getInputStream());
            out = new DataOutputStream(client.getOutputStream());

            clientID = in.readInt();
            System.out.println("You are client #" + clientID + ", you are connected to the server!");
            if (clientID == 1) {
                System.out.println("Waiting for another client to connect...");
            }
        } catch (IOException e) {
            System.out.println("Could not connect to server");
            System.exit(-1);
        }
    }

    private void sendPixelInfoListToServer() {
        try {
            // Send each pixel information to the server
            for (int[] pixelInfo : pixelInfoList) {
                out.writeInt(pixelInfo[0]); // row
                out.writeInt(pixelInfo[1]); // col
                out.writeInt(pixelInfo[2]); // clientID
                out.writeInt(pixelInfo[3]); // x
                out.writeInt(pixelInfo[4]); // y
                out.writeInt(pixelInfo[5]); // isFilled
            }
            out.flush(); // Flush the output stream to ensure all data is sent
            pixelInfoList.clear(); // Clear the pixelInfoList
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private class SyncServer implements Runnable {
        public void run() {
            try {
                while (true) {
                    int currentRow = in.readInt();
                    int currentCol = in.readInt();
                    int currentClientID = in.readInt();
                    int currentX = in.readInt();
                    int currentY = in.readInt();
                    int currentIsFilled = in.readInt();
                    int winner = in.readInt();

                    board[currentRow][currentCol] = currentClientID;

                    SwingUtilities.invokeLater(() -> {
                        // draw on board/cell
                        JPanel cell = (JPanel) boardPanel.getComponent(currentRow * NUM_CELLS + currentCol);
                        Graphics boardImage = cell.getGraphics();
                        boardImage.setColor(currentClientID == 1 ? CLIENT1_COLOR : CLIENT2_COLOR);
                        boardImage.fillRect(currentX, currentY, BRUSH_SIZE, BRUSH_SIZE);

                        // fill cell if passed threshold
                        if (currentIsFilled != 0) {
                            boardImage.fillRect(0, 0, cell.getWidth(), cell.getHeight());
                        }

                        // announce winner
                        if (winner != 0) {
                            if (winner == clientID) {
                                printGameResult("You win!");
                            } else if (winner == -1) {
                                printGameResult("Draw!");
                            } else {
                                printGameResult("You lose!");
                            }
                        }
                    });
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Client client = new Client();
        client.connectServer();
        client.clientGUI(client.clientID);

        new Thread(client.new SyncServer()).start();

        // Periodically send pixelInfoList to server
        Timer timer = new Timer(100, e -> {
            client.sendPixelInfoListToServer();
        });
        timer.start();
    }
}