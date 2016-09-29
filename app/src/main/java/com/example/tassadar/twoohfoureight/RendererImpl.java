package com.example.tassadar.twoohfoureight;

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
import java.util.HashSet;
import java.util.Map;


public class RendererImpl implements Renderer {
    private static final float GRID_PADDING = 0.1f;

    private View m_view;

    private Paint m_paintGrid;
    private Paint m_paintTile;
    private Paint[] m_tilePaints;
    private Paint m_tileStrokePaint;
    private Paint m_tileTextLight;
    private Paint m_tileTextDark;

    private float m_gridRoundness;
    private RectF m_gridRect;
    private float m_shakeOffset;

    private boolean m_initialized;

    private RectF[] m_tileRects;
    private RectF m_translatedTileRect;

    private HashMap<Integer, RenderedTile> m_tiles;
    private HashSet<Animator> m_animations;

    RendererImpl(Context ctx, View view) {
        super();

        m_view = view;

        Resources res = ctx.getResources();

        m_paintGrid = new Paint();
        m_paintGrid.setColor(res.getColor(R.color.gridBackground));
        m_paintGrid.setAntiAlias(true);

        m_paintTile = new Paint();
        m_paintTile.setColor(res.getColor(R.color.tileBackground));
        m_paintTile.setAntiAlias(true);

        m_tileRects = new RectF[GameController.GRID*GameController.GRID];
        for(int i = 0; i < m_tileRects.length; ++i) {
            m_tileRects[i] = new RectF();
        }

        TypedArray ta = res.obtainTypedArray(R.array.tileColors);
        m_tilePaints = new Paint[ta.length()];
        for (int i = 0; i < ta.length(); i++) {
            m_tilePaints[i] = new Paint();
            m_tilePaints[i].setColor(ta.getColor(i, 0));
            m_tilePaints[i].setAntiAlias(true);
        }
        ta.recycle();

        m_tileStrokePaint = new Paint();
        m_tileStrokePaint.setStyle(Paint.Style.STROKE);
        m_tileStrokePaint.setAntiAlias(true);
        m_tileStrokePaint.setColor(Color.GRAY);
        m_tileStrokePaint.setStrokeWidth(1.f);

        m_tileTextDark = new Paint();
        m_tileTextDark.setColor(res.getColor(R.color.tileTextDark));
        m_tileTextDark.setAntiAlias(true);
        m_tileTextDark.setTextAlign(Paint.Align.CENTER);

        m_tileTextLight = new Paint();
        m_tileTextLight.setColor(res.getColor(R.color.tileTextLight));
        m_tileTextLight.setAntiAlias(true);
        m_tileTextLight.setTextAlign(Paint.Align.CENTER);

        m_tiles = new HashMap<>();
        m_animations = new HashSet<>();
    }

    public void cleanup() {
        // cancel all animations, swap the member container
        // to prevent Concurrent access from onStop animation listener

        HashSet<Animator> anims = m_animations;
        m_animations = new HashSet<>();
        for(Animator a : anims) {
            a.cancel();
        }
    }

    public void finishAllAnimations() {
         // stop all animations, swap the member container
         // to prevent Concurrent access from onStop animation listener

         HashSet<Animator> anims = m_animations;
         m_animations = new HashSet<>();
         for(Animator a : anims) {
             a.end();
         }
    }

    public void render(Canvas c) {
        if(!m_initialized) {
            return;
        }

        if(m_shakeOffset != 0.f) {
            c.translate(m_shakeOffset, 0);
        }

        c.drawRoundRect(m_gridRect, m_gridRoundness, m_gridRoundness, m_paintGrid);

        for(RectF tile : m_tileRects) {
            c.drawRoundRect(tile, m_gridRoundness, m_gridRoundness, m_paintTile);
        }

        for(Map.Entry<Integer, RenderedTile> e : m_tiles.entrySet()) {
            RenderedTile t = e.getValue();

            c.save();
            c.translate(t.center.x, t.center.y);
            c.scale(t.scale, t.scale);

            c.drawRoundRect(m_translatedTileRect, m_gridRoundness, m_gridRoundness, t.rectPaint);
            c.drawRoundRect(m_translatedTileRect, m_gridRoundness, m_gridRoundness, m_tileStrokePaint);

            final float y = -(t.valuePaint.descent() + t.valuePaint.ascent()) / 2;
            c.drawText(t.valueText, 0.f, y, t.valuePaint);
            c.restore();
        }
    }

    public void invalidate() {
        m_view.invalidate();
    }

    public void dimensionsChanged(int w, int h) {
        if(w <= h) {
            int y = (h - w) / 2;
            m_gridRect = new RectF(0, y, w, y + w);
        } else {
            int x = (w - h) / 2;
            m_gridRect = new RectF(x, 0, x + h, h);
        }

        m_gridRoundness = m_gridRect.width() * 0.02f;

        layoutTiles();

        m_tileTextDark.setTextSize(m_tileRects[0].height()*0.4f);
        m_tileTextLight.setTextSize(m_tileRects[0].height()*0.4f);

        m_initialized = true;
        invalidate();
    }

    private void layoutTiles() {
        final float padding = (m_gridRect.width() * GRID_PADDING) / (GameController.GRID + 1);
        float startLeft = m_gridRect.left + padding;

        float left = startLeft;
        float top = m_gridRect.top + padding;

        float tileWidth = (m_gridRect.width()*(1.f - GRID_PADDING)) / GameController.GRID;

        m_translatedTileRect = new RectF(-(tileWidth/2), -(tileWidth/2), tileWidth/2, tileWidth/2);

        for(int y = 0; y < GameController.GRID; ++y) {
            for(int x = 0; x < GameController.GRID; ++x) {
                RectF rect = m_tileRects[y*GameController.GRID + x];
                rect.left = left;
                rect.top = top;
                rect.right = left + tileWidth;
                rect.bottom = top + tileWidth;

                left += tileWidth + padding;
            }

            top += tileWidth + padding;
            left = startLeft;
        }

        finishAllAnimations();

        // make sure tiles are at the right positions, remove merged ones
        for (Map.Entry<Integer, RenderedTile> integerRenderedTileEntry : m_tiles.entrySet()) {
            RenderedTile tile = integerRenderedTileEntry.getValue();
            tile.setPosition(m_tileRects[tile.gridIndex]);
        }
    }

    public void setShakeOffset(float off) {
        m_shakeOffset = off;
    }

    public void shake() {
        ObjectAnimator a = ObjectAnimator.ofFloat(this, "shakeOffset", 0.f, 20.f, -20.f, 20.f, -20.f, 0.f);
        a.setDuration(400);
        a.addUpdateListener(m_tileAnimationUpdateListener);
        a.addListener(m_tileAnimationListener);
        a.start();

        m_animations.add(a);
    }

    private static class RenderedTile {
        final PointF center = new PointF(); // also acts as a lock
        float scale;

        Paint rectPaint;
        String valueText;
        int value;
        Paint valuePaint;
        int gridIndex;

        public void setPosition(float x, float y) {
            this.center.set(x, y);
        }

        public void setPosition(RectF rect) {
            this.center.set(rect.centerX(), rect.centerY());
        }

        public void setScale(float scale) {
            this.scale = scale;
        }

        public void setX(float val) {
            this.center.x = val;
        }

        public void setY(float val) {
            center.y = val;
        }
    }

    public void addTile(int id, int value, int position) {
        RenderedTile t = new RenderedTile();
        t.scale = 0.f;

        ObjectAnimator anim = ObjectAnimator.ofFloat(t, "scale", 0.f, 1.f);
        anim.setInterpolator(new OvershootInterpolator());
        anim.addListener(m_tileAnimationListener);
        anim.addUpdateListener(m_tileAnimationUpdateListener);
        anim.setDuration(500);
        anim.setStartDelay(200);

        m_tiles.put(id, t);
        setTileValue(id, value);
        setTilePosition(id, position, false);

        m_animations.add(anim);
        anim.start();
    }

    public void removeTile(int id) {
        m_tiles.remove(id);
    }

    public void removeAllTiles() {
        cleanup();
        m_tiles.clear();
    }

    public void setTileValue(int tileId, int value) {
        int idx = Integer.numberOfTrailingZeros(value) - 1;
        if(idx < 0) {
            idx = 0;
        } else if(idx >= m_tilePaints.length) {
            idx = m_tilePaints.length - 1;
        }


        RenderedTile tile = m_tiles.get(tileId);
        if(tile == null) {
            return;
        }

        tile.rectPaint = m_tilePaints[idx];
        tile.valueText = String.valueOf(value);
        tile.value = value;

        final int c = tile.rectPaint.getColor();
        // Is the color dark?
        if (((c & 0xFF) + ((c >> 8) & 0xFF) + ((c >> 16) & 0xFF)) / 3 < 170) {
            tile.valuePaint = m_tileTextLight;
        } else {
            tile.valuePaint = m_tileTextDark;
        }
    }

    public ObjectAnimator setTilePosition(int tileId, int gridIdx, boolean animate) {
        RenderedTile tile = m_tiles.get(tileId);

        tile.gridIndex = gridIdx;

        if(!animate) {
            tile.setPosition(m_tileRects[gridIdx]);
            invalidate();
            return null;
        }

        ObjectAnimator anim;
        float destX = m_tileRects[gridIdx].centerX();
        float destY = m_tileRects[gridIdx].centerY();

        if (Math.abs(destX - tile.center.x) > 1) {
            anim = ObjectAnimator.ofFloat(tile, "x", tile.center.x, destX);
        } else {
            anim = ObjectAnimator.ofFloat(tile, "y", tile.center.y, destY);
        }

        anim.setInterpolator(new OvershootInterpolator());
        anim.addListener(m_tileAnimationListener);
        anim.addUpdateListener(m_tileAnimationUpdateListener);
        anim.setDuration(500);
        anim.start();

        m_animations.add(anim);
        return anim;
    }

    public void mergeTiles(int tileIdA, int tileIdB, int targetGridIdx) {
        ObjectAnimator anim = setTilePosition(tileIdB, targetGridIdx, true);
        anim.addListener(new MergeTileAnimatorListener(tileIdA, tileIdB));
    }

    private void bounceTile(int tileId) {
        RenderedTile tile = m_tiles.get(tileId);

        ObjectAnimator a = ObjectAnimator.ofFloat(tile, "scale", 1.f, 1.3f, 1.f);
        a.setDuration(400);
        a.addListener(m_tileAnimationListener);
        a.addUpdateListener(m_tileAnimationUpdateListener);
        a.start();
    }

    private class MergeTileAnimatorListener implements Animator.AnimatorListener {
        int tileIdA, tileIdB;

        MergeTileAnimatorListener(int tileIdA, int tileIdB) {
            this.tileIdA = tileIdA;
            this.tileIdB = tileIdB;
        }

        @Override
        public void onAnimationEnd(Animator animator) {
            removeTile(tileIdB);

            RenderedTile tileA = m_tiles.get(tileIdA);
            setTileValue(tileIdA, tileA.value*2);
            bounceTile(tileIdA);

            invalidate();
        }

        @Override
        public void onAnimationStart(Animator animator) { }
        @Override
        public void onAnimationCancel(Animator animator) { }
        @Override
        public void onAnimationRepeat(Animator animator) { }
    }

    private Animator.AnimatorListener m_tileAnimationListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animator) {

        }

        @Override
        public void onAnimationEnd(Animator animator) {
            m_animations.remove(animator);
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    };

    private ValueAnimator.AnimatorUpdateListener m_tileAnimationUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            invalidate();
        }
    };

}
