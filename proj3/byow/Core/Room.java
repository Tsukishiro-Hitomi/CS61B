package byow.Core;

public class Room {
    /* X, Y 用于记录房间左下角的位置 */
    private final int x;
    private final int y;
    private final int width;
    private final int height;

    Room(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int visitX() {
        return this.x;
    }

    public int visitY() {
        return this.y;
    }

    public int visitWidth() {
        return this.width;
    }

    public int visitHeight() {
        return this.height;
    }

    public boolean isOverlapped(Room other) {
        /* 无重叠的要求是不接触 */
        /* 因此将 other 的外周扩大一圈 */
        int otherX = other.visitX() - 1;
        int otherY = other.visitY() - 1;
        int otherWidth = other.visitWidth() + 2;
        int otherHeight = other.visitHeight() + 2;

        if (x + width <= otherX || y + height <= otherY) {
            return false;
        }

        if (otherX + otherWidth <= x || otherY + otherHeight <= y) {
            return false;
        }

        return true;
    }

    public Position getCentralPoint() {
        return new Position(x + width / 2, y + height / 2);
    }
}
