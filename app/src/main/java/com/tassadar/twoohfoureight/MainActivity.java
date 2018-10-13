package com.tassadar.twoohfoureight;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements GameController.OnStateChangeListener {
    private GameView m_gameView;
    private GameController m_controller;
    private TextView m_score;
    private TextView m_statusText;
    private int m_lastScoreValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_score = (TextView)findViewById(R.id.score);
        m_statusText = (TextView)findViewById(R.id.status_text);

        m_gameView = (GameView)findViewById(R.id.game_view);
        m_controller = m_gameView.getController();
        m_controller.setListener(this);

        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        m_controller.restoreInstanceState(pref);
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        m_controller.saveInstanceState(editor);
        editor.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.restartItem:
                m_gameView.restartGame();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setScore(int score) {
        m_score.setText(String.valueOf(score));
    }

    @Override
    public void onScoreChanged(int score) {
        ObjectAnimator anim = ObjectAnimator.ofInt(this, "score", m_lastScoreValue, score);
        anim.setDuration(400);
        anim.start();

        m_lastScoreValue = score;
    }

    @Override
    public void onStateChanged(int state) {
        switch(state) {
            case GameController.STATE_WON:
                m_statusText.setText(R.string.you_have_won);
                break;
            case GameController.STATE_LOST:
                m_statusText.setText(R.string.game_over);
                break;
            default:
                m_statusText.setText("");
                break;
        }
    }

    @Override
    public void onLoss() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(R.string.game_over);
        b.setMessage(this.getString(R.string.game_over_body, m_lastScoreValue));
        b.setCancelable(false);
        b.setPositiveButton(R.string.ok, null);
        b.setNeutralButton(R.string.restart, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_gameView.restartGame();
            }
        });
        b.create().show();
    }

    @Override
    public void onWin() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(R.string.you_have_won);
        b.setMessage(R.string.you_have_won_body);
        b.setCancelable(false);
        b.setPositiveButton(R.string.ok, null);
        b.setNeutralButton(R.string.restart, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_gameView.restartGame();
            }
        });
        b.create().show();
    }
}
