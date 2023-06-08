package com.belyware.touchassist;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
//import android.support.v4.view.InputDeviceCompat;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class MainService extends Service implements MyTouchAssistService.TimerTaskListener {

    private static final String TAG = "MainService";

    private static final int MSG_BASE = 1000;
    private static final int MSG_SEND_TOUCH_EVENT = MSG_BASE + 1;
    private static final int TIMER_DELAY_SEND_TOUCH_EVENT = 10000;

    private static final int TIMER_REMOVE_CLICK_POINT = 500;

    private static final int TIMER_LAUNCH_ACCESSIBILITY_ACTIVITY = 500;

    private static final int ALL_MENU_BUTTON_COUNT = 5;
//    private static final int ALERT_WINDOW_TYPE = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;//
//            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
//                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
//                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

    private static final int ALERT_WINDOW_TYPE = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE);


    LinearLayout mLayout_Logo;
    WindowManager.LayoutParams mParams_Logo;
    WindowManager mWindowManager;

    Button mButton_Logo;
    //ImageButton mButton_Logo;
    ImageButton mButton_Pause;
    Button mButton_Config;
    Button mButton_Quit;

    Handler mMainHandler;

    //状态栏高度.
    int statusBarHeight = -1;

    private int lastX, startX;
    private int lastY, startY;

    private boolean isDrag;

    private int parentHeight;//悬浮的父布局高度
    private int parentWidth;
    //不与Activity进行绑定.

    private ArrayList<ClickPosChangeListener> mClickChangeListeners = new ArrayList<ClickPosChangeListener>();

    IBinder mBinder = new MyBinder();

    @Override
    public void onTimeredClick(int x, int y) {
        drawClickPoint(x, y);
    }

    @Override
    public void onTimeredSwipe(int x1, int y1, int x2, int y2) {
        drawSwipePoint(x1, y1, x2, y2);
    }

    public interface ClickPosChangeListener {
        void onClickPosChanged(int x, int y, int tag);
        void onSwipeEndChange(int x, int y, int tag);
    }



    @Override
    public IBinder onBind(Intent intent)
    {
        return mBinder;
    }

    public class MyBinder extends Binder {
        MainService getService() {
            return MainService.this;
        }
        public void shutdown() {
            MainService.this.stopSelf();
        }
        public void showOtherButtons(boolean show) {
            if (show) {
                mParams_Logo.width = (int)(Utils.mDesity * (Utils.LOGO_ICON_WIDTH*ALL_MENU_BUTTON_COUNT + ALL_MENU_BUTTON_COUNT * Utils.LOGO_ICON_MARGIN));
                mWindowManager.updateViewLayout(mLayout_Logo, mParams_Logo);
                mButton_Quit.setVisibility(View.VISIBLE);
                mButton_Config.setVisibility(View.VISIBLE);
            } else {
                mParams_Logo.width = (int)(Utils.mDesity * Utils.LOGO_ICON_WIDTH);
                mWindowManager.updateViewLayout(mLayout_Logo, mParams_Logo);
                mButton_Quit.setVisibility(View.GONE);
                mButton_Config.setVisibility(View.GONE);
            }
        }
        public void showClickIconForCustomButton(int x, int y, int tag, String name, boolean forceClose) {
            if (mWindowManager == null) {
                mWindowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
            }
            if (mIconListForCustomButton.get(tag) != null) {
                mWindowManager.removeView(mIconListForCustomButton.get(tag));
                mIconListForCustomButton.remove(tag);
                return;
            }
            if (forceClose) {
                return;
            }
            View icon = createClickIcon(x, y, tag, name);
            icon.setVisibility(View.VISIBLE);
            mIconListForCustomButton.put(tag, icon);
        }
        public void showClickButton(boolean show, int x, int y, int tag) {
            if (show) {
                mClickView = createClickIcon(x, y, tag);

                llparamClick.x = x;
                llparamClick.y = y;
                mClickView.setVisibility(View.VISIBLE);
                mWindowManager.updateViewLayout(mClickView,llparamClick);
                Log.d(TAG, "show click button at : x="+x + ", y="+y);
            } else {
                if (mClickView != null) {
                    mClickView.setVisibility(View.GONE);
                }
            }
        }
        public void showSwipeButtonsForCustomButton(boolean show, int x1, int y1, int x2, int y2, int tag) {
            if (!show) {
                clearDraw();
            }
            showClickButton(show, x1, y1, tag);
            if (show) {
                if (mSwipeDestView == null) {
                    mSwipeDestView = createSwipeEndIcon(x2, y2, tag);
                }
                llparamSwipeEnd.x = x2;
                llparamSwipeEnd.y = y2;
                mSwipeDestView.setVisibility(View.VISIBLE);
                mWindowManager.updateViewLayout(mSwipeDestView,llparamSwipeEnd);
                drawSwipeLine(x1,y1,x2,y2);

            } else {
                if (mSwipeDestView != null) {
                    mSwipeDestView.setVisibility(View.GONE);
                }
            }
        }

        public void showSwipeButtons(boolean show, int x1, int y1, int x2, int y2) {
            Utils.mInitialDrawLine = false;
            if (!show) {
                clearDraw();
            }
            showClickButton(show, x1, y1, 0);
            if (show) {
                if (mSwipeDestView == null) {
                    mSwipeDestView = createSwipeEndIcon(x2, y2, 0);
                }
                llparamSwipeEnd.x = x2;
                llparamSwipeEnd.y = y2;
                mSwipeDestView.setVisibility(View.VISIBLE);
                mWindowManager.updateViewLayout(mSwipeDestView,llparamSwipeEnd);
                drawSwipeLine(x1,y1,x2,y2);

            } else {
                if (mSwipeDestView != null) {
                    mSwipeDestView.setVisibility(View.GONE);
                }
            }
        }

        public void addClicPosChangeListener(ClickPosChangeListener listener) {
            if (!mClickChangeListeners.contains(listener)) {
                mClickChangeListeners.add(listener);
            }
        }

        public void removeClicPosChangeListener(ClickPosChangeListener listener) {
            mClickChangeListeners.remove(listener);
        }
        public void drawSwipeLine(int x1, int y1, int x2, int y2) {
            doDrawSwipeLine(x1, y1, x2, y2);
        }

        public void showCustomeButton(int x, int y, int width, int height, String name, int tag, CustomButtonConfigInfo.ButtonItem buttoninfo, boolean forceclose) {
            createCustomButton(x, y, width, height, name, tag, buttoninfo, forceclose);
        }
        public void registerButtonChangeListener(Utils.CustomButtonChangeListener listener) {
            registerBChangeListener(listener);
        }
        public void hideAllCustomBtns() {
            hideCustomBtns();
        }
    }
    private Bitmap baseBitmap;
    private Canvas canvas;
    private Paint paint;
    private ImageView swipeImage;
    private View mSwipeLineView;
    private void initPaint() {
        paint = new Paint();
        paint.setStrokeWidth(Utils.mDesity * 5);
        paint.setAntiAlias(true);
        paint.setColor(Color.RED);

        Log.d(TAG, "bitmap width="+Utils.mScreenWidth+",height="+Utils.mScreenHeight);
        baseBitmap = Bitmap.createBitmap(Utils.screenWithInPixel,
                Utils.screenHeightInPixel, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(baseBitmap);
        canvas.drawColor(Color.TRANSPARENT);
    }

    private void doDrawSwipeLine(int startX, int startY, int stopX, int stopY) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        if (!Utils.mInitialDrawLine) {
            canvas.drawLine(startX + Utils.mXOffset, startY + Utils.mYOffset, stopX + Utils.mXOffset, stopY + Utils.mYOffset, paint);
        } else {
            canvas.drawLine(startX, startY, stopX, stopY, paint);
        }
        swipeImage.setImageBitmap(baseBitmap);
        showDraw();
    }

    private void drawButtonRectangle(int x, int y, int w, int h) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setPathEffect(new DashPathEffect( new float [ ] { 5, 3 }, 0 ));
        canvas.drawRect(new Rect((x-4)<0?0:(x-4),
                y<0?0:y,
                (x+w + 4)>Utils.screenWithInPixel?Utils.screenWithInPixel:(x+w + 4),
                (y+h + 4)>Utils.screenHeightInPixel?Utils.screenHeightInPixel:(y+h+4)), paint);

        swipeImage.setImageBitmap(baseBitmap);
        showDraw();
        paint.setPathEffect(null);
    }

    private void showDraw() {
        mSwipeLineView.bringToFront();
        mSwipeLineView.setVisibility(View.VISIBLE);
    }
    private void clearDraw() {
        mSwipeLineView.setVisibility(View.GONE);
    }
    private Runnable mRemoveClickPointRunnable = new Runnable() {
        @Override
        public void run() {
            clearDraw();
        }
    };
    private void drawClickPoint(int x, int y) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        canvas.drawCircle(x, y, 5, paint);
        canvas.drawCircle(x, y, 25, paint);
        canvas.drawCircle(x, y, 45, paint);
        swipeImage.setImageBitmap(baseBitmap);
        showDraw();
        mMainHandler.postDelayed(mRemoveClickPointRunnable, TIMER_REMOVE_CLICK_POINT);
    }
    private void drawSwipePoint(int x1, int y1, int x2, int y2) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        canvas.drawCircle(x1, y1, 5, paint);
        canvas.drawCircle(x1, y1, 25, paint);
        canvas.drawCircle(x1, y1, 45, paint);

        canvas.drawCircle(x2, y2, 5, paint);
        canvas.drawCircle(x2, y2, 25, paint);
        canvas.drawCircle(x2, y2, 45, paint);

        swipeImage.setImageBitmap(baseBitmap);
        showDraw();
        mMainHandler.postDelayed(mRemoveClickPointRunnable, TIMER_REMOVE_CLICK_POINT);
    }
    @Override
    public void onCreate()
    {
        super.onCreate();
        Log.i(TAG,"MainService Created");
        Utils.getAndroiodScreenProperty(this);
        createToucher();
        //HandlerThread eventInjectionThread = new HandlerThread("EventInjectionThread");
       // eventInjectionThread.start();
       // mHandler = new Handler(eventInjectionThread.getLooper(),
        //        new TouchAssistHandler());
        //initTouchMsg();
        mMainHandler = new Handler();

        initPaint();
        createSwipeImg();
        clearDraw();
        //CustomButtonConfig.loadButtonConfig(this);
        MyTouchAssistService.setTimerTaskListener(this);
    }

    private void createSwipeImg() {
        WindowManager.LayoutParams lparams = new WindowManager.LayoutParams();
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        }

        lparams.type =  ALERT_WINDOW_TYPE;

        //设置效果为背景透明.
        lparams.format = PixelFormat.RGBA_8888;
        //设置flags.不可聚焦及不可使用按钮对悬浮窗进行操控.
        lparams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;

        //设置窗口初始停靠位置.
        lparams.gravity = Gravity.LEFT | Gravity.TOP;
        lparams.x = 0;
        lparams.y = 0;

        //设置悬浮窗口长宽数据.
//        lparams.width = Utils.mScreenWidth;
        //lparams.height = Utils.mScreenHeight;

        LayoutInflater inflater = LayoutInflater.from(getApplication());
        //获取浮动窗口视图所在布局.
        View llayout = (LinearLayout) inflater.inflate(R.layout.swipeimage,null);
        swipeImage = (ImageView)llayout.findViewById(R.id.img_swipeline);
        //添加toucherlayout
        mWindowManager.addView(llayout,lparams);
        mSwipeLineView = llayout;
    }

    View mClickView = null;
    View mSwipeDestView = null;
    WindowManager.LayoutParams llparamClick;
    WindowManager.LayoutParams llparamSwipeEnd;
    private View createClickIcon(int x, int y, int tag) {
        return createClickIcon(x, y, tag, null);
    }
    private View createClickIcon(int x, int y, int tag, String name) {
        llparamClick = new WindowManager.LayoutParams();
        llparamClick.x = x;
        llparamClick.y = y;
        View llayout = createFloatIcon(R.layout.click, llparamClick);
        ImageView clickIcon = (ImageView)llayout.findViewById(R.id.btniconclick);
        clickIcon.setOnTouchListener(new MyTouchListener(llayout, llparamClick, false, tag));
        clickIcon.setOnClickListener(onTouchIconClickListener);
        if (tag != 0 && name != null) {
            TextView txt_name = (TextView)llayout.findViewById(R.id.icon_name);
            txt_name.setVisibility(View.VISIBLE);
            txt_name.setText(name);
        }
        return llayout;
    }

    private View createSwipeEndIcon(int x, int y, int tag) {
        llparamSwipeEnd = new WindowManager.LayoutParams();
        llparamSwipeEnd.x = x;
        llparamSwipeEnd.y = y;
        View llayout = createFloatIcon(R.layout.swipeend, llparamSwipeEnd);
        ImageView clickIcon = (ImageView)llayout.findViewById(R.id.btnswipeend);
        clickIcon.setOnTouchListener(new MyTouchListener(llayout, llparamSwipeEnd, true, tag));
        clickIcon.setOnClickListener(onTouchIconClickListener);
        return llayout;
    }

    private View.OnClickListener onTouchIconClickListener = new View.OnClickListener()
    {

        @Override
        public void onClick(View view) {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.setClass(MainService.this, TouchConfigActivity.class);
            MainService.this.startActivity(intent);
        }
    };

    private void showConfigActivity() {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.setClass(MainService.this, TouchConfigActivity.class);
        MainService.this.startActivity(intent);
    }
    ConcurrentHashMap<Integer, View> customButtonList = new ConcurrentHashMap<>();
    ConcurrentHashMap<Integer, View> mIconListForCustomButton = new ConcurrentHashMap<>();
    Utils.CustomButtonChangeListener mButtonChangeListener = null;
    public void hideCustomBtns() {
        for (Integer tag : customButtonList.keySet()) {
            if (customButtonList.get(tag) != null) {
                mWindowManager.removeView(customButtonList.get(tag));
                customButtonList.remove(tag);
            }
        }
    }
    public void registerBChangeListener(Utils.CustomButtonChangeListener listener) {
        mButtonChangeListener = listener;
    }
    private void createCustomButton(int x, int y, int width, int height, String name, int tag, CustomButtonConfigInfo.ButtonItem buttoninfo, boolean forceClose) {
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        }
        if (customButtonList.get(tag) != null) {
            mWindowManager.removeView(customButtonList.get(tag));
            customButtonList.remove(tag);
            return;
        }
        if (forceClose) {
            return;
        }
        WindowManager.LayoutParams lparams = new WindowManager.LayoutParams();
        lparams.x = x;
        lparams.y = y;
        //设置type.系统提示型窗口，一般都在应用程序窗口之上.
        lparams.type = ALERT_WINDOW_TYPE;
        //设置效果为背景透明.
        lparams.format = PixelFormat.RGBA_8888;
        //设置flags.不可聚焦及不可使用按钮对悬浮窗进行操控.
        lparams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        //设置窗口初始停靠位置.
        lparams.gravity = Gravity.LEFT | Gravity.TOP;
//        lparams.x = 0;
//        lparams.y = 0;

        //设置悬浮窗口长宽数据.
        lparams.width = width;
        lparams.height = height;

        LayoutInflater inflater = LayoutInflater.from(getApplication());
        //获取浮动窗口视图所在布局.
        final View llayout =  inflater.inflate(R.layout.custombutton,null);
//        Button iv2 = (Button)llayout.findViewById(R.id.custBtn);
        //Button iv = (Button)llayout.findViewById(R.id.btn_custom);
        //iv2.setVisibility(View.GONE);
        //iv.setVisibility(View.VISIBLE);
        /*
        llayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mWindowManager.removeView(llayout);
                customButtonList.remove((int)view.getTag());
            }
        });*/
        llayout.setTag(tag);

        //添加toucherlayout
        Button btn = (Button)llayout.findViewById(R.id.custBtn);
        if (btn != null) {
            btn.setText(name);
            btn.setOnTouchListener(new CustomBtnTouchListener(btn, lparams, buttoninfo));
        }
        ImageView resizeIcon = llayout.findViewById(R.id.resizeicon);
        if (resizeIcon != null) {
            resizeIcon.setOnTouchListener(new CustomBtnResizeListener(btn, lparams));
        }
        ImageView closeIcon = llayout.findViewById(R.id.closeicon);
        if (closeIcon != null) {
            closeIcon.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mWindowManager.removeView(llayout);
                    customButtonList.remove((int)llayout.getTag());
                }
            });
        }

        mWindowManager.addView(llayout,lparams);
        //llayout.setVisibility(View.VISIBLE);
        //mWindowManager.updateViewLayout(iv2, lparams);
        customButtonList.put(tag, llayout);
    }

    //only for resize
    private class CustomBtnResizeListener implements View.OnTouchListener {
        private View mHost;
        private WindowManager.LayoutParams mLParams;
        private int mStartX, mStartY, mLastX, mLastY;
        private boolean mIsDrag, mTouchCenter;
        private Handler mHandler;
        public CustomBtnResizeListener(View host, WindowManager.LayoutParams lparams) {
            mHost = host;
            mLParams = lparams;
        }
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int rawX = (int) event.getRawX();
            int rawY = (int) event.getRawY();
            int[] location = new int[2] ;
            Log.i(TAG,"onTouch:event="+event.getAction()+",rawx="+rawX+",rawy="+rawY);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mHost.setPressed(true);//默认是点击事件
                    mIsDrag=true;//默认是非拖动而是点击事件

                    mStartX = mLastX=rawX;
                    mStartY = mLastY=rawY - statusBarHeight;

                    mHost.getLocationOnScreen (location);
                    int hostX = location[0] + (int)(mLParams.width/2);
                    int hostY = location[1] - statusBarHeight + (int)(mLParams.height/2);
                    mTouchCenter = Math.abs(hostX - mStartX) < 40 && Math.abs(hostY - mStartY) < 40;
                    Log.i(TAG, "w="+mLParams.width+"h="+mLParams.height+",desity="+Utils.mDesity+",locationx="+location[0]+",locationy="+location[1]);
                    Log.i(TAG,"down:x="+mStartX+",y="+mStartY + ",hostX="+hostX+",hostY="+hostY+"mTouchCenter="+mTouchCenter);
                    //mHandler.removeCallbacks(mPressNholdRunnable);
                    //mHandler.postDelayed(mPressNholdRunnable,  TIMER_LONG_PRESS);
                    break;
                case MotionEvent.ACTION_MOVE:
                    int dx=rawX-mStartX;
                    int dy=rawY-mStartY- statusBarHeight;
                    Log.i(TAG,"move:dx="+dx+",dy="+dy);
                    //这里修复一些华为手机无法触发点击事件
                    int distance= (int) Math.sqrt(dx*dx+dy*dy);
                    mIsDrag = distance>100;//只有位移大于0说明拖动了
                    if(!mIsDrag) {
                        Log.i(TAG,"move:abort");
                        break;
                    }
                    if (false) { //move position
                        mLParams.x = (int)(rawX - mLParams.width/2);
                        mLParams.y = (int)(rawY - statusBarHeight - mLParams.height/2);
                        mWindowManager.updateViewLayout(mHost, mLParams);
                        Log.i(TAG,"move:to="+rawX+","+rawY);
                        if (mButtonChangeListener != null) {
                            mButtonChangeListener.onButtonPosChanged((int)mHost.getTag(), mLParams.x, mLParams.y);
                        }
//                        drawButtonRectangle(mLParams.x, mLParams.y,
//                                mLParams.width, mLParams.height);
                        mWindowManager.updateViewLayout(mHost, mLParams);
                    } else { //change size

                        v.getLocationOnScreen (location);
                        mLParams.width = location[0]  - mLParams.x;
                        mLParams.height = location[1]  - mLParams.y;
                        Log.i(TAG, "rawX="+rawX+",rawY="+rawY+",mLParam.x="+mLParams.x+",mLParam.y="+mLParams.y+",w="+mLParams.width+",h="+mLParams.height);

                        if (mLParams.width <= 200 || mLParams.height <= 200) {
                            break;
                        }

                        mWindowManager.updateViewLayout(mHost, mLParams);
//                        drawButtonRectangle(mLParams.x, mLParams.y,
//                                mLParams.width, mLParams.height);

                    }

                    break;
                case MotionEvent.ACTION_UP:
                    /*
                    if (mLongPressed && !mTouchCenter) {
                        mWindowManager.updateViewLayout(mHost, mLParams);
                        if (mButtonChangeListener != null) {
                            Log.i(TAG,"size changed to : " + mLParams.width/Utils.mDesity + ", " +  (mLParams.height/Utils.mDesity));
                            mButtonChangeListener.onButtonSizeChanged((int)mHost.getTag(), (int) (mLParams.width), (int) (mLParams.height));
                        }
                    }*/
                    clearDraw();
                    //如果是拖动状态下即非点击按压事件
                    //mHandler.removeCallbacks(mPressNholdRunnable);
                    //mLongPressed = false;
                    mHost.setPressed(false);
                    dx=rawX-mStartX;
                    dy=rawY-mStartY- statusBarHeight;
                    distance= (int) Math.sqrt(dx*dx+dy*dy);
                    mIsDrag = distance>mDeltaToMove;
                    mStartX = 0;
                    mStartY = 0;
                    Log.i(TAG,"up:isDrag="+mIsDrag);

                    break;
            }

            return mIsDrag;
        }
    }

    //only for move
    private class CustomBtnTouchListener implements View.OnTouchListener {

        private View mHost;
        private CustomButtonConfigInfo.ButtonItem mButtonInfo;
        private WindowManager.LayoutParams mLParams;
        private int mStartX, mStartY, mLastX, mLastY;
        private boolean mIsDrag, mTouchCenter;
        private Handler mHandler;
        public CustomBtnTouchListener(View host, WindowManager.LayoutParams lparams, CustomButtonConfigInfo.ButtonItem buttonInfo) {
            mHost = host;
            mLParams = lparams;
            mButtonInfo = buttonInfo;
            mHandler = new Handler();
        }
        private Runnable mPressNholdRunnable = new Runnable() {
            @Override
            public void run() {
                drawButtonRectangle(mLParams.x, mLParams.y,
                        mLParams.width, mLParams.height);

            }
        };
        private Runnable mPerformButtonRunnable = new Runnable() {
            @Override
            public void run() {
                handleCustomButtonInfo(mButtonInfo);
            }
        };
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            int rawX = (int) event.getRawX();
            int rawY = (int) event.getRawY();
            Log.i(TAG,"onTouch:event="+event.getAction()+",rawx="+rawX+",rawy="+rawY);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mHost.setPressed(true);//默认是点击事件
                    mIsDrag=true;//默认是非拖动而是点击事件

                    mStartX = mLastX=rawX;
                    mStartY = mLastY=rawY - statusBarHeight;
                    int[] location = new int[2] ;
                    mHost.getLocationOnScreen (location);
                    int hostX = location[0] + (int)(mLParams.width/2);
                    int hostY = location[1] - statusBarHeight + (int)(mLParams.height/2);
                    mTouchCenter = Math.abs(hostX - mStartX) < 40 && Math.abs(hostY - mStartY) < 40;
                    Log.i(TAG, "w="+mLParams.width+"h="+mLParams.height+",desity="+Utils.mDesity+",locationx="+location[0]+",locationy="+location[1]);
                    Log.i(TAG,"down:x="+mStartX+",y="+mStartY + ",hostX="+hostX+",hostY="+hostY+"mTouchCenter="+mTouchCenter);
                    //mHandler.removeCallbacks(mPressNholdRunnable);
                    //mHandler.postDelayed(mPressNholdRunnable,  TIMER_LONG_PRESS);
                    break;
                case MotionEvent.ACTION_MOVE:
                    int dx=rawX-mStartX;
                    int dy=rawY-mStartY- statusBarHeight;
                    Log.i(TAG,"move:dx="+dx+",dy="+dy);
                    //这里修复一些华为手机无法触发点击事件
                    int distance= (int) Math.sqrt(dx*dx+dy*dy);
                    mIsDrag = distance>100;//只有位移大于0说明拖动了
                    if(!mIsDrag) {
                        Log.i(TAG,"move:abort");
                        break;
                    }
                    if (true) { //move position
                        mLParams.x = (int)(rawX - mLParams.width/2);
                        mLParams.y = (int)(rawY - statusBarHeight - mLParams.height/2);
                        mWindowManager.updateViewLayout(mHost, mLParams);
                        Log.i(TAG,"move:to="+rawX+","+rawY);
                        if (mButtonChangeListener != null) {
                            mButtonChangeListener.onButtonPosChanged((int)mHost.getTag(), mLParams.x, mLParams.y);
                        }
//                        drawButtonRectangle(mLParams.x, mLParams.y,
//                                mLParams.width, mLParams.height);
                        mWindowManager.updateViewLayout(mHost, mLParams);
                    } else { //change size
                        mLParams.width = rawX - mLParams.x;
                        mLParams.height = rawY - mLParams.y;
                        Log.i(TAG, "rawX="+rawX+",rawY="+rawY+",mLParam.x="+mLParams.x+",mLParam.y="+mLParams.y+",w="+mLParams.width+",h="+mLParams.height);

                        if (mLParams.width <= 200 || mLParams.height <= 200) {
                            break;
                        }

//                        mWindowManager.updateViewLayout(mHost, mLParams);
                        drawButtonRectangle(mLParams.x, mLParams.y,
                                mLParams.width, mLParams.height);

                    }

                    break;
                case MotionEvent.ACTION_UP:
                    /*
                    if (mLongPressed && !mTouchCenter) {
                        mWindowManager.updateViewLayout(mHost, mLParams);
                        if (mButtonChangeListener != null) {
                            Log.i(TAG,"size changed to : " + mLParams.width/Utils.mDesity + ", " +  (mLParams.height/Utils.mDesity));
                            mButtonChangeListener.onButtonSizeChanged((int)mHost.getTag(), (int) (mLParams.width), (int) (mLParams.height));
                        }
                    }*/
                    clearDraw();
                    //如果是拖动状态下即非点击按压事件
                    //mHandler.removeCallbacks(mPressNholdRunnable);
                    //mLongPressed = false;
                    mHost.setPressed(false);
                    dx=rawX-mStartX;
                    dy=rawY-mStartY- statusBarHeight;
                    distance= (int) Math.sqrt(dx*dx+dy*dy);
                    mIsDrag = distance>mDeltaToMove;
                    mStartX = 0;
                    mStartY = 0;
                    Log.i(TAG,"up:isDrag="+mIsDrag);
                    //mTouchCenter = false;
                    if (!mIsDrag) {
                        if (mButtonInfo == null) {
                            mHost.performClick();
                        } else {
                            mHandler.post(mPerformButtonRunnable);
                            return true;
                        }
                    }
                    break;
            }

            return mIsDrag;
        }
    }

    private void handleCustomButtonInfo(CustomButtonConfigInfo.ButtonItem buttonInfo) {
        if (buttonInfo == null) {
            return;
        }
        if (!MyTouchAssistService.isServiceRunning() || MyTouchAssistService.mTouchServiceInstance == null) {
            Toast.makeText(this, R.string.str_grantaccessisbility, Toast.LENGTH_SHORT).show();
            mMainHandler.postDelayed(()->{
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
            }, TIMER_LAUNCH_ACCESSIBILITY_ACTIVITY);
            return;
        }
        Log.d(TAG, "handleCustomButtonInfo, action= " + buttonInfo.mAction);
        switch(buttonInfo.mAction) {
            case TouchConfigItem.ACTION_CLICK:
                drawClickPoint(buttonInfo.mTargetX1, buttonInfo.mTargetY1);
                Log.d(TAG, "send click to " + buttonInfo.mTargetX1 + ", " + buttonInfo.mTargetY1 + ", statusBarHeight="+statusBarHeight);
                MyTouchAssistService.mTouchServiceInstance.sendClickEvent(buttonInfo.mTargetX1, buttonInfo.mTargetY1);
                break;

            case TouchConfigItem.ACTION_SWIPE:
                drawSwipePoint(buttonInfo.mTargetX1, buttonInfo.mTargetY1, buttonInfo.mTargetX2, buttonInfo.mTargetY2);
                Log.d(TAG, "send swipe to, x1= " + buttonInfo.mTargetX1 + ", y1=" + buttonInfo.mTargetY1 );
                Log.d(TAG, "x2 =" + buttonInfo.mTargetX2 + ", y2=" + buttonInfo.mTargetY2);
                MyTouchAssistService.mTouchServiceInstance.sendSwipeEvent(buttonInfo.mTargetX1, buttonInfo.mTargetY1, buttonInfo.mTargetX2, buttonInfo.mTargetY2);
                break;
        }
    }

    private View createFloatIcon(int resid, WindowManager.LayoutParams lparams) {
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        }
        //设置type.系统提示型窗口，一般都在应用程序窗口之上.
        lparams.type = ALERT_WINDOW_TYPE;
        //设置效果为背景透明.
        lparams.format = PixelFormat.RGBA_8888;
        //设置flags.不可聚焦及不可使用按钮对悬浮窗进行操控.
        lparams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        //设置窗口初始停靠位置.
        lparams.gravity = Gravity.LEFT | Gravity.TOP;
//        lparams.x = 0;
//        lparams.y = 0;

        //设置悬浮窗口长宽数据.
        lparams.width = WindowManager.LayoutParams.WRAP_CONTENT;//(int)Utils.mDesity*Utils.TOUCH_ICON_WIDTH;
        lparams.height = WindowManager.LayoutParams.WRAP_CONTENT;;//(int)Utils.mDesity*Utils.TOUCH_ICON_HEIGHT;

        LayoutInflater inflater = LayoutInflater.from(getApplication());
        //获取浮动窗口视图所在布局.
        View llayout = (LinearLayout) inflater.inflate(resid,null);
        //添加toucherlayout
        mWindowManager.addView(llayout,lparams);
        return llayout;
    }

    private void createToucher()
    {
        //赋值WindowManager&LayoutParam.
        mParams_Logo = new WindowManager.LayoutParams();
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        }
        //设置type.系统提示型窗口，一般都在应用程序窗口之上.
        mParams_Logo.type = ALERT_WINDOW_TYPE;
        //设置效果为背景透明.
        mParams_Logo.format = PixelFormat.RGBA_8888;
        //设置flags.不可聚焦及不可使用按钮对悬浮窗进行操控.
        mParams_Logo.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        //设置窗口初始停靠位置.
        mParams_Logo.gravity = Gravity.LEFT | Gravity.TOP;
        mParams_Logo.x = 0;
        mParams_Logo.y = 0;

        //设置悬浮窗口长宽数据.
        mParams_Logo.width = (int)(Utils.mDesity * Utils.LOGO_ICON_WIDTH);
        mParams_Logo.height = (int)(Utils.mDesity * Utils.LOGO_ICON_WIDTH);

        LayoutInflater inflater = LayoutInflater.from(getApplication());
        //获取浮动窗口视图所在布局.
        mLayout_Logo = (LinearLayout) inflater.inflate(R.layout.activity_main,null);
        //添加toucherlayout
        mWindowManager.addView(mLayout_Logo, mParams_Logo);

        Log.i(TAG,"toucherlayout-->left:" + mLayout_Logo.getLeft());
        Log.i(TAG,"toucherlayout-->right:" + mLayout_Logo.getRight());
        Log.i(TAG,"toucherlayout-->top:" + mLayout_Logo.getTop());
        Log.i(TAG,"toucherlayout-->bottom:" + mLayout_Logo.getBottom());

        //主动计算出当前View的宽高信息.
        mLayout_Logo.measure(View.MeasureSpec.UNSPECIFIED,View.MeasureSpec.UNSPECIFIED);

        //用于检测状态栏高度.
        int resourceId = getResources().getIdentifier("status_bar_height","dimen","android");
        if (resourceId > 0)
        {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        Utils.mStatusBarHeight = statusBarHeight;
        Log.i(TAG,"状态栏高度为:" + statusBarHeight);

        //浮动窗口按钮.
        mButton_Logo = (Button) mLayout_Logo.findViewById(R.id.logo);
        mButton_Logo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {/*
                mParams_Logo.x = (int) event.getRawX() - 150;
                mParams_Logo.y = (int) event.getRawY() - 150 - statusBarHeight;
                mWindowManager.updateViewLayout(mLayout_Logo,mParams_Logo);
                return false;*/

                int rawX = (int) event.getRawX();
                int rawY = (int) event.getRawY();
                Log.i(TAG,"onTouch:event="+event.getAction());
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mButton_Logo.setPressed(true);//默认是点击事件
                        isDrag=true;//默认是非拖动而是点击事件

                        startX = lastX=rawX;
                        startY = lastY=rawY;
                        Log.i(TAG,"down:x="+startX+",y="+startY);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int dx=rawX-startX;
                        int dy=rawY-startY;
                        Log.i(TAG,"move:dx="+dx+",dy="+dy);
                        //这里修复一些华为手机无法触发点击事件
                        int distance= (int) Math.sqrt(dx*dx+dy*dy);
                        isDrag = distance>mDeltaToMove;//只有位移大于0说明拖动了
                        if(!isDrag) {
                            Log.i(TAG,"move:abort");
                            break;
                        }

                        mParams_Logo.x = rawX;
                        mParams_Logo.y = rawY;
                        mWindowManager.updateViewLayout(mLayout_Logo, mParams_Logo);
                        Log.i(TAG,"move:to="+rawX+","+rawY);
                        int[] location = new int[2] ;
                        mButton_Logo.getLocationOnScreen(location);
                        Log.i(TAG,"button:x="+location[0]+",y="+location[1]);
                        break;
                    case MotionEvent.ACTION_UP:
                        //如果是拖动状态下即非点击按压事件
                        mButton_Logo.setPressed(false);
                        dx=rawX-startX;
                        dy=rawY-startY;
                        distance= (int) Math.sqrt(dx*dx+dy*dy);
                        isDrag = distance>mDeltaToMove;
                        startX = 0;
                        startY = 0;
                        Log.i(TAG,"up:isDrag="+isDrag);
                        if (!isDrag) {
                            mButton_Logo.performClick();
                        }
                        break;
                }

                //如果不是拖拽，那么就不消费这个事件，以免影响点击事件的处理
                //拖拽事件要自己消费
                return isDrag;//|| super.onTouch(v, event);
            }
        });

        mButton_Quit = (Button) mLayout_Logo.findViewById(R.id.btnquit);
        mButton_Config  = (Button) mLayout_Logo.findViewById(R.id.btnconfig);
    }


    private void notifyClickPosChange(int x, int y, int tag) {
        for (ClickPosChangeListener listener : mClickChangeListeners) {
            if (listener != null) {
                listener.onClickPosChanged(x, y, tag);
            }
        }
    }
    private void notifySwipeEndPosChange(int x, int y, int tag) {
        for (ClickPosChangeListener listener : mClickChangeListeners) {
            if (listener != null) {
                listener.onSwipeEndChange(x, y, tag);
            }
        }
    }
    private int mDeltaToMove = 100;
    private class MyTouchListener implements View.OnTouchListener {
        private View mMyView;
        private boolean mIsDrag;
        private int myStartX, myStartY, myLastX, myLastY;
        WindowManager.LayoutParams mParams;
        boolean swipeEnd;
        private int mTag;
        public MyTouchListener(View view, WindowManager.LayoutParams params, boolean swipe, int tag) {
            mMyView = view;
            mParams = params;
            swipeEnd = swipe;
            mTag = tag;
        }

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            {
                int rawX = (int) event.getRawX();
                int rawY = (int) event.getRawY();
                Log.i(TAG,"onTouch:event="+event.getAction());
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mMyView.setPressed(true);//默认是点击事件
                        mIsDrag=true;//默认是非拖动而是点击事件

                        myStartX = myLastX=rawX;
                        myStartY = myLastY=rawY;
                        Log.i(TAG,"down:x="+myStartX+",y="+myStartY);
/*                        if (swipeEnd) {
                            ((ImageView)view).setImageResource(R.drawable.end_p);
                        } else {
                            ((ImageView)view).setImageResource(R.drawable.target_f);
                        }*/
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int dx=rawX-myStartX;
                        int dy=rawY-myStartY;
                        Log.i(TAG,"move:dx="+dx+",dy="+dy);
                        //这里修复一些华为手机无法触发点击事件
                        int distance= (int) Math.sqrt(dx*dx+dy*dy);
                        mIsDrag = distance>mDeltaToMove;//只有位移大于0说明拖动了
                        if(!mIsDrag) {
                            Log.i(TAG,"move:abort");
                            break;
                        }

                        mParams.x = rawX;
                        mParams.y = rawY;
                        mWindowManager.updateViewLayout(mMyView,mParams);
                        Log.i(TAG,"move:to="+rawX+","+rawY);
                        int[] location = new int[2] ;
                        mMyView.getLocationOnScreen(location);

                        int targetX = location[0] + mMyView.getWidth()/2;
                        int targetY = location[1] + mMyView.getHeight() - Utils.mStatusBarHeight;
                        Log.i(TAG,"button:x="+location[0]+",y="+location[1] +
                                ",width="+mMyView.getWidth()+",height="+mMyView.getHeight() +
                                ",targetX="+targetX+",targetY="+targetY);
                        if (swipeEnd) {
                            notifySwipeEndPosChange(targetX, targetY, mTag);
                        } else {
                            notifyClickPosChange(targetX, targetY, mTag);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        //如果是拖动状态下即非点击按压事件
                        /*if (swipeEnd) {
                            ((ImageView)view).setImageResource(R.drawable.target);
                        } else {
                            ((ImageView)view).setImageResource(R.drawable.target);
                        }*/
                        mMyView.setPressed(false);
                        dx=rawX-myStartX;
                        dy=rawY-myStartY;
                        distance= (int) Math.sqrt(dx*dx+dy*dy);
                        mIsDrag = distance>mDeltaToMove;
                        myStartX = 0;
                        myStartY = 0;
                        Log.i(TAG,"up:isDrag="+mIsDrag+",dx="+dx+",dy="+dy);
                        if (!mIsDrag) {
                            showConfigActivity();
                        }

                        break;
                }


                return mIsDrag;
            }
        }
    }
    @Override
    public void onDestroy()
    {
        if (mButton_Logo != null)
        {
            mWindowManager.removeView(mLayout_Logo);
        }
        super.onDestroy();
    }

}
