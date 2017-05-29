package papercastle.com.papercastle;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

/**
 * Created by Josh on 5/28/2017.
 */

class EndObject extends GameObject {

    public EndObject(Point point) {
        super(point, 0.0,  Color.argb(255, 255, 255, 0));
    }

    @Override
    public void draw(CoordinateSpace cs, Canvas canvas, Paint paint) {
        final Point pos = getCurScreenPos(cs);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        final int halfGridSize = cs.getGridSize() / 2;
        canvas.drawRect(pos.x - halfGridSize, pos.y - halfGridSize, pos.x + halfGridSize, pos.y + halfGridSize, paint);
    }
}
