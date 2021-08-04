package com.video.evolution.application.analytics;

import com.video.evolution.BuildConfig;

public class CrashReportingManager {

    public static void logException(Exception e) {
        logException(e, false);
    }

    public static void logException(Exception e, boolean log) {
        if(BuildConfig.DEBUG){
            e.printStackTrace();
        } else if(log) {
        }
    }
}



