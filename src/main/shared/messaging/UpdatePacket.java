package main.shared.messaging;

import java.io.Serializable;

public class UpdatePacket implements Serializable {
    // Packet Types
    // 0 = client to server
    // 1 = server to client
    // 2 = game over packet

    int type = 0;
    int size;
    int[] data;

    public UpdatePacket(int type, int[] data) {
        this.type = type;
        if (type == 0) { // Client -> Server or Server -> Client
            // data[0] = row
            // data[1] = col
            // data[2] = ClientID (Who own's it)
            // data[3] = x
            // data[4] = y
            // data[5] = isFilled
            size = 6;
        } else if (type == 1) { // Cell lock message
            // data[0] = row
            // data[1] = col
            // data[2] = ClientID (Who own's it)
            size = 3;
        } else if (type == 2) { // Game over message
            // int winner = data[0];
            size = 1;
        } else {
            System.out.println("Invalid packet type or parameters given");
            size = 0;
        }


        this.data = new int[size];
    }

    public int getType() {
        return this.type;
    }

    public int getSize() {
        return this.size;
    }

    public int[] getData() {
        return this.data;
    }
}
