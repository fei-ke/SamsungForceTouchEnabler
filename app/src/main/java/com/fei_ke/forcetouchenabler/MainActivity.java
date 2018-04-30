package com.fei_ke.forcetouchenabler;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        if (checkCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS") != PackageManager.PERMISSION_GRANTED)
            try {
                Runtime.getRuntime().exec("su -c pm grant " + BuildConfig.APPLICATION_ID + " android.permission.WRITE_SECURE_SETTINGS")
                        .waitFor();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        Switch switchSwitch = findViewById(R.id.switchCustomHeight);
        final TextView textViewHeight = findViewById(R.id.textViewHeight);
        final SeekBar seekBarHeight = findViewById(R.id.seekBarHeight);

        final ContentResolver resolver = getContentResolver();

        final int isCustom = Settings.Global.getInt(resolver, BuildConfig.APPLICATION_ID + ".is_custom_navi_bar_height", 0);
        switchSwitch.setChecked(isCustom == 1);

        switchSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Settings.Global.putInt(resolver, BuildConfig.APPLICATION_ID + ".is_custom_navi_bar_height", isChecked ? 1 : 0);

                textViewHeight.setEnabled(isChecked);
                seekBarHeight.setEnabled(isChecked);
            }
        });

        seekBarHeight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewHeight.setText(String.valueOf(progress));
                Settings.Global.putInt(resolver, BuildConfig.APPLICATION_ID + ".custom_navi_bar_height", progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        int height = Settings.Global.getInt(resolver, BuildConfig.APPLICATION_ID + ".custom_navi_bar_height", 45);
        seekBarHeight.setProgress(height);
    }
}
