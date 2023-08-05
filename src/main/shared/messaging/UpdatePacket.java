package main.shared.messaging;

import java.io.Serializable;

public class UpdatePacket implements Serializable {
    // Packet Types
    // 0 = client to server
    // 1 = Cell lock message
    // 2 = game over packet

    private int type = 0;
    private int size;
    private int[] data;

    // If data type is 0
    // size = 6 ints
    // data[0] = cellRow
    // data[1] = cellCol
    // data[2] = clientSource
    // data[3] = currentX pixel
    // data[4] = currentY pixel
    // data[5] = currentIsFilled

    // If data type is 2
    // size = 1 int
    // data[0] = winnerID

    // Once the type and data is set, it cannot be modified again by any clients
    public UpdatePacket(int type, int size, int[] data) {
        this.type = type;
        this.size = size;
        this.data = data;
    }

    // Accessors
    public int getType() {
        return this.type;
    }

    public int getSize() {
        return this.size;
    }

    public int[] getData() {
        return this.data;
    }

    public int getData(int index) {
        return this.data[index];
    }
}