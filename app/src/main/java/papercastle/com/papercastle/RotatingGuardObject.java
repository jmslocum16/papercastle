package papercastle.com.papercastle;

import android.graphics.Point;

/**
 * Created by Josh on 5/30/2017.
 */

public class RotatingGuardObject extends GuardObject {

    private static final int ROTATE_MS = CELEBRATE_MS;
    private int rotateMs;

    private final int[] directions;
    private int directionIndex;

    public RotatingGuardObject(Point start, int los, int[] directions) {
        super(start, los, directions[0]);
        if (directions.length < 2) throw new IllegalArgumentException("" + directions.length);
        this.directions = directions;
        directionIndex = 0;
        rotateMs = 0;
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
            directionIndex = -(directions.length - 2);
        } else {
            directionIndex++;
        }
        dir = directions[Math.abs(directionIndex)];
    }

    public static class RotatingGuardFactory implements GuardFactory {

        private final Point p;
        private final int los;
        private final int[] dirs;

        public RotatingGuardFactory(Point p, int los, int[] dirs) {
            this.p = p;
            this.los = los;
            this.dirs = dirs;
        }

        @Override
        public GuardObject create() {
            return new RotatingGuardObject(p, los, dirs);
        }
    }
}
