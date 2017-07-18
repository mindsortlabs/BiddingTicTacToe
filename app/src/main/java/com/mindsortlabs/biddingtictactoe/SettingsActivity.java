package com.mindsortlabs.biddingtictactoe;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

public class SettingsActivity extends AppCompatActivity {

    public static int animatedPlay = 0;
    public static int soundEffects = 0;
    ToggleButton animatedPlayBtn, soundToggleBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        animatedPlayBtn = (ToggleButton) findViewById(R.id.animated_play_btn);
        soundToggleBtn = (ToggleButton) findViewById(R.id.sound_toggle_btn);

        if(animatedPlay==0){
            animatedPlayBtn.setChecked(false);
        }
        else{
            animatedPlayBtn.setChecked(true);
        }

        animatedPlayBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    animatedPlay = 1;
                }

                else{
                    animatedPlay = 0;
                }
            }
        });


        if(soundEffects==0){
            soundToggleBtn.setChecked(false);
        }
        else{
            soundToggleBtn.setChecked(true);
        }

        soundToggleBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    soundEffects = 1;
                }

                else{
                    soundEffects = 0;
                }
            }
        });

    }
}
