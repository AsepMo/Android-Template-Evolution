package com.video.evolution.application;

import android.content.Intent;
import android.content.Context;

public class Application {

    private static volatile Application Instance = null;
    public static Application getInstance() {
        Application localInstance = Instance;
        if (localInstance == null) {
            synchronized (Application.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new Application();
                }
            }
        }
        return localInstance;
    }

    public void exitApplication(Context c) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        c.startActivity(intent);
    }

}
