package papercastle.com.papercastle;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import java.util.List;

/**
 * Created by Josh on 5/26/2017.
 */

public interface CoordinateSpace {

    // returns the screen coordinate of the center of point p
    public Point posToScreen(Point p);

    // returns the point represented by screen coordinate p
    public Point screenToPos(Point p);

    public int distance (Point a, Point b);

    public void draw(Canvas canvas, Paint paint);

    public void highlightCell(Point p, Canvas canvas, Paint paint);

    public void updateGridSize(int gridSize);

    public int getGridSize();

    public List<Point> neighbors(Point p);

    public int numDirections();

    public Point getNeighborInDirection(Point p, int direction);

    public int getDirectionVector(Point a, Point b);
}
