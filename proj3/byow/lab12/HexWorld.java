package byow.lab12;
import org.junit.Test;
import static org.junit.Assert.*;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Random;

/**
 * Draws a world consisting of hexagonal regions.
 */
public class HexWorld {
    private static final long SEED = 28733;
    private static final Random RANDOM = new Random(SEED);

    public static void addHexagon(TETile[][] world, int size, int x, int y, TETile t) {
        for (int i = 1; i <= 2 * size; i++) {
            int offset = hexRowOffset(size, i);
            int width = hexRowWidth(size, i);
            addRow(world, t, x + offset, y + i - 1, width);
        }
    }

    /* 从 (x, y) 开始填写 rowWidth 格 */
    private static void addRow(TETile[][] world, TETile t, int x, int y, int rowWidth) {
        for (int i = 0; i < rowWidth; i++) {
            world[x + i][y] = t;
        }
    }

    /* 从 (x, y) 开始在竖直方向上堆 num 个六边形 */
    private static void addColumn(TETile[][] world, int x, int y, int size, int num) {
        for (int i = 0; i < num; i++) {
            addHexagon(world, size, x, y + 2 * size * i, randomTile());
        }
    }

    private static TETile randomTile() {
        int tileNum = RANDOM.nextInt(3);
        switch (tileNum) {
            case 0: return Tileset.WALL;
            case 1: return Tileset.FLOWER;
            case 2: return Tileset.FLOOR;
            default: return Tileset.NOTHING;
        }
    }

    /* 从 (x, y) 开始密集堆叠 19 个六边形 */
    public static void addTesselation(TETile[][] world, int x, int y, int size) {
        addColumn(world, x, y, size, 3);
        addColumn(world, x + (2 * size - 1), y - size, size, 4);
        addColumn(world, x + 2 * (2 * size - 1), y - 2 * size, size, 5);
        addColumn(world, x + 3 * (2 * size - 1), y - size, size, 4);
        addColumn(world, x + 4 * (2 * size - 1), y, size, 3);
    }

    /* 计算六边形第 i 行 的宽度 */
    private static int hexRowWidth(int size, int i) {
        if (i <= size) {
            return size + 2 * (i - 1);
        } else {
            return size + 2 * (2 * size - i);
        }
    }

    /* 计算六边形第 i 行左边空的格数 */
    private static int hexRowOffset(int size, int i) {
        if (i <= size) {
            return size - i;
        } else {
            return i - size - 1;
        }
    }

    /* 临时主函数，用于测试 */
    public static void main(String[] args) {
        int size = Integer.parseInt(args[0]);
        TERenderer ter = new TERenderer();
        ter.initialize(50, 50);
        TETile[][] world = new TETile[50][50];
        for (int i = 0; i < 50; i++) {
            for (int j = 0; j < 50; j++) {
                world[i][j] = Tileset.NOTHING;
            }
        }
        addTesselation(world, 10, 10, size);
        ter.renderFrame(world);
    }
}
