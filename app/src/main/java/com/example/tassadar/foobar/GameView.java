package com.example.tassadar.foobar;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by tassadar on 11.10.16.
 */

public class GameView extends View {
    private RenderImpl m_renderer;

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
        m_renderer = new RenderImpl(ctx);

        addOnLayoutChangeListener(m_layoutChangedListener);
    }

    private final View.OnLayoutChangeListener m_layoutChangedListener = new OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            m_renderer.dimensionsChanged(right -left, bottom - top);
        }
    };

    @Override
    public void onDraw(Canvas canvas) {
        m_renderer.render(canvas);
    }
}
