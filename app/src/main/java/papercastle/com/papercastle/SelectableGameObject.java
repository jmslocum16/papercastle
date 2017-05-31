package papercastle.com.papercastle;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;

/**
 * Created by Josh on 5/28/2017.
 */

public class SelectableGameObject extends GameObject {

    private volatile boolean selected;
    private int selectedColor;

    public SelectableGameObject(Point start, double speed, int color) {
        super(start, speed, color);
        selected = false;
        selectedColor = Color.argb(255, 255 - Color.red(color), 255 - Color.green(color), 255 - Color.blue(color));
    }

    public boolean isSelected() {
        return selected;
    }

    public void unselect() {
        Log.e("SelectableGameObject", "unselecting " + toString());
        selected = false;
    }

    public void select() {
        Log.e("SelectableGameObject", "selecting " + toString());
        selected = true;
    }

    @Override
    public void draw(CoordinateSpace cs, Canvas canvas, Paint paint) {
        super.draw(cs, canvas, paint);

        if (selected) {
            final Point screenPos = getCurScreenPos(cs);
            paint.setColor(selectedColor);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(10);
            canvas.drawCircle(screenPos.x, screenPos.y, cs.getGridSize() / 4, paint);
        }
    }

    public interface CloneFactory {
        SelectableGameObject create(Point p);
        int color();
    }

    public static class SimpleCloneFactory implements CloneFactory {
        final double speed;
        final int color;

        SimpleCloneFactory(double speed, int color) {
            this.speed = speed;
            this.color = color;
        }


        @Override
        public SelectableGameObject create(Point p) {
            return new SelectableGameObject(p, speed, color);
        }

        @Override
        public int color() {
            return color;
        }
    }

    // TODO real clone types
    public static final CloneFactory[] CLONE_TYPE_DEFS = new CloneFactory[] {
            new SimpleCloneFactory(1.0, Color.argb(255, 255, 160, 0)),
            new SimpleCloneFactory(2.0, Color.argb(255, 255, 0, 0)),
            new SimpleCloneFactory(0.5, Color.argb(100, 0, 255, 0)),
            new SimpleCloneFactory(4.0, Color.argb(255, 0, 0, 255)),
            new SimpleCloneFactory(0.25, Color.argb(255, 255, 0, 160)),
    };
}
