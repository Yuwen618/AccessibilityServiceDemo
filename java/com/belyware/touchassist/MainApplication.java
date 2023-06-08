package com.belyware.touchassist;

import android.app.Application;
import android.content.Context;


public class MainApplication extends Application {
    public static Context mMyContext;
    @Override
    public void onCreate() {
        super.onCreate();
        mMyContext = getApplicationContext();
    }

}
