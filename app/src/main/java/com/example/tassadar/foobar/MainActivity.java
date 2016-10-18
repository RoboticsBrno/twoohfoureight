package com.example.tassadar.foobar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Button btn = (Button)findViewById(R.id.button_start_game);
        btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        startActivity(new Intent(this, GameActivity.class));
    }



























    private void intents() {
       /* Intent i2 = new Intent(Intent.ACTION_DIAL);
        startActivity(i2);





        Intent i1 = new Intent(Intent.ACTION_VIEW);
        i1.setData(Uri.parse("http://tasemnice.eu"));
        startActivity(i1);





        String action = BuildConfig.APPLICATION_ID + ".MOJE_ACTION";
        Intent i3 = new Intent(action);
        sendBroadcast(i3);

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // udelam neco pri dostani intentu

                intent.

            }
        }, new IntentFilter(action));






        startActivity(new Intent(this, MainActivity.class));*/
    }




}
