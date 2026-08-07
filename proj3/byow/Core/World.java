package byow.Core;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import byow.TileEngine.TERenderer;
import static byow.Core.RandomUtils.uniform;

import java.util.Random;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.ArrayDeque;

public class World {
    private TETile[][] tiles;
    private Position avatar;

    /* World 的大小 */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 30;
    private final Random random;

    /* 房间的最大/小尺寸，实际尺寸为区间内的随机值 */
    private static final int MIN_ROOM = 4;
    private static final int MAX_ROOM = 10;
    private static final int MAX_ATTEMPTS = 50;

    public World(long seed) {
        random = new Random(seed);
        tiles = new TETile[WIDTH][HEIGHT];
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                tiles[i][j] = Tileset.NOTHING;
            }
        }
        ArrayList<Room> rooms = generateRooms();
        for (int i = 0; i < rooms.size() - 1; i++) {
            Position from = rooms.get(i).getCentralPoint();
            Position to = rooms.get(i + 1).getCentralPoint();
            carveHallway(from, to);
        }
        addWalls();
        avatar = firstFloor();
        tiles[avatar.getX()][avatar.getY()] = Tileset.AVATAR;
    }

    public TETile[][] getTiles() {
        return this.tiles;
    }

    private void fillRectangle(int x, int y, int width, int height, TETile t) {
        for (int i = x; i < x + width; i++) {
            for (int j = y; j < y + height; j++) {
                tiles[i][j] = t;
            }
        }
    }

    private void carveHallway(Position from, Position to) {
        int fromX = from.getX();
        int fromY = from.getY();
        int toX = to.getX();
        int toY = to.getY();
        /* L 型走廊，先横后竖 */
        fillRectangle(Math.min(fromX, toX), fromY, Math.abs(fromX - toX) + 1, 1, Tileset.FLOOR);
        fillRectangle(toX, Math.min(fromY, toY), 1, Math.abs(fromY - toY) + 1, Tileset.FLOOR);
    }

    private void addWalls() {
        /* 遍历附近的 8 个位置，如果发现 FLOOR 则改为 WALL */
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                if (!tiles[i][j].equals(Tileset.NOTHING)) {
                    continue;
                }

                if (i - 1 >= 0) {
                    if (tiles[i - 1][j].equals(Tileset.FLOOR)) {
                        tiles[i][j] = Tileset.WALL;
                    }
                    if (j - 1 >= 0 && tiles[i - 1][j - 1].equals(Tileset.FLOOR)) {
                        tiles[i][j] = Tileset.WALL;
                    }
                    if (j + 1 < HEIGHT && tiles[i - 1][j + 1].equals(Tileset.FLOOR)) {
                        tiles[i][j] = Tileset.WALL;
                    }
                }

                if (i + 1 < WIDTH) {
                    if (tiles[i + 1][j].equals(Tileset.FLOOR)) {
                        tiles[i][j] = Tileset.WALL;
                    }
                    if (j - 1 >= 0 && tiles[i + 1][j - 1].equals(Tileset.FLOOR)) {
                        tiles[i][j] = Tileset.WALL;
                    }
                    if (j + 1 < HEIGHT && tiles[i + 1][j + 1].equals(Tileset.FLOOR)) {
                        tiles[i][j] = Tileset.WALL;
                    }                   
                }

                if (j - 1 >= 0 && tiles[i][j - 1].equals(Tileset.FLOOR)) {
                    tiles[i][j] = Tileset.WALL;
                }
                
                if (j + 1 < HEIGHT && tiles[i][j + 1].equals(Tileset.FLOOR)) {
                    tiles[i][j] = Tileset.WALL;
                }
            }
        }
    }

    private ArrayList<Room> generateRooms() {
        ArrayList<Room> result = new ArrayList<Room>();

        for (int i = 0; i < MAX_ATTEMPTS; i++) {

            int w = uniform(random, MIN_ROOM, MAX_ROOM + 1);
            int h = uniform(random, MIN_ROOM, MAX_ROOM + 1);
            int x = uniform(random, 1, WIDTH - w);
            int y = uniform(random, 1, HEIGHT - h);

            Room newRoom = new Room(x, y, w, h);
            boolean isAdded = true;
            for (Room r : result) {
                if (r.isOverlapped(newRoom)) {
                    isAdded = false;
                    break;
                }
            }

            if (isAdded) {
                result.add(newRoom);
                /* 铺地板 */
                fillRectangle(x, y, w, h, Tileset.FLOOR);
            }
        }
        return result;
    }

    private int allFloors() {
        int ans = 0;
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                if (isWalkable(i, j)) {
                    ans += 1;
                }
            }
        }
        return ans;
    }

    /* 返回第一个找到的 FLOOR 格，世界里没有地板时返回 null */
    private Position firstFloor() {
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                if (tiles[i][j].equals(Tileset.FLOOR)) {
                    return new Position(i, j);
                }
            }
        }
        return null;
    }

    private boolean isWalkable(int x, int y) {
        return tiles[x][y].equals(Tileset.AVATAR) || tiles[x][y].equals(Tileset.FLOOR);
    }

    /* 从任意一个 FLOOR 格出发，观察其是否可以走到所有 FLOOR 格，由此验证图是全连通的 */
    private boolean isFullyConnected() {
        int floorNums = allFloors();
        Position start = firstFloor();
        if (start == null) {
            return true;
        }

        int count = 0;
        HashSet<Position> seen = new HashSet<Position>();
        ArrayDeque<Position> record = new ArrayDeque<Position>();
        record.addFirst(start);

        while (!record.isEmpty()) {
            int size = record.size();
            for (int i = 0; i < size; i++) {
                Position currentPosition = record.removeFirst();
                if (seen.contains(currentPosition)) {
                    continue;
                }
                seen.add(currentPosition);
                count += 1;

                int x = currentPosition.getX();
                int y = currentPosition.getY();

                if (x - 1 >= 0 && isWalkable(x - 1, y)) {
                    record.addLast(new Position(x - 1, y));
                }

                if (y - 1 >= 0 && isWalkable(x, y - 1)) {
                    record.addLast(new Position(x, y - 1));
                }

                if (x + 1 < WIDTH && isWalkable(x + 1, y)) {
                    record.addLast(new Position(x + 1, y));
                }
                
                if (y + 1 < HEIGHT && isWalkable(x, y + 1)) {
                    record.addLast(new Position(x, y + 1));
                }
            }
        }

        return count == floorNums;
    }

    /* 检查墙补完整了没有：任何一个 FLOOR 格的 8 邻域里都不允许出现 NOTHING。
       邻居越界也算不合格，说明地板贴到了世界边缘，外面没地方放墙 */
    private boolean wallsAreComplete() {
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                if (!tiles[i][j].equals(Tileset.FLOOR)) {
                    continue;
                }
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        int nx = i + dx;
                        int ny = j + dy;
                        if (nx < 0 || nx >= WIDTH || ny < 0 || ny >= HEIGHT) {
                            return false;
                        }
                        if (tiles[nx][ny].equals(Tileset.NOTHING)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /* 临时测试入口，后删 */
    public static void main(String[] args) {
        long seed = Long.parseLong(args[0]);
        World w = new World(seed);

        /* 带第二个参数时只输出文本，不开 StdDraw 窗口，方便做 diff */
        if (args.length > 1) {
            System.out.println(TETile.toString(w.getTiles()));
            System.out.println("connected=" + w.isFullyConnected()
                    + " walls=" + w.wallsAreComplete()
                    + " floors=" + w.allFloors());
            return;
        }

        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);
        ter.renderFrame(w.getTiles());
    }
}
