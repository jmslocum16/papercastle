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

    private final CSType csType;
    private final Terrain[][] layout;
    private final int[] cloneTypes;
    // TODO clone type definitions

    Level(CSType csType, Terrain[][] layout, int[] cloneTypes) {
        this.csType = csType;
        this.layout = layout;
        this.cloneTypes = cloneTypes;
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
            }, new int[] {1, 0, 2, 0, 21}),
            new Level(GRID, new Terrain[][] {
                    {NONE, NONE, NONE, NONE, NONE, WALL, NONE, NONE, NONE},
                    {NONE, NONE, NONE, WALL, NONE, WALL, NONE, NONE, NONE},
                    {NONE, NONE, START, WALL, NONE, WALL, WALL, WALL, NONE},
                    {WALL, WALL, WALL, WALL, NONE, WALL, END, NONE, NONE},
                    {NONE, NONE, NONE, NONE, NONE, WALL, WALL, WALL, NONE},
                    {NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE}
            }, new int[] {0, 0, 1})
    };
}
