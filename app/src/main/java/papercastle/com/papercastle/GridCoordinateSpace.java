package papercastle.com.papercastle;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

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
    public void highlightCell(Point p, Canvas canvas, Paint paint) {
        paint.setStrokeWidth(10);
        final int x = screenOrigin.x + p.x * gridSize;
        final int y = screenOrigin.y + p.y * gridSize;
        canvas.drawRect(x, y, x + gridSize, y + gridSize, paint);
    }

    @Override
    public void updateGridSize(final int gridSize) {
        this.gridSize = gridSize;
    }

    @Override
    public int getGridSize() {
        return gridSize;
    }

    @Override
    public List<Point> neighbors(Point p) {
        final List<Point> l = new ArrayList<>(4);
        addIfInBounds(l, p.x - 1, p.y);
        addIfInBounds(l, p.x + 1, p.y);
        addIfInBounds(l, p.x, p.y - 1);
        addIfInBounds(l, p.x, p.y + 1);
        return l;
    }

    @Override
    public int numDirections() {
        return 4;
    }

    private int[][] deltas = new int[][] {{1, 0}, {0, -1}, {-1, 0}, {0, 1}};

    @Override
    public Point getNeighborInDirection(Point p, int direction) {
        return new Point(p.x + deltas[direction][0], p.y + deltas[direction][1]);
    }

    @Override
    public int getDirectionVector(Point a, Point b) {
        final int dx = b.x - a.x;
        final int dy = b.y - a.y;

        int index = -1;
        for (int i = 0; i < 4; i++) {
            if (deltas[i][0] == dx && deltas[i][1] == dy) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            throw new IllegalArgumentException();
        }
        return index;
    }

    private void addIfInBounds(List<Point> l, int x, int y) {
        if (x >= 0 && y >= 0 && x < gridWidth && y < gridHeight) {
            l.add(new Point(x, y));
        }
    }
}
