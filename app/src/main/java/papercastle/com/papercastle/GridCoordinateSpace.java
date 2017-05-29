package papercastle.com.papercastle;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

/**
 * A grid coordinate space
 * Created by Josh on 5/26/2017.
 */

public class GridCoordinateSpace implements CoordinateSpace {

    private final Point screenOrigin;
    private int gridSize;
    private final int gridWidth;
    private final int gridHeight;

    public GridCoordinateSpace(Point screenOrigin, int gridSize, int gridWidth, int gridHeight) {
        this.screenOrigin = screenOrigin;
        this.gridSize = gridSize;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
    }

    @Override
    public Point posToScreen(Point p) {
        return new Point(pTS(p.x, screenOrigin.x), pTS(p.y, screenOrigin.y));
    }

    // helper for posToScreen that does 1 dimension
    private int pTS(int pos, int origin) {
        return origin + (pos * gridSize) + (gridSize / 2);
    }

    @Override
    public Point screenToPos(Point p) {
        return new Point(sTP(p.x, screenOrigin.x), sTP(p.y, screenOrigin.y));
    }

    // helper for screenToPos that does 1 dimension
    private int sTP(int pos, int origin) {
        return (pos - origin) / gridSize;
    }

    @Override
    public int distance(Point a, Point b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        paint.setARGB(255, 0, 0, 0);
        paint.setStrokeWidth(3);
        for (int x = 0; x <= gridWidth; x++) {
            canvas.drawLine(x * gridSize, screenOrigin.y, x * gridSize, screenOrigin.y + gridHeight * gridSize, paint);
        }
        for (int y = 0; y <= gridHeight; y++) {
            canvas.drawLine(screenOrigin.x, y * gridSize, screenOrigin.x + gridWidth * gridSize, y * gridSize, paint);
        }
    }

    @Override
    public void updateGridSize(final int gridSize) {
        this.gridSize = gridSize;
    }

    @Override
    public int getGridSize() {
        return gridSize;
    }
}
