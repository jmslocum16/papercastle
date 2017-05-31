package papercastle.com.papercastle;

import android.graphics.Point;

import static papercastle.com.papercastle.Level.CSType.GRID;
import static papercastle.com.papercastle.Level.Terrain.END;
import static papercastle.com.papercastle.Level.Terrain.NONE;
import static papercastle.com.papercastle.Level.Terrain.START;
import static papercastle.com.papercastle.Level.Terrain.WALL;

/**
 * Created by Josh on 5/26/2017.
 */

public class Level {

    public enum CSType {
        GRID, HEX
    }

    enum Terrain {
        NONE, WALL, START, END
    }

    public static boolean isPassable(Terrain[][] terrain, int x, int y) {
        return x >= 0 && y >= 0 && y < terrain.length && x < terrain[y].length && terrain[y][x] == NONE;
    }

    private final CSType csType;
    private final Terrain[][] layout;
    private final int[] cloneTypes;
    private final GuardObject.GuardFactory[] guards;
    // TODO clone type definitions

    Level(CSType csType, Terrain[][] layout, int[] cloneTypes, GuardObject.GuardFactory[] guards) {
        this.csType = csType;
        this.layout = layout;
        this.cloneTypes = cloneTypes;
        this.guards = guards;
    }

    public CSType getCsType() {
        return csType;
    }

    public Terrain[][] getLayout() {
        return layout;
    }

    public int[] getCloneTypes() {
        return cloneTypes;
    }

    public GuardObject.GuardFactory[] getGuards() {
        return guards;
    }

    public static final int WIDTH = 9;
    public static final int HEIGHT = 6;

    public static final Level[] ALL_LEVELS = new Level[] {
            new Level(GRID, new Terrain[][] {
                    {START, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE},
                    {NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE},
                    {NONE, NONE, END, WALL, NONE, NONE, NONE, NONE, NONE},
                    {NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE},
                    {NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE},
                    {NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE}
            }, new int[] {1, 0, 2}
            ,new GuardObject.GuardFactory[] {
                    new RotatingGuardObject.RotatingGuardFactory(new Point(1, 3), 4, new int[] {0, 1, 2, 3}),
                    new RotatingGuardObject.RotatingGuardFactory(new Point(5, 5), 2, new int[] {0, 2})
            }),
            new Level(GRID, new Terrain[][] {
                    {NONE, NONE, NONE, NONE, NONE, WALL, NONE, NONE, NONE},
                    {NONE, NONE, NONE, WALL, NONE, WALL, NONE, NONE, NONE},
                    {NONE, NONE, START, WALL, NONE, WALL, WALL, WALL, NONE},
                    {WALL, WALL, WALL, WALL, NONE, WALL, END, NONE, NONE},
                    {NONE, NONE, NONE, NONE, NONE, WALL, WALL, WALL, NONE},
                    {NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE}
            }, new int[] {20}
            , new GuardObject.GuardFactory[] {
                    new GuardObject.StationaryGuardFactory(new Point(0, 5), 4, 0),
                    new GuardObject.StationaryGuardFactory(new Point(0, 2), 5, 1),
                    new GuardObject.StationaryGuardFactory(new Point(8, 0), 1, 2),
                    new GuardObject.StationaryGuardFactory(new Point(6, 0), 4, 3)
            } )
    };
}
