package papercastle.com.papercastle;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Josh on 5/30/2017.
 */

public class PatrollingGuardObject extends GuardObject {

    private final List<Point> patrol;
    private final boolean restart;

    public PatrollingGuardObject(Point[] patrol, int los, boolean restart) {
        super(patrol[0], los, 0);
        speed = 0.5;
        if (restart && !patrol[0].equals(patrol[patrol.length - 1])) {
            throw new IllegalArgumentException("patrol that restarts must end and start on same point!");
        }
        if (patrol.length < 2) {
            throw new IllegalArgumentException("" + patrol.length);
        }

        // convert from array to lists
        this.patrol = new ArrayList<>();
        for (int i = 0; i < patrol.length; i++) {
            this.patrol.add(patrol[i]);
        }
        setPath(this.patrol);
        this.restart = restart;
    }

    @Override
    public void endOfPath() {
        if (!restart) {
            Collections.reverse(patrol);
        }
        setPath(patrol);
    }

    @Override
    public void update(long ms) {
        super.update(ms);
    }

    @Override
    public Point getCurScreenPos(final CoordinateSpace cs) {
        final Point p = super.getCurScreenPos(cs);
        calcDir(cs);
        return p;
    }

    private void calcDir(final CoordinateSpace cs) {
        final int pathIndex = (int)pathPos;
        final Point a, b;
        if (pathIndex == path.size() - 1) {
            a = path.get(path.size() - 2);
            b = path.get(path.size() - 1);
        } else {
            a = path.get(pathIndex);
            b = path.get(pathIndex + 1);
        }
        dir = cs.getDirectionVector(a, b);
    }

    public static class PatrollingGuardFactory implements GuardFactory {

        private final Point[] patrol;
        private final int los;
        private final boolean restart;

        public PatrollingGuardFactory(Point[] patrol, int los, boolean restart) {
            this.patrol = patrol;
            this.los = los;
            this.restart = restart;
        }

        @Override
        public GuardObject create(CoordinateSpace cs) {
            final PatrollingGuardObject guard = new PatrollingGuardObject(patrol, los, restart);
            guard.calcDir(cs);
            return guard;
        }
    }
}
