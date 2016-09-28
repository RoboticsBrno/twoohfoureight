package com.example.tassadar.twoohfoureight;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    private RenderThread m_renderThread;
    private GameController m_controller;
    private GestureDetector m_gestureDetector;

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
        m_controller = new GameController();
        m_gestureDetector = new GestureDetector(ctx, m_gestureListener);


        getHolder().addCallback(this);
    }

    public GameController getController() {
        return m_controller;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        m_renderThread = new RenderThread(this.getContext(), surfaceHolder);
        m_renderThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int w, int h) {
        m_renderThread.canvasDimensionsChanged(w, h);
        m_controller.restoreState(m_renderThread);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        m_renderThread.interrupt();
        try {
            m_renderThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        m_renderThread.cleanup();
        m_renderThread = null;
    }

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

            Log.i("Test", String.format("direction %d vx %f vy %f x %f %f y %f %f", direction, velocityX, velocityY, e1.getX(), e2.getX(), e1.getY(), e2.getY()));

            m_controller.onSwipe(direction, m_renderThread);

            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onDown(MotionEvent motionEvent) {
            return true;
        }
    };
}
