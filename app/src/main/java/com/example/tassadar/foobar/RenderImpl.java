package com.example.tassadar.foobar;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * Created by tassadar on 18.10.16.
 */

public class RenderImpl {
    public static final int GRID = 4;

    private static final float GRID_PADDING = 0.1f;

    private int m_lastWidth, m_lastHeight;
    private boolean m_initialized;

    private float m_gridRoundness;
    private RectF m_baseRect;
    private RectF[] m_gridRects;
    private RectF m_basePlayTileRect;

    private Paint[] m_tilePaints;
    private Paint m_tileTextLight;
    private Paint m_tileTextDark;
    private Paint m_baseRectPaint;
    private Paint m_gridRectPaint;
    private View m_parent;

    private HashMap<Integer, PlayTile> m_playTiles;

    RenderImpl(Context ctx, View parent) {
        m_gridRects = new RectF[GRID*GRID];
        for(int i = 0; i < m_gridRects.length; ++i) {
            m_gridRects[i] = new RectF();
        }

        m_parent = parent;

        m_playTiles = new HashMap<>();

        Resources res = ctx.getResources();

        m_baseRectPaint = new Paint();
        m_baseRectPaint.setColor(res.getColor(R.color.gridBackground));
        m_baseRectPaint.setAntiAlias(true);

        m_gridRectPaint = new Paint();
        m_gridRectPaint.setColor(res.getColor(R.color.tileBackground));
        m_gridRectPaint.setAntiAlias(true);

        m_tileTextDark = new Paint();
        m_tileTextDark.setColor(res.getColor(R.color.tileTextDark));
        m_tileTextDark.setAntiAlias(true);
        m_tileTextDark.setTextAlign(Paint.Align.CENTER);

        m_tileTextLight = new Paint();
        m_tileTextLight.setColor(res.getColor(R.color.tileTextLight));
        m_tileTextLight.setAntiAlias(true);
        m_tileTextLight.setTextAlign(Paint.Align.CENTER);

        TypedArray ta = res.obtainTypedArray(R.array.tileColors);
        m_tilePaints = new Paint[ta.length()];
        for (int i = 0; i < ta.length(); i++) {
            m_tilePaints[i] = new Paint();
            m_tilePaints[i].setColor(ta.getColor(i, 0));
            m_tilePaints[i].setAntiAlias(true);
        }
        ta.recycle();
    }

    public boolean dimensionsChanged(int w, int h) {
        if(m_lastWidth == w && m_lastHeight == h) {
            return false;
        }

        m_lastWidth = w;
        m_lastHeight = h;

        if(w <= h) {
            int side = (h - w) / 2;
            m_baseRect = new RectF(0, side, w, side+w);
        } else {
            int side = (w - h ) / 2;
            m_baseRect = new RectF(side, 0, h, side+h);
        }

        m_gridRoundness = m_baseRect.width() * 0.02f;

        layoutGrid();

        if(!m_initialized) {
            m_initialized = true;

            int id = 0;
            addTile(id++, 4, 0);
            addTile(id++, 8, 4);
            addTile(id++, 8, 5);
            addTile(id++, 32, 15);

            this.parent.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //setTilePosition(0, 3, true);
                    mergeTails(1,2,0);
                }
            }, 2000);

            return true;
        }

        return false;
    }

    private void layoutGrid() {
        final float padding = (m_baseRect.width() * GRID_PADDING) / (GRID + 1);
        final float startX = m_baseRect.left + padding;

        final float tileWidth = (m_baseRect.width() * (1 - GRID_PADDING)) / GRID;

        m_basePlayTileRect = new RectF(-tileWidth/2, -tileWidth/2, tileWidth/2, tileWidth/2);

        m_tileTextDark.setTextSize(tileWidth * 0.4f);
        m_tileTextLight.setTextSize(tileWidth * 0.4f);

        float tileX = startX;
        float tileY = m_baseRect.top + padding;

        for(int y = 0; y < GRID; ++y) {
            for(int x = 0; x < GRID; ++x) {
                m_gridRects[y*GRID + x].set(tileX, tileY, tileX + tileWidth, tileY + tileWidth);
                tileX += tileWidth + padding;
            }
            tileX = startX;
            tileY += tileWidth + padding;
        }
    }

    public void render(Canvas c) {
        if(!m_initialized) {
            return;
        }

        c.drawRoundRect(m_baseRect, m_gridRoundness, m_gridRoundness, m_baseRectPaint);

        for(RectF rect : m_gridRects) {
            c.drawRoundRect(rect, m_gridRoundness, m_gridRoundness, m_gridRectPaint);
        }

        for(Map.Entry<Integer, PlayTile> e  : m_playTiles.entrySet()) {
            PlayTile t = e.getValue();

            c.save();
            c.translate(t.center.x, t.center.y);
            c.scale(t.scale, t.scale);
            c.drawRoundRect(m_basePlayTileRect, m_gridRoundness, m_gridRoundness, t.rectPaint);

            final float y = -(t.textPaint.descent() + t.textPaint.ascent()) / 2;
            c.drawText(t.valueStr, 0.f, y, t.textPaint);
            c.restore();
        }
    }

    public void addTile(int id, int value, int position) {
        PlayTile t = new PlayTile();
        t.center = new PointF();

        m_playTiles.put(id, t);

        setTilePosition(id, position, false);
        setTileValue(id, value);
        ObjectAnimator oa = ObjectAnimator.ofFloat(t, "scale", 0.0f, 1.0f);
        oa.setDuration(300);
        oa.setInterpolator(new OvershootInterpolator());
        oa.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                m_parent.invalidate();
            }
        });
        oa.start();
    }

    public ObjectAnimator raise(int id) {
        ObjectAnimator oa = ObjectAnimator.ofFloat(m_playTiles.get(id), "scale", 1.0f, 1.3f, 1.0f);
        oa.setDuration(300);
        oa.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                parent.invalidate();
            }
        });
        oa.start();
        return oa;
    }

    public ObjectAnimator setTilePosition(int id, int position, boolean animate) {
        PlayTile t = m_playTiles.get(id);
        t.gridIndex = position;

        if (animate) {
            ObjectAnimator oa;
            if (t.getX() == m_gridRects[position].centerX()) {
                // change Y position
                oa = ObjectAnimator.ofFloat(t, "y", t.getY(), m_gridRects[position].centerY());
            } else {
                // change X position
                oa = ObjectAnimator.ofFloat(t, "x", t.getX(), m_gridRects[position].centerX());
            }
            oa.setDuration(500);
            oa.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    parent.invalidate();
                }
            });
            oa.start();
            return oa;
        } else {
            t.setPosition(m_gridRects[position]);
            return null;
        }
    }

    public void mergeTails(int tail1, int tail2, int mergePos) {
        ObjectAnimator anim = setTilePosition(tail2, mergePos, true);
        anim.addListener(new MergeTailAnimatorListener(tail1, tail2));
    }

    private class MergeTailAnimatorListener implements Animator.AnimatorListener {
        int tail1, tail2;

        MergeTailAnimatorListener(int tail1, int tail2) {
            this.tail1 = tail1;
            this.tail2 = tail2;
        }

        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            setTileValue(this.tail1, m_playTiles.get(this.tail1).getValue()*2);
            m_playTiles.remove(this.tail2);
            raise(this.tail1);
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }

    public void setTileValue(int id, int value) {
        int idx = Integer.numberOfTrailingZeros(value) - 1;
        if(idx < 0) {
            idx = 0;
        } else if(idx >= m_tilePaints.length) {
            idx = m_tilePaints.length - 1;
        }

        PlayTile tile = m_playTiles.get(id);
        tile.rectPaint = m_tilePaints[idx];
        tile.valueStr = String.valueOf(value);
        tile.value = value;

        final int c = tile.rectPaint.getColor();
        // Is the color dark?
        if ((Color.red(c) + Color.green(c) + Color.blue(c)) / 3 < 170) {
            tile.textPaint = m_tileTextLight;
        } else {
            tile.textPaint = m_tileTextDark;
        }
    }
}
