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
}
