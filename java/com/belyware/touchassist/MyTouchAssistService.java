package com.belyware.touchassist;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.graphics.Path;
import android.os.Handler;
import android.accessibilityservice.AccessibilityService;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class MyTouchAssistService extends AccessibilityService  {
    private static String TAG = "BelyTouchAssistService";

    private static final int MSG_BASE = 1000;
    private static final int MSG_SEND_TOUCH_EVENT = MSG_BASE + 1;
    private static final int TIMER_DELAY_SEND_TOUCH_EVENT = 20000;
    private static final int MSG_EXECUTE_TASK = MSG_BASE + 2;

    public static Handler mHandler;
    public static boolean mRunning = false;
    public static boolean mServiceConnected = false;

    private int mCurStep = 0;
    public static boolean mRestartAction = false;
    private TouchConfigItem mCurrentConfig;
    private static TimerTaskListener mTimerTaskListener;
    public static MyTouchAssistService mTouchServiceInstance = null;
    public interface TimerTaskListener {
        void onTimeredClick(int x, int y);
        void onTimeredSwipe(int x1, int y1, int x2, int y2);
    }

    public MyTouchAssistService() {
    }

    public static void setTimerTaskListener(TimerTaskListener listener) {
        mTimerTaskListener = listener;
    }

    public void pause() {
        mHandler.removeCallbacksAndMessages(null);
    }

    public void stop() {
        pause();
        //disableSelf();
    }

    public void start() {
        mRestartAction = true;
        initTouchMsg();
    }

    public static boolean isServiceRunning() {
        if (mTouchServiceInstance == null) {
            return false;
        }
        AccessibilityManager accessibilityManager = (AccessibilityManager) mTouchServiceInstance.getSystemService(Context.ACCESSIBILITY_SERVICE);
        AccessibilityServiceInfo info = mTouchServiceInstance.getServiceInfo();
        if (info == null) {
            return false;
        }
        List<AccessibilityServiceInfo> list = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        Iterator<AccessibilityServiceInfo> iterator = list.iterator();

        boolean isConnect = false;
        while (iterator.hasNext()) {
            AccessibilityServiceInfo i = iterator.next();
            if (i.getId().equals(info.getId())) {
                isConnect = true;
                break;
            }
        }
        if (!isConnect) {
            return false;
        }
        return true;
    }

    @Override
    protected void onServiceConnected() {
//        setServiceInfo();
        super.onServiceConnected();
        Log.i(TAG, "onServiceConnected");
//        HandlerThread eventInjectionThread = new HandlerThread("MyTouchAssistService");
//        eventInjectionThread.start();
        mHandler = new Handler(Looper.getMainLooper(),
                new TouchAssistHandler());

        mServiceConnected = true;
        mTouchServiceInstance = this;
    }

    private class TouchAssistHandler implements Handler.Callback {
        @Override
        public boolean handleMessage(Message msg) {

            Log.d(TAG, "TouchAssistHandler Received "+msg.what);

            switch(msg.what) {
                case MSG_EXECUTE_TASK:
                    runTask();
                    break;
            }

            return true;
        }
    }

    private void initTouchMsg() {
        mHandler.sendEmptyMessageDelayed(MSG_EXECUTE_TASK, 1000);
    }


    private void runTask() {


        if (mCurrentConfig != null && mCurrentConfig.mActions != null) {
            TouchConfigItem.ActionItem item;
            if (mCurStep >= mCurrentConfig.mActions.size()) {
                mCurStep = 0;
                if (mCurrentConfig.mRepeatType == TouchConfigItem.REPEAT_NONE) {
                    return;
                }
            }
            item = mCurrentConfig.mActions.get(mCurStep ++);
            if (item != null) {
                switch (item.mAction) {
                    case TouchConfigItem.ACTION_CLICK:
                        if (mTimerTaskListener != null) {
                            mTimerTaskListener.onTimeredClick(item.mX1, item.mY1);
                        }
                        sendClickEvent(item.mX1, item.mY1);
                        mHandler.sendEmptyMessage(MSG_EXECUTE_TASK);
                        break;
                    case TouchConfigItem.ACTION_DELAY:
                        int delaytime = 0;
                        if (item.mRandom) {
                            Random r = new Random();
                            int distance = Math.abs(item.mRandDelay_High - item.mRandDelay_Low);
                            if (distance == 0) {
                                delaytime = 30;//set a default timer in case any issues
                            }
                            delaytime = r.nextInt(distance) + (item.mRandDelay_Low<=item.mRandDelay_High?item.mRandDelay_Low:item.mRandDelay_High);
                            Log.i(TAG,"this time delay : " + delaytime + " s");
                        } else {
                            delaytime = item.mDelayTime;
                        }
                        Menu.startTimerShow(delaytime);
                        mHandler.sendEmptyMessageDelayed(MSG_EXECUTE_TASK, delaytime*1000);
                        break;
                    case TouchConfigItem.ACTION_LONGCLICK:
                        sendLongClickEvent();
                        mHandler.sendEmptyMessage(MSG_EXECUTE_TASK);
                        break;
                    case TouchConfigItem.ACTION_SWIPE:
                        if (mTimerTaskListener != null) {
                            mTimerTaskListener.onTimeredSwipe(item.mX1, item.mY1, item.mX2, item.mY2);
                        }
                        sendSwipeEvent(item.mX1, item.mY1, item.mX2, item.mY2);
                        mHandler.sendEmptyMessage(MSG_EXECUTE_TASK);
                        break;
                    case TouchConfigItem.ACTION_DOUBLECLICK:
                        sendClickEvent(item.mX1, item.mY1);
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        sendClickEvent(item.mX1, item.mY1);
                        mHandler.sendEmptyMessage(MSG_EXECUTE_TASK);
                        break;
                }
            }
        }
    }

    public void sendClickEvent(int x, int y) {
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        Path path = new Path();

        path.moveTo(x, y + Utils.mStatusBarHeight);
        path.lineTo(x, y + Utils.mStatusBarHeight);
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(path, 0, 200));

        boolean ret = dispatchGesture(gestureBuilder.build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                Log.i(TAG,"Gesture Completed");
                super.onCompleted(gestureDescription);
            }
        }, null);
    }



    private void sendLongClickEvent() {

    }

    public void sendSwipeEvent(int x1, int y1, int x2, int y2) {
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        Path path = new Path();

        path.moveTo(x1, y1 + Utils.mStatusBarHeight);
        path.lineTo(x2, y2 + Utils.mStatusBarHeight);
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(path, 0, 400));

        boolean ret = dispatchGesture(gestureBuilder.build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                Log.i(TAG,"Gesture Completed");
                super.onCompleted(gestureDescription);
            }
        }, null);
    }

    private void createEvent() {
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        Path path = new Path();

        path.moveTo(300, 1500);
        path.lineTo(300, 200);
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(path, 0, 400));

        boolean ret = dispatchGesture(gestureBuilder.build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                Log.i(TAG,"Gesture Completed");
                super.onCompleted(gestureDescription);
            }
        }, null);
//        Toast.makeText(this, "moving..."+ret, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) {
            return;
        }
        String pkgName = event.getPackageName().toString();
        int eventType = event.getEventType();

        Log.i(TAG, "eventType: " + eventType + " pkgName: " + pkgName);
        switch (eventType) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                break;
            case AccessibilityEvent.TYPE_GESTURE_DETECTION_START:
                break;
            case AccessibilityEvent.TYPE_GESTURE_DETECTION_END:
                break;
        }
    }

    @Override
    public void onInterrupt() {
        Log.i(TAG, "onInterrupt");
    }


    private void setServiceInfo() {
        String[] packageInfo = {"com.android.settings"};
        AccessibilityServiceInfo mServiceInfo = new AccessibilityServiceInfo();
        mServiceInfo.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        mServiceInfo.packageNames = null;//packageInfo;

        mServiceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
        mServiceInfo.notificationTimeout = 100;
        reflectCapbility(mServiceInfo);
        setServiceInfo(mServiceInfo);
    }

    private void reflectCapbility(AccessibilityServiceInfo mServiceInfo){
        try {
            Method setCapabilityMethod;
            String methodName = "setCapabilities";
            setCapabilityMethod = AccessibilityServiceInfo.class.getMethod(
                    methodName, new Class[]{Integer.TYPE});

            int cap = mServiceInfo.getCapabilities();
            cap |= AccessibilityServiceInfo.CAPABILITY_CAN_PERFORM_GESTURES;

            setCapabilityMethod.invoke(mServiceInfo, cap);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
