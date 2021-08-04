package com.video.evolution.engine.app.settings;

import android.app.Activity;
import android.app.Application;
import android.preference.PreferenceManager;
import com.video.evolution.EvolutionApplication;

public class AppSettings extends Settings
{

    public static AppSettings getSettings(Activity activity)
    {
        return getSettings(activity.getApplication());
    }

    public static AppSettings getSettings(Application application)
    {
        return ((EvolutionApplication) application).settings;
    }

    private final EvolutionApplication application;

    public AppSettings(EvolutionApplication application)
    {
        this.application = application;
    }

    public String storePath(){
        return getStorePath();
    }

    public void load()
    {
        load(PreferenceManager.getDefaultSharedPreferences(application));
    }

    public void save()
    {
        save(PreferenceManager.getDefaultSharedPreferences(application));
    }

    public void saveDeferred()
    {
        saveDeferred(PreferenceManager.getDefaultSharedPreferences(application));
    }
}



