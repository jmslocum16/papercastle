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
    // TODO clone type definitions

    Level(CSType csType, Terrain[][] layout) {
        this.csType = csType;
        this.layout = layout;
    }

    public CSType getCsType() {
        return csType;
    }

    public Terrain[][] getLayout() {
        return layout;
    }

    public static final Level[] ALL_LEVELS = new Level[] {
            new Level(GRID, new Terrain[][] {
                    {START, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE},
                    {NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE},
                    {NONE, NONE, END, WALL, NONE, NONE, NONE, NONE, NONE},
                    {NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE},
                    {NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE},
                    {NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE, NONE}
            })
    };
}
