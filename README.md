# Divide and Conquer Game
A collaborative project between Jiadi Luo, Adrian Marin Estrada, Jay Esquivel, Kayla Lee, and Harpreet Dubb for our CMPT 371 course: Data Communications and Networking.

The project, written in Java, uses sockets to allow communication between the clients and a server over a TCP connection. The GUI is implemented using the Java Swing library.

### Game Description 
Divide and Conquer is a real-time multiplayer game featuring an interactive nxn grid board (by default 4x4) where players compete to competitively conquer lockable cells via colouring. The objective of the players is to fill as many cells as possible with their respective colours (Client 1 - Pink and Client 2 - Gray) while preventing the opponent from claiming more cells. The player with the most filled cells at the end wins. 

Each of the 16 cells on the board can be coloured by players using their mouse through a graphical user interface. The game follows a client-server model, with two clients connected to a server. Each client controls in a different colour and competes by colouring the cells on the board. While a client is colouring a particular cell, the cell will be locked and other players will be unable to draw within that same cell. The clients use a simple GUI to draw on the grid, and the server updates the game state based on their actions. 

In order to conquer a free box/cell, a player must click-and-hold inside the box while scribbling in it. Upon releasing the click button, the game calculates the percentage of the area that is coloured. If the coloured area is more than the set threshold, the player successfully takes over the box, and the entire box changes its colour to the colour of the conquering player. Once conquered, no other player can take over that box anymore. 

However, if the coloured area is less than the threshold and the player’s attempt to take over the box fails the game immediately clears the box, changing its appearance back to pure white and making the box available for other players to conquer. A player may fail to take over the box by either releasing the click button before colouring more than the threshold amount, or by drawing outside of the box boundaries. Should a player draw outside of the boundaries of the box, then the capturing of the box fails regardless of how much of the box has been coloured.
