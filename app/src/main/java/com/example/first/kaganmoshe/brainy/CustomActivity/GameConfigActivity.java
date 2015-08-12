package com.example.first.kaganmoshe.brainy.CustomActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.first.kaganmoshe.brainy.MenuActivity;
import com.example.first.kaganmoshe.brainy.R;
import com.example.first.kaganmoshe.brainy.Utils;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by tamirkash on 8/5/15.
 */
public abstract class GameConfigActivity extends AppActivity {

    protected TextView configTitle;
    protected SeekBar sessionsRangeSeekBar;
    protected TextView sessionsRangeValueTextView;
    protected Button m_StartGameButton;

    @Override
    public final void onBackPressed(){
        Utils.startNewActivity(this, MenuActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart(){
        super.onStart();

        sessionsRangeSeekBar = (SeekBar) findViewById(R.id.sessionsRangeSeekBar);
        sessionsRangeValueTextView = (TextView) findViewById(R.id.numberOfSessionsChoice);
        m_StartGameButton = (Button) findViewById(R.id.startGameButton);
        configTitle = (TextView) findViewById(R.id.configTitle);

        setTouchNClick(R.id.startGameButton);

        sessionsRangeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int value = sessionsRangeSeekBar.getProgress() + 1;

                sessionsRangeValueTextView.setText(Integer.toString(value));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        m_StartGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStartGameClick();
            }
        });
    }

    abstract protected void onStartGameClick();

    protected void setTitle(String title){
        configTitle.append(title);
        Utils.changeFont(getAssets(), configTitle);
    }
}
