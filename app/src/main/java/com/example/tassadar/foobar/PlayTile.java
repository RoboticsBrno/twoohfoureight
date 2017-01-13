package com.example.tassadar.foobar;

import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;

/**
 * Created by tassadar on 25.10.16.
 */

public class PlayTile {
    PointF center;
    Paint rectPaint;
    Paint textPaint;
    String valueStr;
    int value;
    int gridIndex;
    float scale;

    public float getX() {
        return center.x;
    }
    public float getY() {
        return center.y;
    }
    public void setX(float x) {
        center.x = x;
    }
    public void setY(float y) {
        center.y = y;
    }

    public void setPosition(RectF rect) {
        center.set(rect.centerX(), rect.centerY());
    }

    public void setScale(float ns) { scale = ns; }
}
