package com.video.evolution.engine.app.settings;

import android.support.v7.app.AppCompatDelegate;
import android.support.v4.text.TextUtilsCompat;
import android.annotation.TargetApi;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.preference.PreferenceManager;

import com.video.evolution.R;
import com.video.evolution.EvolutionApplication;

public class Settings
{
    private static final String INITIALIZED_KEY = "initialized";
    private static final String STORE_PATH_KEY = "key_gallery_name";
    public static final int FRAGMENT_OPEN = 99;
    public static final String EXTRA_RECREATE = "recreate";
    public static final String KEY_ROOT_MODE = "rootMode";
    public static final String KEY_PRIMARY_COLOR = "primaryColor";
    public static final String KEY_ACCENT_COLOR = "accentColor";
    public static final String KEY_THEME_STYLE = "themeStyle";
    public static final String KEY_FOLDER_ANIMATIONS = "folderAnimations";
    public static final String KEY_RECENT_MEDIA = "recentMedia";

    private boolean initialized;
    private static String storePath;

    public boolean isInitialized()
    {
        return initialized;
    }

    public void setInitialized(boolean initialized)
    {
        this.initialized = initialized;
    }

    public String getStorePath()
    {
        return storePath;
    }

    public void setStorePath(String storePath)
    {
        this.storePath = storePath;
    }

    public static boolean getRootMode(Context context)
    {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(KEY_ROOT_MODE, true);
    }

    public static int getPrimaryColor(Context context)
    {
        int newColor = ContextCompat.getColor(context, R.color.defaultColor);
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getInt(KEY_PRIMARY_COLOR, newColor);
    }

    public static int getPrimaryColor()
    {
        return PreferenceManager.getDefaultSharedPreferences(EvolutionApplication.getInstance().getBaseContext())
            .getInt(KEY_PRIMARY_COLOR, Color.parseColor("#0288D1"));
    }

    public static int getAccentColor()
    {
        return PreferenceManager.getDefaultSharedPreferences(EvolutionApplication.getInstance().getBaseContext())
            .getInt(KEY_ACCENT_COLOR, Color.parseColor("#EF3A0F"));
    }

    public static void setAccentColor(int color)
    {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(EvolutionApplication.getInstance().getBaseContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_ACCENT_COLOR, color);
        editor.commit();
    }

    public static String getThemeStyle()
    {
        return PreferenceManager.getDefaultSharedPreferences(EvolutionApplication.getInstance().getBaseContext())
            .getString(KEY_THEME_STYLE, "1");
    }

    public static void changeThemeStyle(AppCompatDelegate delegate)
    {
        int nightMode = Integer.valueOf(getThemeStyle());
        AppCompatDelegate.setDefaultNightMode(nightMode);
        delegate.setLocalNightMode(nightMode);
    }

    public void load(SharedPreferences prefs)
    {
        initialized = prefs.getBoolean(INITIALIZED_KEY, false);
        storePath = prefs.getString(STORE_PATH_KEY, null);
    }

    public void save(SharedPreferences prefs)
    {
        SharedPreferences.Editor editor = prefs.edit();
        save(editor);
        editor.commit();
    }

    public void saveDeferred(SharedPreferences prefs)
    {
        SharedPreferences.Editor editor = prefs.edit();
        save(editor);
        editor.apply();
    }

    public void save(SharedPreferences.Editor editor)
    {
        editor.putBoolean(INITIALIZED_KEY, initialized);
        editor.putString(STORE_PATH_KEY, storePath);
    }
}



