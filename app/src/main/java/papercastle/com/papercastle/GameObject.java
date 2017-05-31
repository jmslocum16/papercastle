package papercastle.com.papercastle;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Object that has a position and/or a path
 * Created by Josh on 5/26/2017.
 */

public class GameObject {

    //

    private List<Point> path;
    private double pathPos;
    private double speed; // cells per second

    protected final int color;
    protected final int pathColor;

    private Point curScreenPos;

    public GameObject(Point start, double speed, int color) {
        this.path = new ArrayList<>();
        path.add(start);
        pathPos = 0;
        this.speed = speed;
        this.color = color;
        // darker version of color
        this.pathColor = Color.argb(160, Color.red(color) / 2, Color.green(color) / 2, Color.blue(color) / 2);
        curScreenPos = null;
    }

    public void addPointToPath(final Point p) {
        this.path.add(p);
        trimPath();
    }

    public void setPath(final List<Point> newPath) {
        this.path = new ArrayList<>(newPath);
        this.pathPos = 0;
    }

    private void trimPath() {
        // TODO do anything?
    }

    public void update(final long ms) {
        curScreenPos = null; // invalidate cached screen position
        final double fractionOfSec = ms / 1000.0;
        pathPos += fractionOfSec * speed;
        if (pathPos >= path.size() - 1) {
            pathPos = path.size() - 1;
            endOfPath();
        }
    }

    // subclasses can do something interesting here if they want to
    protected void endOfPath() {
        // do nothing
    }

    public Point getCurScreenPos(final CoordinateSpace cs) {
        if (curScreenPos == null) {
            final int pathIndex = (int)pathPos;
            final double pathMod = pathPos - pathIndex;
            final Point a = cs.posToScreen(path.get(pathIndex));
            if (pathIndex == path.size() - 1) {
                curScreenPos = a;
            } else {
                final Point b = cs.posToScreen(path.get(pathIndex + 1));
                curScreenPos =  new Point((int)interpolate(a.x, b.x, pathMod), (int)interpolate(a.y, b.y, pathMod));
            }
        }
        return curScreenPos;
    }

    private double interpolate(int a, int b, double p) {
        return a * (1 - p) + b * p;
    }

    protected void drawPath(final CoordinateSpace cs, final Canvas canvas, final Paint paint) {
        paint.setColor(pathColor);
        paint.setStrokeWidth(5);

        final int pathIndex = (int)pathPos;

        if (pathIndex < path.size() - 1) {
            Point lastScreenPoint = getCurScreenPos(cs);
            for (int i = pathIndex + 1; i < path.size(); i++) {
                final Point nextScreenPoint = cs.posToScreen(path.get(i));
                canvas.drawLine(lastScreenPoint.x, lastScreenPoint.y, nextScreenPoint.x, nextScreenPoint.y, paint);
                lastScreenPoint = nextScreenPoint;
            }
        }
    }

    protected void drawBasic(final CoordinateSpace cs, final Canvas canvas, final Paint paint) {
        final Point screenPos = getCurScreenPos(cs);
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(screenPos.x, screenPos.y, cs.getGridSize() / 4, paint);
    }

    public void draw(final CoordinateSpace cs, final Canvas canvas, final Paint paint) {
        drawPath(cs, canvas, paint);
        drawBasic(cs, canvas, paint);
    }

    public Point getLastPathPoint() {
        return path.get(path.size() - 1);
    }


}
