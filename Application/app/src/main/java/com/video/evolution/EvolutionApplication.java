package com.video.evolution;

import android.app.Application;
import android.app.Activity;
import android.content.Context;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.video.evolution.engine.Engine;
import com.video.evolution.engine.app.settings.AppSettings;
import com.video.evolution.engine.widget.soundPool.ISoundPoolLoaded;
import com.video.evolution.engine.widget.soundPool.SoundPoolManager;
import com.video.evolution.application.analytics.AnalyticsManager;

public class EvolutionApplication extends Application {
    private static EvolutionApplication sInstance;
    public final AppSettings settings = new AppSettings(this);
    private static Context mContext;
    
    @Override
    public void onCreate() {
        super.onCreate();
           if (!BuildConfig.DEBUG) {
            AnalyticsManager.intialize(getApplicationContext());
        }
        Engine.initDefault(new Engine.Builder(getApplicationContext())
                           .setFolder(getApplicationContext(), settings)
                           .setDownloadFile(getApplicationContext())
                           .build());
        sInstance = this;
        mContext = getApplicationContext();                       
        SoundPool();
	}
    
    public static synchronized EvolutionApplication getInstance() {
        return sInstance;
    } 

    public static Context getContext() {
        return mContext;
    }
    
    public void SoundPool() {
        SoundPoolManager.CreateInstance();
        List<Integer> sounds = new ArrayList<Integer>();
        sounds.add(R.raw.sound1);
        sounds.add(R.raw.sound2);
        SoundPoolManager.getInstance().setSounds(sounds);
        try {
            SoundPoolManager.getInstance().InitializeSoundPool(getApplicationContext(), new ISoundPoolLoaded() {
                    @Override
                    public void onSuccess() {

                    }
                });
        } catch (Exception e) {
            e.printStackTrace();
        }

        SoundPoolManager.getInstance().setPlaySound(true);
    }
}
