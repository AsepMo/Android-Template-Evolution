package com.video.evolution.application;

import android.annotation.TargetApi;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.ClipData;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.PreferenceManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.ArrayList;
import java.io.File;

import com.video.evolution.R;
import com.video.evolution.application.updater.Updater;
import com.video.evolution.application.updater.UpdateModel;
import com.video.evolution.application.updater.UpdateListener;
import com.video.evolution.application.updater.UpdaterService;
import com.video.evolution.application.updater.UpgradeUtil;
import com.video.evolution.application.player.VideoPlayerFragment;
import com.video.evolution.application.library.VideoLibraryFragment;
import com.video.evolution.application.folder.VideoFolderFragment;
import com.video.evolution.application.download.VideoDownloadFragment;
import com.video.evolution.engine.Api;
import com.video.evolution.engine.app.ActionBarActivity;
import com.video.evolution.engine.app.settings.Settings;
import com.video.evolution.engine.app.settings.AppSettings;
import com.video.evolution.engine.app.menu.DrawerAdapter;
import com.video.evolution.engine.app.menu.DrawerItem;
import com.video.evolution.engine.app.menu.SimpleItem;
import com.video.evolution.engine.app.menu.SpaceItem;
import com.video.evolution.engine.graphics.SystemBarTintManager;
import com.video.evolution.engine.widget.VideoLayout;
import com.video.evolution.engine.widget.SlidingRootNav;
import com.video.evolution.engine.widget.SlidingRootNavBuilder;

public class ApplicationActivity extends ActionBarActivity implements DrawerAdapter.OnItemSelectedListener {

    private static final String TAG = ApplicationActivity.class.getSimpleName();
    public static void start(Context c)
    {
        Intent mApplication = new Intent(c, ApplicationActivity.class);
        c.startActivity(mApplication);
    }
    
    private static final int POS_VIDEO_PLAYER = 0;
    private static final int POS_VIDEO_LIBRARY = 1;
    private static final int POS_VIDEO_FOLDER = 2;
    private static final int POS_VIDEO_DOWNLOAD = 3;
    private static final int POS_LOGOUT = 5;

    private String[] screenTitles;
    private Drawable[] screenIcons;

    private VideoLayout mVideoLayout;
    private SlidingRootNav slidingRootNav;
    public static final String UPGRADE = "upgrade";
    private String mDirPath;
    private UpdaterService loadFileService;
    private boolean isRegisteredService;
    private AppSettings settings;
    private Handler mHandler = new Handler(); 
    private Runnable mFirstTimeRunner = new Runnable()
    {
        @Override 
        public void run() {
            mVideoLayout.setPathOrUrl("videolayout.mp4");     
        }
    }; 
    
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
        setUpStatusBar();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setSubtitle("The Best Of File Manager");
        setSupportActionBar(mToolbar);
        
        
        slidingRootNav = new SlidingRootNavBuilder(this)
            .withToolbarMenuToggle(mToolbar)
            .withMenuOpened(false)
            .withContentClickableWhenMenuOpened(false)
            .withSavedState(savedInstanceState)
            .withMenuLayout(R.layout.menu_left_drawer)
            .inject();

        mVideoLayout = (VideoLayout)findViewById(R.id.videoLayout);
        mHandler.postDelayed(mFirstTimeRunner, 200);
        
        screenIcons = loadScreenIcons();
        screenTitles = loadScreenTitles();

        DrawerAdapter adapter = new DrawerAdapter(Arrays.asList(
                                                      createItemFor(POS_VIDEO_PLAYER).setChecked(true),
                                                      createItemFor(POS_VIDEO_LIBRARY),
                                                      createItemFor(POS_VIDEO_FOLDER),
                                                      createItemFor(POS_VIDEO_DOWNLOAD),
                                                      new SpaceItem(48),
                                                      createItemFor(POS_LOGOUT)));
        adapter.setListener(this);

        RecyclerView list = findViewById(R.id.list);
        list.setNestedScrollingEnabled(false);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        adapter.setSelected(POS_VIDEO_PLAYER);
        settings = AppSettings.getSettings(this);
        mDirPath = createDownLoadUpgradeDir(getApplicationContext());
        /*new Updater(this, "https://asepmo-story.000webhostapp.com/updater/updater.json", new UpdateListener() {
                @Override
                public void onJsonDataReceived(final UpdateModel updateModel, JSONObject jsonObject) {
                    if (Updater.getCurrentVersionCode(ApplicationActivity.this) < updateModel.getVersionCode()) {
                        new AlertDialog.Builder(ApplicationActivity.this)
                            .setTitle(getString(R.string.actions_update))
                            .setCancelable(updateModel.isCancellable())
                            .setPositiveButton(getString(R.string.btn_update), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String url = updateModel.getUrl();
                                    Intent intent = new Intent(getApplicationContext(), UpdaterService.class);
                                    intent.putExtra("down_load_path", mDirPath);
                                    intent.putExtra("down_load_url", url);
                                    bindService(intent, serviceConnection, BIND_AUTO_CREATE);
                                }
                            })
                            .show();
                    }
                }

                @Override
                public void onError(String error) {
                    // Do something
                    
                }
			}).execute();*/
        changeActionBarColor(); 
    }

    @Override
    public void onItemSelected(int position) {

        if (position == POS_VIDEO_PLAYER) {
            showFragment(VideoPlayerFragment.createFor("Video Player"));
        }
        else if (position == POS_VIDEO_LIBRARY) {
            showFragment(VideoLibraryFragment.createFor("Video Library"));
        }
        else if (position == POS_VIDEO_FOLDER) {
            showFragment(VideoFolderFragment.createFor("Video Folder"));
        }
        else if (position == POS_VIDEO_DOWNLOAD) {
            showFragment(VideoDownloadFragment.createFor("Video Download"));
        }

        else if (position == POS_LOGOUT) {
            Application.getInstance().exitApplication(this);
        }
        slidingRootNav.closeMenu();
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.content_frame, fragment)
            .commit();
    }

    private DrawerItem createItemFor(int position) {
        return new SimpleItem(screenIcons[position], screenTitles[position])
            .withIconTint(color(R.color.textColorSecondary))
            .withTextTint(color(R.color.textColorPrimary))
            .withSelectedIconTint(color(R.color.colorAccent))
            .withSelectedTextTint(color(R.color.colorAccent));
    }

    private String[] loadScreenTitles() {
        return getResources().getStringArray(R.array.ld_activityScreenTitles);
    }

    private Drawable[] loadScreenIcons() {
        TypedArray ta = getResources().obtainTypedArray(R.array.ld_activityScreenIcons);
        Drawable[] icons = new Drawable[ta.length()];
        for (int i = 0; i < ta.length(); i++) {
            int id = ta.getResourceId(i, 0);
            if (id != 0) {
                icons[i] = ContextCompat.getDrawable(this, id);
            }
        }
        ta.recycle();
        return icons;
    }

    @ColorInt
    private int color(@ColorRes int res) {
        return ContextCompat.getColor(this, res);
    }

    @Override
    protected void onResume() {
        super.onResume();
        changeActionBarColor();
        mVideoLayout.onResumeVideoLayout(); 
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mFirstTimeRunner);
        mVideoLayout.onPauseVideoLayout(); 
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mFirstTimeRunner);
        mVideoLayout.onDestroyVideoLayout();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_about) {
            // launch about activity
            startActivity(new Intent(ApplicationActivity.this, AboutActivity.class));
            return true;
        }

        if (id == R.id.action_settings) {
            // launch settings activity
            startActivity(new Intent(ApplicationActivity.this, SettingsActivity.class));
            return true;
        } 
        
        if (id == R.id.action_exit) {
            // Exit Application
            Application.getInstance().exitApplication(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setStopDownload(){
        if (isRegisteredService) {
            unbindService(serviceConnection);
            isRegisteredService = false;
        }
    }
    public void setDeleteFile()
    {
        if (isRegisteredService) {
            unbindService(serviceConnection);
            isRegisteredService = false;
        }
        File dir = new File(mDirPath);
        UpgradeUtil.deleteContentsOfDir(dir);
    }

    private static String createDownLoadUpgradeDir(Context context) {
        String dir = null;
        final String dirName = UPGRADE;
        File root = null;
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            root = context.getExternalFilesDir(null);
        } else {
            root = context.getFilesDir();
        }
        File file = new File(root, dirName);
        file.mkdirs();
        dir = file.getAbsolutePath();
        return dir;
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            loadFileService = ((UpdaterService.LocalBinder) service).getService();
            isRegisteredService = true;

            loadFileService.setOnProgressChangeListener(new UpdaterService.onProgressChangeListener() {
                    @Override
                    public void onProgressChange(final int progress, final String message) {
                        /*handler.post(new Runnable() {
                         @Override
                         public void run() {
                         messageTextView.setText(message + ",Progress" + progress + "%");
                         }
                         });*/
                    }
                });
            Log.i(TAG, "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(TAG, "onServiceDisconnected");
            loadFileService = null;
            isRegisteredService = false;
        }
    };
    
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
        int color = ContextCompat.getColor(this, R.color.alertColor);
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
    
    
    @Override
    public void onBackPressed()
    {
        // TODO: Implement this method
        //super.onBackPressed();
        if (slidingRootNav.isMenuOpened())
        {
            slidingRootNav.closeMenu();
        }
        else
        {
            slidingRootNav.openMenu();
        }
	}
}

