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
    private DataOutputStream out;
    private DataInputStream in;
    
    private final List<int[]> pixelInfoList = new ArrayList<>();
    private JPanel boardPanel;
    private JFrame startScreen;
    private int[][] board;
    private int[][] boardCurrentStatus;
    private int[][] coloredArea;
    private boolean[][][][] coloredPixels;

    private boolean isGameStarted = false;
    private boolean isGameTerminated = false;
    int winner = 0;

    // constants
    private static final int NUM_CELLS = 4; // the number of cells in a row/column
    private static final int BOARD_SIZE = 600; // the width/height of the board
    private static final double COLOR_THRESHOLD = 0.3; // the threshold: filled if >= COLOR_THRESHOLD% of the cell is colored
    private static final int BRUSH_SIZE = 10; // the size of the brush
    private static final Color CLIENT1_COLOR = Color.PINK; // the color for client 1
    private static final Color CLIENT2_COLOR = Color.GRAY; // the color for client 2

    private void showStartScreen() {
        startScreen = new JFrame();
        startScreen.setTitle("Waiting for Player #2");
        startScreen.setSize(BOARD_SIZE, BOARD_SIZE);
        startScreen.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        startScreen.setLocationRelativeTo(null);
        startScreen.setResizable(false);

        JLabel label = new JLabel("You are Player #1, waiting for Player #2...");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        startScreen.add(label);

        startScreen.setVisible(true);
    }

    private void clientGUI(int clientID) {
        // Initialize the board
        board = new int[NUM_CELLS][NUM_CELLS];
        for (int i = 0; i < NUM_CELLS; i++) {
            for (int j = 0; j < NUM_CELLS; j++) {
                board[i][j] = 0;
            }
        }

        // Initialize the current status of the board
        boardCurrentStatus = new int[NUM_CELLS][NUM_CELLS];
        for (int i = 0; i < NUM_CELLS; i++) {
            for (int j = 0; j < NUM_CELLS; j++) {
                boardCurrentStatus[i][j] = 0;
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
        setLocationRelativeTo(null); // board appears in middle of screen
        setResizable(false);

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

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        checkThreshold(cell, row, col, e.getX(), e.getY());
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

    private void checkThreshold(JPanel cell, int row, int col, int x, int y) {
        int cellWidth = cell.getWidth();
        int cellHeight = cell.getHeight();
        int cellArea = cellWidth * cellHeight;

        // Check if the cell is filled >= threshold
        if ((board[row][col] == 0 || board[row][col] == -1) && (boardCurrentStatus[row][col] == clientID || boardCurrentStatus[row][col] == 0)) {
            int isFilled = 0;
            int belongsTo = clientID;
            if (coloredArea[row][col] >= cellArea * COLOR_THRESHOLD) { // if filled
                isFilled = clientID;
            } else { // if not filled, clear cell
                coloredArea[row][col] = 0;
                cell.removeAll();
                cell.revalidate();
                cell.repaint();
                isFilled = -1;
                belongsTo = 0;
                for (int i = 0; i < cellWidth; i++) { // flush coloredPixels
                    for (int j = 0; j < cellHeight; j++) {
                        coloredPixels[row][col][i][j] = false;
                    }
                }
            }
            pixelInfoList.add(new int[]{row, col, belongsTo, x, y, isFilled});
        }
    }

    private Color setBrushColor(){
        if(clientID == 1) return CLIENT1_COLOR;
        else if(clientID == 2) return CLIENT2_COLOR;

        //if no match, there is an error. Return black
        System.out.println("CLIENT NOT RECOGNIZED");
        return Color.BLACK;
    }

    private void paintCell(JPanel cell, int row, int col, int x, int y) {
        if ((board[row][col] == 0 || board[row][col] == -1) && (boardCurrentStatus[row][col] == clientID || boardCurrentStatus[row][col] == 0)) {
            // System.out.println("boardCurrentStatus[" + row + "][" + col + "] = " + boardCurrentStatus[row][col]);
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

            // System.out.println("coloredArea: " + coloredArea[row][col] + ", cellArea: " + cellArea);

            // // Check if the cell is filled >= threshold
            int isFilled = 0;

            // Add pixel information to the list, to be sent to the server
            pixelInfoList.add(new int[]{row, col, clientID, x, y, isFilled});
        }
    }

    private void printGameResult(String result) {
        JOptionPane.showMessageDialog(this, result);
        System.exit(0);
    }

    private int checkWinner() {
        // check if the board is full
        for (int row = 0; row < NUM_CELLS; row++) {
            for (int col = 0; col < NUM_CELLS; col++) {
                if (board[row][col] == 0 || board[row][col] == -1) { // the board is not full, no winner yet
                    return 0;
                }
            }
        }

        // check if there is a winner
        int client1Count = 0;
        int client2Count = 0;
        for (int row = 0; row < NUM_CELLS; row++) {
            for (int col = 0; col < NUM_CELLS; col++) {
                if (board[row][col] == 1) {
                    client1Count++;
                } else if (board[row][col] == 2) {
                    client2Count++;
                }
            }
        }

        if (client1Count > client2Count) {
            return 1;
        } else if (client1Count < client2Count) {
            return 2;
        } else { // draw
            return -1;
        }
    }

    public void connectServer() {
        try {
            client = new Socket("localhost", 7070);

            out = new DataOutputStream(client.getOutputStream());
            in = new DataInputStream(client.getInputStream());
            
            clientID = in.readInt();
            System.out.println("You are client #" + clientID + ", you are connected to the server!");
            if (clientID == 1) {
                System.out.println("Waiting for another client to connect...");
                showStartScreen();
            }
        } catch (IOException e) {
            System.out.println("Could not connect to server");
            System.exit(-1);
        }
    }

    private void sendPixelInfoListToServer() {
        try {
            // save each pixel info to a string
            StringBuilder tokenizedMessage = new StringBuilder();
            // Send each pixel information to the server
            for (int[] pixelInfo : pixelInfoList) {
                tokenizedMessage.append(pixelInfo[0]).append(";"); // row
                tokenizedMessage.append(pixelInfo[1]).append(";"); // col
                tokenizedMessage.append(pixelInfo[2]).append(";"); // clientID
                tokenizedMessage.append(pixelInfo[3]).append(";"); // x
                tokenizedMessage.append(pixelInfo[4]).append(";"); // y
                tokenizedMessage.append(pixelInfo[5]).append("#"); // isFilled
            }

            // send the string to server
            out.writeUTF(tokenizedMessage.toString());
            
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
                    // read the string from server
                    String tokenizedMessage = in.readUTF();

                    // if the game is not started and the message is "start", start the game
                    if (tokenizedMessage.equals("start") && !isGameStarted) {
                        if (startScreen != null) {
                            startScreen.dispose();
                        }
                        clientGUI(clientID);
                        isGameStarted = true;
                    } 
                    
                    // if the game is started, read the tokenized message
                    else {
                        // tokenize the string
                        String[] tokens = tokenizedMessage.split("#");
                        String[] lastToken = tokens[tokens.length - 1].split(";");

                        // read the token if it is valid
                        if (lastToken.length == 6) {
                            // System.out.println("lastToken: " + lastToken[0] + ", " + lastToken[1] + ", " + lastToken[2] + ", " + lastToken[3] + ", " + lastToken[4] + ", " + lastToken[5]);
                            int currentRow = Integer.parseInt(lastToken[0]);
                            int currentCol = Integer.parseInt(lastToken[1]);
                            int currentClientID = Integer.parseInt(lastToken[2]);
                            int currentX = Integer.parseInt(lastToken[3]);
                            int currentY = Integer.parseInt(lastToken[4]);
                            int currentIsFilled = Integer.parseInt(lastToken[5]);

                            System.out.println("currentClientID: " + currentClientID + " is drawing on " + currentRow + ", " + currentCol + " at " + currentX + ", " + currentY + " with isFilled: " + currentIsFilled);

                            board[currentRow][currentCol] = currentIsFilled;
                            boardCurrentStatus[currentRow][currentCol] = currentClientID;
                            winner = checkWinner();

                            SwingUtilities.invokeLater(() -> {
                                // draw on board/cell
                                JPanel cell = (JPanel) boardPanel.getComponent(currentRow * NUM_CELLS + currentCol);
                                Graphics boardImage = cell.getGraphics();
                                boardImage.setColor(currentClientID == 1 ? CLIENT1_COLOR : CLIENT2_COLOR);
                                boardImage.fillRect(currentX, currentY, BRUSH_SIZE, BRUSH_SIZE);


                                // if player released before threshold, clear cell
                                if (currentIsFilled == -1) {
                                    System.out.println("SYNC Removing drawnlines");
                                    cell.removeAll();
                                    cell.revalidate();
                                    cell.repaint();
                                }

                                // fill cell if passed threshold
                                if (currentIsFilled != 0 && currentIsFilled != -1) {
                                    boardImage.fillRect(0, 0, cell.getWidth(), cell.getHeight());
                                }

                                // announce winner
                                if ((winner == -1 || winner == 1 || winner == 2) && !isGameTerminated) {
                                    isGameTerminated = true;
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
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void closeSocket() {
        try {
            if (client != null) {
                System.out.println("Closing client socket...");
                client.close();
                in.close();
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        Client client = new Client();
        client.connectServer();

        new Thread(client.new SyncServer()).start();

        // Periodically send pixelInfoList to server
        Timer timer = new Timer(10, e -> {
            client.sendPixelInfoListToServer();
        });
        timer.start();

        // close connection
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            client.closeSocket();
        }));
    }
}
