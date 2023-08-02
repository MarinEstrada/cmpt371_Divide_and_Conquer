package main.client.controller;

import main.shared.messaging.GamePacket;
import main.shared.messaging.UpdatePacket;
import main.shared.model.*;

import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Client1 extends JFrame {
    private Socket client;
    private int clientID;
    private Player clientPlayer;

    ObjectInputStream in;
    ObjectOutputStream out;

    private JPanel boardPanel;

    private Game game;
    // private int[][] board;
    private int[][] coloredArea;
    private boolean[][][][] coloredPixels;

    private boolean isGameTerminated = false;

    // constants
    private void clientGUI(int clientID) {
        // Initialize the game, board, and players
        game = new Game(Settings.NUM_CELLS, 2); 

        // Initialize the colored area in a cell
        coloredArea = new int[Settings.NUM_CELLS][Settings.NUM_CELLS];
        for (int i = 0; i < Settings.NUM_CELLS; i++) {
            for (int j = 0; j < Settings.NUM_CELLS; j++) {
                coloredArea[i][j] = 0;
            }
        }

        // Initialize the colored pixels in a cell
        coloredPixels = new boolean[Settings.NUM_CELLS][Settings.NUM_CELLS][Settings.BOARD_SIZE][Settings.BOARD_SIZE];

        // Initialize the GUI
        setTitle("Deny and Conquer - Client #" + clientID);
        setSize(Settings.BOARD_SIZE, Settings.BOARD_SIZE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        boardPanel = new JPanel(new GridLayout(Settings.NUM_CELLS, Settings.NUM_CELLS, 2, 2));
        add(boardPanel);
        setLocationRelativeTo(null); // board appears in middle of screen
        setResizable(false);

        updateBoard();

        setVisible(true);
    }

    private void updateBoard() {
        for (int i = 0; i < Settings.NUM_CELLS; i++) {
            for (int j = 0; j < Settings.NUM_CELLS; j++) {
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
        int currOwnerID = game.getGameBoard().getCell(row, col).getOwnerID();

        // Check if the cell is filled >= threshold
        if (currOwnerID == 0 || currOwnerID == -1) {
            int isFilled = 0;
            if (coloredArea[row][col] >= cellArea * Settings.COLOR_THRESHOLD) { // if filled
                isFilled = clientID;
            } else { // if not filled, clear cell
                coloredArea[row][col] = 0;
                cell.removeAll();
                cell.revalidate();
                cell.repaint();
                isFilled = -1;
                for (int i = 0; i < cellWidth; i++) { // flush coloredPixels
                    for (int j = 0; j < cellHeight; j++) {
                        coloredPixels[row][col][i][j] = false;
                    }
                }
            }
            game.getPlayer(clientID).pixelInfoList.add(new int[]{row, col, 0, x, y, isFilled});
        }
    }

    private void paintCell(JPanel cell, int row, int col, int x, int y) {
        int currOwnerID = game.getGameBoard().getCell(row, col).getOwnerID();

        if (currOwnerID == 0 || currOwnerID == -1) {
            int cellWidth = cell.getWidth();
            int cellHeight = cell.getHeight();
            int cellArea = cellWidth * cellHeight;

            Color brushColor = clientID == 1 ? Settings.CLIENT1_COLOR : Settings.CLIENT2_COLOR;

            // Draw on the JPanel (for display purposes)
            Graphics boardImage = cell.getGraphics();
            boardImage.setColor(brushColor);
            boardImage.fillRect(x, y, Settings.BRUSH_SIZE, Settings.BRUSH_SIZE);

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

            // // Check if the cell is filled >= threshold
            int isFilled = 0;
            if (coloredArea[row][col] >= cellArea * Settings.COLOR_THRESHOLD) {
                isFilled = clientID;
            }

            // Add pixel information to the list, to be sent to the server
            game.getPlayer(clientID).getPixelInfoList().add(new int[]{row, col, clientID, x, y, isFilled});
        }
    }

    private void printGameResult(String result) {
        JOptionPane.showMessageDialog(this, result);
        System.exit(0);
    }

    public void connectServer() {
        try {
            client = new Socket("localhost", 7070);

            in = new ObjectInputStream(client.getInputStream());
            out = new ObjectOutputStream(client.getOutputStream());

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
            for (int[] pixelInfo : game.getPlayer(clientID).getPixelInfoList()) {
                UpdatePacket packet = new UpdatePacket(0, pixelInfo);
                packet.getData()[0] = pixelInfo[0];
                packet.getData()[1] = pixelInfo[1];
                packet.getData()[2] = pixelInfo[2];
                packet.getData()[3] = pixelInfo[3];
                packet.getData()[4] = pixelInfo[4];
                packet.getData()[5] = pixelInfo[5];

                out.writeObject(packet);
            }
            out.flush(); // Flush the output stream to ensure all data is sent
            game.getPlayer(clientID).getPixelInfoList().clear(); // Clear the pixelInfoList
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private class SyncServer implements Runnable {
        public void run() {
            try {
                while (true) {
                    // Read the information from the server (Another client sent info and the server is relaying it back to you)
                    int currentRow = in.readInt();
                    int currentCol = in.readInt();
                    int currentClientID = in.readInt();
                    int currentX = in.readInt();
                    int currentY = in.readInt();
                    int currentIsFilled = in.readInt();
                    int winner = in.readInt();
                    
                    // Update the game board
                    game.getGameBoard().setCell(currentRow, currentCol, currentIsFilled);

                    SwingUtilities.invokeLater(() -> {
                        // draw on board/cell
                        JPanel cell = (JPanel) boardPanel.getComponent(currentRow * Settings.NUM_CELLS + currentCol);
                        Graphics boardImage = cell.getGraphics();
                        boardImage.setColor(currentClientID == 1 ? Settings.CLIENT1_COLOR : Settings.CLIENT2_COLOR);
                        boardImage.fillRect(currentX, currentY, Settings.BRUSH_SIZE, Settings.BRUSH_SIZE);

                        
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

                        System.out.println("Current Winner ID: " + winner);

                        // announce winner
                        if (winner >= -1 && winner != 0 && winner <= 2 && !isGameTerminated) {
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
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Client1 client = new Client1();
        client.connectServer();
        client.clientGUI(client.clientID);

        new Thread(client.new SyncServer()).start();

        // Periodically send pixelInfoList to server
        Timer timer = new Timer(50, e -> {
            client.sendPixelInfoListToServer();
        });
        timer.start();
    }
}