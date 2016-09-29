package com.example.tassadar.twoohfoureight;

import android.animation.ObjectAnimator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements GameController.OnStateChangeListener {
    private GameView m_gameView;
    private GameController m_controller;
    private TextView m_score;
    private int m_lastScoreValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_score = (TextView)findViewById(R.id.score);
        m_gameView = (GameView)findViewById(R.id.game_view);
        m_controller = m_gameView.getController();
        m_controller.setListener(this);

        if(savedInstanceState != null) {
            m_controller.restoreInstanceState(savedInstanceState);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        m_controller.saveInstanceState(state);
        super.onSaveInstanceState(state);
    }

    @Override
    public void onScoreChanged(int score) {
        ObjectAnimator anim = ObjectAnimator.ofInt(this, "score", m_lastScoreValue, score);
        anim.setDuration(400);
        anim.start();

        m_lastScoreValue = score;
    }

    public void setScore(int score) {
        m_score.setText(String.valueOf(score));
    }
}
