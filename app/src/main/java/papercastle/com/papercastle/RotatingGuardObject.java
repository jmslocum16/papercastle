package papercastle.com.papercastle;

import android.graphics.Point;

/**
 * Created by Josh on 5/30/2017.
 */

public class RotatingGuardObject extends GuardObject {

    private static final int ROTATE_MS = CELEBRATE_MS;
    private int rotateMs;

    private final int[] directions;
    private final boolean restart;
    private int directionIndex;

    public RotatingGuardObject(Point start, int los, int[] directions, boolean restart) {
        super(start, los, directions[0]);
        if (directions.length < 2) throw new IllegalArgumentException("" + directions.length);
        this.directions = directions;
        directionIndex = 0;
        rotateMs = 0;
        this.restart = restart;
    }

    @Override
    public void update(long ms) {
        super.update(ms);
        // calculate rotation
        rotateMs += ms;
        while (rotateMs >= ROTATE_MS) {
            rotate();
            rotateMs -= ROTATE_MS;
        }
    }

    private void rotate() {
        if (directionIndex == directions.length - 1) {
            if (restart) {
                directionIndex = 0;
            } else {
                directionIndex = -(directions.length - 2);
            }
        } else {
            directionIndex++;
        }
        dir = directions[Math.abs(directionIndex)];
    }

    public static class RotatingGuardFactory implements GuardFactory {

        private final Point p;
        private final int los;
        private final int[] dirs;
        private final boolean restart;

        public RotatingGuardFactory(Point p, int los, int[] dirs, boolean restart) {
            this.p = p;
            this.los = los;
            this.dirs = dirs;
            this.restart = restart;
        }

        @Override
        public GuardObject create(CoordinateSpace cs) {
            return new RotatingGuardObject(p, los, dirs, restart);
        }
    }
}
