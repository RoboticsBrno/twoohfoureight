package com.example.tassadar.twoohfoureight;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;


public class GameView extends View {
    private GameController m_controller;
    private GestureDetector m_gestureDetector;
    private RendererImpl m_renderer;

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

        setFocusable(true);
    }

    private void init(Context ctx) {
        m_controller = new GameController();
        m_gestureDetector = new GestureDetector(ctx, m_gestureListener);

        m_renderer = new RendererImpl(ctx, this);
        this.addOnLayoutChangeListener(m_layoutChangeListener);
    }

    public GameController getController() {
        return m_controller;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        m_renderer.render(canvas);
    }

    private final View.OnLayoutChangeListener m_layoutChangeListener = new View.OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            m_renderer.dimensionsChanged(right - left, bottom - top);
            m_controller.restoreState(m_renderer);
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return m_gestureDetector.onTouchEvent(event);
    }

    private GestureDetector.OnGestureListener m_gestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            int direction;
            if(Math.abs(e1.getX() - e2.getX()) > Math.abs(e1.getY() - e2.getY())) {
                if(velocityX > 0) {
                    direction = GameController.RIGHT;
                } else {
                    direction = GameController.LEFT;
                }
            } else {
                if(velocityY > 0) {
                    direction = GameController.DOWN;
                } else {
                    direction = GameController.UP;
                }
            }

            m_controller.onSwipe(direction, m_renderer);

            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onDown(MotionEvent motionEvent) {
            return true;
        }
    };
}
