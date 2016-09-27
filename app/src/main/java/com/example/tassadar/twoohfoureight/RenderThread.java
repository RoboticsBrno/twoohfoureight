package com.example.tassadar.twoohfoureight;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.view.SurfaceHolder;

public class RenderThread extends Thread {
    private static final int GRID = 4;
    private static final float GRID_PADDING = 0.1f;

    private final Object m_renderRequest = new Object();
    private SurfaceHolder m_holder;
    private Context m_context;

    private Paint m_paintGrid;
    private Paint m_paintTile;
    private int m_colorBackground;

    private float m_gridRoundness;
    private RectF m_gridRect;

    private boolean m_initialized;

    private RectF[] m_tileRects;

    RenderThread(Context ctx, SurfaceHolder holder) {
        super();

        m_holder = holder;
        m_context = ctx;

        Resources res = m_context.getResources();

        m_paintGrid = new Paint();
        m_paintGrid.setColor(res.getColor(R.color.gridBackground));
        m_paintGrid.setAntiAlias(true);

        m_paintTile = new Paint();
        m_paintTile.setColor(res.getColor(R.color.tileBackground));
        m_paintTile.setAntiAlias(true);

        m_colorBackground = res.getColor(R.color.baseBackground);

        m_tileRects = new RectF[GRID*GRID];
        for(int i = 0; i < GRID*GRID; ++i) {
            m_tileRects[i] = new RectF();
        }
    }

    @Override
    public void run() {
        try {
            synchronized (m_renderRequest) {
                while(!this.isInterrupted()) {
                    render();
                    m_renderRequest.wait();
                }
            }
        } catch (InterruptedException e) {
            // let the thread exit
        }
    }

    public void canvasDimensionsChanged(int w, int h) {
        synchronized (m_renderRequest) {
            if(w <= h) {
                int y = (h - w) / 2;
                m_gridRect = new RectF(0, y, w, y + w);
            } else {
                int x = (w - h) / 2;
                m_gridRect = new RectF(x, 0, x + h, h);
            }

            m_gridRoundness = m_gridRect.width() * 0.03f;

            layoutTilesLocked();

            m_initialized = true;
        }

        invalidate();
    }

    private void layoutTilesLocked() {
        final float padding = (m_gridRect.width() * GRID_PADDING) / (GRID + 1);
        float startLeft = m_gridRect.left + padding;

        float left = startLeft;
        float top = m_gridRect.top + padding;

        float tileWidth = (m_gridRect.width()*(1.f - GRID_PADDING)) / GRID;

        for(int y = 0; y < GRID; ++y) {
            for(int x = 0; x < GRID; ++x) {
                RectF rect = m_tileRects[y*GRID + x];
                rect.left = left;
                rect.top = top;
                rect.right = left + tileWidth;
                rect.bottom = top + tileWidth;

                left += tileWidth + padding;
            }

            top += tileWidth + padding;
            left = startLeft;
        }
    }

    private void render() {
        if(!m_initialized) {
            return;
        }

        final Canvas c = m_holder.lockCanvas();
        if(c == null) {
            return;
        }

        c.drawColor(m_colorBackground);
        c.drawRoundRect(m_gridRect, m_gridRoundness, m_gridRoundness, m_paintGrid);

        for(RectF tile : m_tileRects) {
            c.drawRoundRect(tile, m_gridRoundness, m_gridRoundness, m_paintTile);
        }

        m_holder.unlockCanvasAndPost(c);
    }

    public void invalidate() {
        synchronized (m_renderRequest) {
            m_renderRequest.notify();
        }
    }
}
