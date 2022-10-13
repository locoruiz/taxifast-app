package com.vitalsoftware.taxifast;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

/**
 * Created by titin on 8/22/16.
 */
public class App extends MultiDexApplication {
    private static Context mContext;

    public App() {
        super();
        mContext = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static Context getContext(){
        return mContext;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
        mContext = base;
    }
}
