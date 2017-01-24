package com.example.tassadar.foobar;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by tassadar on 11.10.16.
 */

public class GameView extends View {
    private RenderImpl m_renderer;
    private GameController m_controller;
    private GestureDetector g_detector;

    public GameView(Context context) {
        super(context);
        init(context);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public GameView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context ctx) {
        m_renderer = new RenderImpl(ctx, this);
        m_controller = new GameController(m_renderer);
        g_detector = new GestureDetector(ctx, new GestureListener());

        addOnLayoutChangeListener(m_layoutChangedListener);
    }

    private final View.OnLayoutChangeListener m_layoutChangedListener = new OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            m_renderer.dimensionsChanged(right -left, bottom - top);
            m_controller.restart();
        }
    };

    @Override
    public void onDraw(Canvas canvas) {
        m_renderer.render(canvas);
    }

    public boolean onTouchEvent(MotionEvent e) {
        g_detector.onTouchEvent(e);
        return true;
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (Math.abs(e1.getX() - e2.getX()) > Math.abs(e1.getY() - e2.getY())) {
                // horizontal swipe
                if (e2.getX() > e1.getX())
                    m_controller.swipe(m_controller.D_RIGHT);
                else
                    m_controller.swipe(m_controller.D_LEFT);
            } else {
                // vertical swipe
                if (e2.getY() > e1.getY())
                    m_controller.swipe(m_controller.D_DOWN);
                else
                    m_controller.swipe(m_controller.D_UP);
            }

            return false;
        }
    }
}
