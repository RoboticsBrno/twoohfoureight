package com.example.tassadar.foobar;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

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

    private Paint m_baseRectPaint;
    private Paint m_gridRectPaint;

    RenderImpl(Context ctx) {
        m_gridRects = new RectF[GRID*GRID];
        for(int i = 0; i < m_gridRects.length; ++i) {
            m_gridRects[i] = new RectF();
        }

        Resources res = ctx.getResources();

        m_baseRectPaint = new Paint();
        m_baseRectPaint.setColor(res.getColor(R.color.gridBackground));
        m_baseRectPaint.setAntiAlias(true);

        m_gridRectPaint = new Paint();
        m_gridRectPaint.setColor(res.getColor(R.color.tileBackground));
        m_gridRectPaint.setAntiAlias(true);
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
            return true;
        }

        return false;
    }

    private void layoutGrid() {
        final float padding = (m_baseRect.width() * GRID_PADDING) / (GRID + 1);
        final float startX = m_baseRect.left + padding;

        final float tileWidth = (m_baseRect.width() * (1 - GRID_PADDING)) / GRID;

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
    }
}
