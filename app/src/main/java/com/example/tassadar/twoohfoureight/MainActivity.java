package com.example.tassadar.twoohfoureight;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    private GameView m_gameView;
    private GameController m_controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_gameView = (GameView)findViewById(R.id.game_view);
        m_controller = m_gameView.getController();

        if(savedInstanceState != null) {
            m_controller.restoreInstanceState(savedInstanceState);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        m_controller.saveInstanceState(state);
        super.onSaveInstanceState(state);
    }
}
