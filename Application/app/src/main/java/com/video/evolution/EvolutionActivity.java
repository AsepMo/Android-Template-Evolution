package com.video.evolution;

import android.annotation.TargetApi;
import android.support.v7.app.AlertDialog;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.content.Intent;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import com.video.evolution.application.ApplicationActivity;
import com.video.evolution.application.SettingsActivity;
import com.video.evolution.engine.Api;
import com.video.evolution.engine.app.ActionBarActivity;
import com.video.evolution.engine.app.settings.Settings;
import com.video.evolution.engine.graphics.SystemBarTintManager;

public class EvolutionActivity extends ActionBarActivity {
    
    private static final String TAG = EvolutionActivity.class.getSimpleName();
    
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        if(Api.hasLollipop()){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }
        else if(Api.hasKitKat()){
            setTheme(R.style.AppTheme_NoActionBar_Translucent);
        }
        setUpDefaultStatusBar();
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evolution);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        int SPLASH_TIME_OUT = 5000;
        new Handler().postDelayed(new Runnable() {

                /*
                 * Showing splash screen with a timer. This will be useful when you
                 * want to show case your app logo / company
                 */

                @Override
                public void run()
                {
                    // This method will be executed once the timer is over
                    // Start your app main activity
                    ApplicationActivity.start(EvolutionActivity.this);
                    EvolutionActivity.this.finish();
                }
            }, SPLASH_TIME_OUT);
        changeActionBarColor();
    }

  
    private Drawable oldBackground;
    private void changeActionBarColor() {

        int color = Settings.getPrimaryColor(this);
        Drawable colorDrawable = new ColorDrawable(color);

        if (oldBackground == null) {
            getSupportActionBar().setBackgroundDrawable(colorDrawable);
        } else {
            TransitionDrawable td = new TransitionDrawable(new Drawable[] { oldBackground, colorDrawable });
            getSupportActionBar().setBackgroundDrawable(td);
            td.startTransition(200);
        }

        oldBackground = colorDrawable;

        setUpStatusBar();
    }
    
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void setUpStatusBar() {
        int color = Api.getStatusBarColor(Settings.getPrimaryColor(this));
        if(Api.hasLollipop()){
            getWindow().setStatusBarColor(color);
        }
        else if(Api.hasKitKat()){
            SystemBarTintManager systemBarTintManager = new SystemBarTintManager(this);
            systemBarTintManager.setTintColor(color);
            systemBarTintManager.setStatusBarTintEnabled(true);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void setUpDefaultStatusBar() {
        int color = ContextCompat.getColor(this, android.R.color.black);
        if(Api.hasLollipop()){
            getWindow().setStatusBarColor(color);
        }
        else if(Api.hasKitKat()){
            SystemBarTintManager systemBarTintManager = new SystemBarTintManager(this);
            systemBarTintManager.setTintColor(Api.getStatusBarColor(color));
            systemBarTintManager.setStatusBarTintEnabled(true);
        }
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    public String getTag()
    {
        return TAG;
    }
}

