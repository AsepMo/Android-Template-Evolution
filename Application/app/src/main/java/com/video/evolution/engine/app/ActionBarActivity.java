package com.video.evolution.engine.app;

import android.support.annotation.Nullable;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.video.evolution.application.analytics.AnalyticsManager;
import com.video.evolution.engine.Api;
import com.video.evolution.engine.app.settings.Settings;

public abstract class ActionBarActivity extends AppCompatActivity{
    public abstract void setUpStatusBar();
    public abstract void setUpDefaultStatusBar();
    public static final String TAG = ActionBarActivity.class.getSimpleName();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Settings.changeThemeStyle(getDelegate());
        super.onCreate(savedInstanceState);

    }

    @Override
    public ActionBar getSupportActionBar() {
        return super.getSupportActionBar();
    }

    @Override
    public void recreate() {
        Settings.changeThemeStyle(getDelegate());
        super.recreate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AnalyticsManager.setCurrentScreen(this, getTag());
    }


    public abstract String getTag();
}



