package com.belyware.touchassist;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.WindowManager;
import android.util.DisplayMetrics;

import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Locale;


public class Utils {
    public static final int MAX_CUSTOM_BUTTON_NUM = 3;
    public static final int MAX_BUTTON_CONFIG_NUM = 2;
    public static final int MAX_AUTOCLICK_CONFIG_NUM = 2;
    private static final String TAG = "TouchAssistUtils";

    public static int LOGO_ICON_WIDTH = 60;
    public static int LOGO_ICON_HEIGHT = 60;
    public static int LOGO_ICON_MARGIN = 20;

    public static int TOUCH_ICON_WIDTH = 40;
    public static int TOUCH_ICON_HEIGHT = 40;
    public static int SWIPE_LINE_WIDTH = 5;
    public static int mXOffset;
    public static int mYOffset;
    public static int mStatusBarHeight;

    public static boolean mInitialDrawLine = false;
    public static float mDesity;
    public static int mScreenWidth;
    public static int mScreenHeight;
    public static int screenWithInPixel,screenHeightInPixel;
    public static int mCurrentActiveConfigIndex;
    public static LinearLayout mMainLayout;
    public static ArrayList<TouchConfigItem> mAllConfigList = null;
    public static boolean isCN = (Locale.getDefault().getLanguage().equalsIgnoreCase("zh")?true:false);
    public static void getAndroiodScreenProperty(Context context) {

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;         // 屏幕宽度（像素）
        int height = dm.heightPixels;       // 屏幕高度（像素）
        float density = dm.density;         // 屏幕密度（0.75 / 1.0 / 1.5）
        int densityDpi = dm.densityDpi;     // 屏幕密度dpi（120 / 160 / 240）
        // 屏幕宽度算法:屏幕宽度（像素）/屏幕密度
        int screenWidth = (int) (width / density);  // 屏幕宽度(dp)
        int screenHeight = (int) (height / density);// 屏幕高度(dp)

        mScreenWidth = screenWidth;
        mScreenHeight = screenHeight;
        mDesity = density;

        Log.d(TAG, "screenwidth(pixel):" + width);
        Log.d(TAG, "screenheight(pixel)" + height);
        Log.d(TAG, "density:" + density);
        Log.d(TAG, "dpi" + densityDpi);
        Log.d(TAG, "screenwidth(dp):" + screenWidth);
        Log.d(TAG, "screenheight(dp):" + screenHeight);
        screenWithInPixel = width;
        screenHeightInPixel = height;

         mXOffset = (int)((Utils.TOUCH_ICON_WIDTH-Utils.SWIPE_LINE_WIDTH)/2 * Utils.mDesity);
         mYOffset = (int)((Utils.TOUCH_ICON_HEIGHT-(Utils.SWIPE_LINE_WIDTH/2))*Utils.mDesity);
    }


    private static boolean mBound;
    private static MainService mService;
    private static boolean mBound2;
    private static  MainService.MyBinder mybinder;

    private static ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            mybinder = (MainService.MyBinder) service;
            mService = mybinder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    public static void connectService1(Context mContext) {
        if (!mBound) {
            Intent intent = new Intent(mContext, MainService.class);
            mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }


    public static void shutdown() {
        if (MyTouchAssistService.mTouchServiceInstance != null) {
            MyTouchAssistService.mTouchServiceInstance.stop();
        }
        if (mybinder != null) {
            mybinder.shutdown();
        }
    }

    public static void showOtherButtons(boolean show) {
        if (mybinder != null) {
            mybinder.showOtherButtons(show);
        }
    }

    public static void showClickIconForCustomButton(int x, int y, int tag, String name) {
        showClickIconForCustomButton(x, y, tag, name, false);
    }
    public static void showClickIconForCustomButton(int x, int y, int tag, String name, boolean forceClose) {
        if (mybinder != null) {
            mybinder.showClickIconForCustomButton(x, y, tag, name, forceClose);
        }
    }

    public static void showClickIcon(boolean show, int x, int y, int tag) {
        if (mybinder != null) {
            mybinder.showClickButton(show, x, y, tag);
        }
    }

    public static void showSwipeButtonsForCustomButton(boolean show, int x1, int y1, int x2, int y2, int tag) {
        if (mybinder != null) {
            mybinder.showSwipeButtonsForCustomButton(show, x1, y1, x2, y2, tag);
        }
    }

    public static void showSwipeIcon(boolean show, int x1, int y1, int x2, int y2) {
        if (mybinder != null) {
            mybinder.showSwipeButtons(show, x1, y1, x2, y2);
        }
    }
    public static void hideTouchIcons() {
        if (mybinder != null) {
            mybinder.showSwipeButtons(false, 0, 0, 0, 0);
        }
    }

    public static void drawSwipeLine(int x1, int y1, int x2, int y2) {
        if (mybinder != null) {
            mybinder.drawSwipeLine(x1, y1, x2, y2);
        }
    }
    public static void showCustomButton(int x, int y, int width, int height, String name, int tag, CustomButtonConfigInfo.ButtonItem info) {
        showCustomButton(x, y, width, height, name, tag, info,false);
    }
    public static void showCustomButton(int x, int y, int width, int height, String name, int tag, CustomButtonConfigInfo.ButtonItem info, boolean forceClose) {
        if (mybinder != null) {
            mybinder.showCustomeButton(x, y, width, height, name, tag, info, forceClose);
        }
    }

    public static void addRemoveClickChangeListener(MainService.ClickPosChangeListener listener, boolean add) {
        if (mybinder != null) {
            if (add) {
                mybinder.addClicPosChangeListener(listener);
            } else {
                mybinder.removeClicPosChangeListener(listener);
            }
        }
    }



    public interface CustomButtonChangeListener {
        void onButtonPosChanged(int tag, int x, int y);
        void onButtonSizeChanged(int tag, int w, int h);
    }

    public static void registerButtonChangeListener(CustomButtonChangeListener listener) {
        if (mybinder != null) {
            mybinder.registerButtonChangeListener(listener);
        }
    }

    public static void hideAllCustomBtns() {
        if (mybinder != null) {
            mybinder.hideAllCustomBtns();
        }
    }
}
