package com.video.evolution.application;

import android.annotation.TargetApi;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Build;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import java.util.List;


import com.video.evolution.R;
import com.video.evolution.engine.Api;
import com.video.evolution.engine.app.folders.DirectoryPreference;
import com.video.evolution.engine.app.settings.Settings;
import com.video.evolution.engine.app.settings.AppSettings;
import com.video.evolution.engine.graphics.SystemBarTintManager;
import com.video.evolution.application.settings.AppCompatPreferenceActivity;
import com.video.evolution.application.analytics.AnalyticsManager;

import static com.video.evolution.engine.app.settings.Settings.KEY_ACCENT_COLOR;
import static com.video.evolution.engine.app.settings.Settings.KEY_PRIMARY_COLOR;
import static com.video.evolution.engine.app.settings.Settings.KEY_THEME_STYLE;

public class SettingsActivity extends AppCompatPreferenceActivity {
    private static final String TAG = SettingsActivity.class.getSimpleName();
    private Resources res;
    private int actionBarColor;
    private Drawable oldBackground;
    private boolean mRecreate = false;
    private AppSettings settings;
    
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int color = Api.getStatusBarColor(Settings.getPrimaryColor(this));
        if (Api.hasLollipop())
        {
            getWindow().setStatusBarColor(color);
        }
        else if (Api.hasKitKat())
        {
            SystemBarTintManager systemBarTintManager = new SystemBarTintManager(this);
            systemBarTintManager.setTintColor(Api.getStatusBarColor(color));
            systemBarTintManager.setStatusBarTintEnabled(true);
        }
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        changeActionBarColor(0);
        res = getResources();
        actionBarColor = Settings.getPrimaryColor(this);
        
        // load settings fragment
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MainPreferenceFragment()).commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        changeActionBarColor(0);
    }
   
    @Override
    public void recreate() {
        mRecreate = true;
        super.recreate();
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivityForResult(intent, Settings.FRAGMENT_OPEN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Settings.FRAGMENT_OPEN) {
            if (resultCode == RESULT_FIRST_USER) {
                recreate();
            }
        }
    }

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(Settings.EXTRA_RECREATE, true);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        if (state.getBoolean(Settings.EXTRA_RECREATE)) {
            setResult(RESULT_FIRST_USER);
        }
    }

    public void changeActionBarColor(int newColor) {

        int color = newColor != 0 ? newColor : Settings.getPrimaryColor(this);
        Drawable colorDrawable = new ColorDrawable(color);

        if (oldBackground == null) {
            getSupportActionBar().setBackgroundDrawable(colorDrawable);

        } else {
            TransitionDrawable td = new TransitionDrawable(new Drawable[] { oldBackground, colorDrawable });
            getSupportActionBar().setBackgroundDrawable(td);
            td.startTransition(200);
        }

        oldBackground = colorDrawable;
    }

    public static void logSettingEvent(String key) {
        AnalyticsManager.logEvent("settings_" + key.toLowerCase());
    }

    public static class MainPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {
        private AppSettings settings;
        @Override
        public void onCreate(final Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_main);

            // notification preference change listener
            bindPreferenceSummaryToValue(findPreference(getString(R.string.key_notifications_new_message_ringtone)));
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(sharedPrefsChangeListener);

            Preference preferencePrimaryColor = findPreference(KEY_PRIMARY_COLOR);
            preferencePrimaryColor.setOnPreferenceChangeListener(this);
            preferencePrimaryColor.setOnPreferenceClickListener(this);

            findPreference(KEY_ACCENT_COLOR).setOnPreferenceClickListener(this);

            Preference preferenceThemeStyle = findPreference(KEY_THEME_STYLE);
            preferenceThemeStyle.setOnPreferenceChangeListener(this);
            preferenceThemeStyle.setOnPreferenceClickListener(this);
            // feedback preference click listener
            Preference myPref = findPreference(getString(R.string.key_send_feedback));
            myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference preference) {
                        sendFeedback(getActivity());
                        return true;
                    }
                });
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            SettingsActivity.logSettingEvent(preference.getKey());
            ((SettingsActivity)getActivity()).changeActionBarColor(Integer.valueOf(newValue.toString()));
            getActivity().recreate();
            return true;
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            SettingsActivity.logSettingEvent(preference.getKey());
            return false;
        } 

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            settings = AppSettings.getSettings(getActivity());

            Preference storePathPrefs = findPreference(getString(R.string.key_gallery_name));
            /*storePathPrefs.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener(){

                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        //SettingsActivity.logSettingEvent(preference.getKey());
                        //((SettingsActivity)getActivity()).setDefaultDir(newValue.toString());
                        getActivity().recreate();
                        return true;
                    }
                    
            });*/   
            storePathPrefs.setSummary(settings.getStorePath());
        }

        @Override
        public void onPause() {
            super.onPause();  
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(sharedPrefsChangeListener);
        }

        @Override
        public void onResume() {
            super.onResume();
            settings.load();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(sharedPrefsChangeListener);
        }

        private final SharedPreferences.OnSharedPreferenceChangeListener sharedPrefsChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                settings.load();
            }
        };
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }


    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                                                                 PreferenceManager
                                                                 .getDefaultSharedPreferences(preference.getContext())
                                                                 .getString(preference.getKey(), ""));
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String stringValue = newValue.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                    index >= 0
                    ? listPreference.getEntries()[index]
                    : null);

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                        preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(R.string.summary_choose_ringtone);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else {
                preference.setSummary(stringValue);
            }
            return true;
        }
    };


    /**
     * Email client intent to send support mail
     * Appends the necessary device information to email body
     * useful when providing support
     */
    public static void sendFeedback(Context context) {
        String body = null;
        try {
            body = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            body = "\n\n-----------------------------\nPlease don't remove this information\n Device OS: Android \n Device OS version: " +
                Build.VERSION.RELEASE + "\n App Version: " + body + "\n Device Brand: " + Build.BRAND +
                "\n Device Model: " + Build.MODEL + "\n Device Manufacturer: " + Build.MANUFACTURER;
        } catch (PackageManager.NameNotFoundException e) {
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"contact@androidhive.info"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Query from android app");
        intent.putExtra(Intent.EXTRA_TEXT, body);
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.choose_email_client)));
    }
}
