package com.example.cisc.retrosquash;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

public class MainActivity extends Activity implements View.OnClickListener{

    Button buttonObjectPlay;
    static CheckBox checkBoxObjectMute;
    static CheckBox checkBoxObjectLimit;
    static CheckBox checkBoxObjectOnePlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonObjectPlay = (Button) findViewById(R.id.buttonPlay);
        buttonObjectPlay.setOnClickListener(this);

        checkBoxObjectLimit = (CheckBox) findViewById(R.id.checkBoxLimit);
        checkBoxObjectMute = (CheckBox) findViewById(R.id.checkBoxMute);
        checkBoxObjectOnePlayer = (CheckBox) findViewById(R.id.checkBoxOnePlayer);
    }

    @Override
    public void onClick(View v) {
        Intent i = new Intent(this, GameActivity.class);
        startActivity(i);
    }
}