package papercastle.com.papercastle;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Stationary Guard
 * Created by Josh on 5/30/2017.
 */

public class GuardObject extends GameObject {

    // TODO just implement as subclasses
    /*enum GuardType {
        STATIONARY, ROTATING, PATROLLING
    }*/

    protected static final int CELEBRATE_MS = 2500; // the amount of time they celebrate after catching you before returning to work

    private final int LOS; // line of sight
    private int curLos;
    protected int dir;

    private int celebrateMs;

    public GuardObject(Point start, int los, int startDir) {
        super(start, 0.0, Color.argb(255, 100, 100, 100));
        this.LOS = los;
        this.curLos = los;
        this.dir = startDir;
        celebrateMs = CELEBRATE_MS;
    }

    public boolean isCelebrating() {
        return celebrateMs < CELEBRATE_MS;
    }

    public void startCelebrating() {
        celebrateMs = 0;
    }

    @Override
    public void update(long ms) {
        super.update(ms);
        // calculate celebrating
        if (isCelebrating()) {
            celebrateMs += ms;
        }
    }

    // TODO subclass only in patrolling
    @Override
    public void endOfPath() {

    }

    public List<Point> getPointsInLOS(final CoordinateSpace cs) {
        List<Point> points = new ArrayList<>();
        Point cur = cs.screenToPos(getCurScreenPos(cs));
        points.add(cur);
        for (int i = 0; i < curLos; i++) {
            cur = cs.getNeighborInDirection(cur, dir);
            points.add(cur);
        }
        return points;
    }

    public void computeLOS(final CoordinateSpace cs, final Level.Terrain[][] level) {
        curLos = 0;
        Point cur = cs.screenToPos(getCurScreenPos(cs));
        while (curLos < LOS) {
            Point neighbor = cs.getNeighborInDirection(cur, dir);
            if (!Level.isPassable(level, neighbor.x, neighbor.y)) {
                break;
            } else {
                curLos++;
            }
            cur = neighbor;
        }
    }

    // TODO how to compute how much it goes to draw?
    protected void drawLOS(final CoordinateSpace cs, final Canvas canvas, final Paint paint) {
        if (curLos <= 0) return;

        final Point p = getCurScreenPos(cs);

        if (isCelebrating()) {
            paint.setARGB(160, 50, 255, 50);
        } else {
            paint.setARGB(160, 255, 50, 50);
        }
        paint.setStyle(Paint.Style.FILL);

        final Point pPos = cs.screenToPos(p);
        Point neighbor = cs.getNeighborInDirection(pPos, dir);

        final Point pPosScreen = cs.posToScreen(pPos);
        final Point neighborScreen = cs.posToScreen(neighbor);

        Point endPoint = new Point(p.x + (curLos * (neighborScreen.x - pPosScreen.x)),
                p.y + (curLos * (neighborScreen.y - pPosScreen.y)));

        int endDx = Math.abs(neighborScreen.y - pPosScreen.y) / 4;
        int endDy = Math.abs(neighborScreen.x - pPosScreen.x) / 4;

        final Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(endPoint.x - endDx, endPoint.y - endDy);
        path.lineTo(endPoint.x + endDx, endPoint.y + endDy);
        path.lineTo(p.x, p.y);
        path.close();
        canvas.drawPath(path, paint);
    }

    @Override
    public void draw(final CoordinateSpace cs, final Canvas canvas, final Paint paint) {
        final Point p = getCurScreenPos(cs);

        // draw LOS
        drawLOS(cs, canvas, paint);

        super.drawBasic(cs, canvas, paint);
        // draw a gold circle in the middle of basic
        paint.setColor(Color.argb(255, 255, 0xd7, 0));
        canvas.drawCircle(p.x, p.y, cs.getGridSize() / 12, paint);
    }

    public interface GuardFactory {
        GuardObject create(CoordinateSpace cs);
    }

    public static class StationaryGuardFactory implements GuardFactory {

        private final Point p;
        private final int los;
        private final int dir;

        public StationaryGuardFactory(Point p, int los, int dir) {
            this.p = p;
            this.los = los;
            this.dir = dir;
        }

        @Override
        public GuardObject create(final CoordinateSpace cs) {
            return new GuardObject(p, los, dir);
        }
    }


}
