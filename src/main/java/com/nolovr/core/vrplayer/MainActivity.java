package com.nolovr.core.vrplayer;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;

public class MainActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        DisplayManager mDisplayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        Display        mDisplay[]      = mDisplayManager.getDisplays();

        Display[] presentationDisplays = mDisplayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION);

        if (presentationDisplays.length > 0) {
            Intent          intent  = new Intent(this, NioActivity.class);
            ActivityOptions options = ActivityOptions.makeBasic();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            options.setLaunchDisplayId(presentationDisplays[0].getDisplayId());
            startActivity(intent, options.toBundle());

            finish();
        }


    }
}