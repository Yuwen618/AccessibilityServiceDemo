package com.belyware.touchassist;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;


public class Menu extends RelativeLayout implements View.OnClickListener{
    private static final String TAG = "TouchMenu";

    private static final int MSG_DBCLICK_STARTED = 1009;
    private static final int TIME_INTERVAL_FOR_DBCLICK = 500;
    private static final int MSG_UPDATE_TIMER_COUNT = 1010;
    private static final int MSG_START_TIMERSHOW = 1011;
    private static final int MSG_HIDE_OTHER_BUTTONS = 1012;
    private static final int MSG_SHOW_DISCLAIMER = 1013;

    private View view;


    public static Button mButton_logo;

    public Button mButton_quit;
    public Button mButton_config;
    public Button mButton_info;
    public Button mButton_showbtn;
    public Button mButton_autoclick;

    public static Handler mUIHandler;
    private boolean mClick_Recorded;
    private Context mContext;

    public static int mTimeCounter = 0;
    private volatile boolean mActionRunning = false;
    private volatile boolean mConfigShowing = false;

    public Menu(Context context) {
        this(context, null);
    }

    public Menu(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
        setListener();
        Utils.connectService1(context);
   //     Utils.connectService2(context);
    }

    private void init() {
        view = LayoutInflater.from(getContext()).inflate(R.layout.menu, Menu.this);
        Utils.mMainLayout = (LinearLayout) view.findViewById(R.id.mainlogo);
        mButton_logo = (Button) view.findViewById(R.id.logo);
        mButton_quit = (Button) view.findViewById(R.id.btnquit);
        mButton_config = (Button) view.findViewById(R.id.btnconfig);
        mButton_info = (Button) view.findViewById(R.id.btninfo);
//        mButton_showbtn = (Button) view.findViewById(R.id.btnshowbtn);
        mButton_autoclick = (Button) view.findViewById(R.id.autoclick);

        mUIHandler = new Handler(new Handler.Callback(){

            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_DBCLICK_STARTED:
                        mClick_Recorded = false;
                        handleSingleClick();
                        break;
                    case MSG_UPDATE_TIMER_COUNT:
                        if (mTimeCounter >= 0) {
                            String stime = String.valueOf(mTimeCounter);
                            mButton_logo.setText(stime);
                            mButton_logo.setBackgroundResource(R.drawable.circlebgicon);
                            mTimeCounter --;
                            if (mTimeCounter > 0) {
                                mUIHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIMER_COUNT, 1000);
                            }
                        }
                        break;
                    case MSG_START_TIMERSHOW:
                        runTimerShow((int)msg.obj);
                        break;
                    case MSG_HIDE_OTHER_BUTTONS:
                        Utils.showOtherButtons(false);
                        mConfigShowing = false;
                        break;
                    case MSG_SHOW_DISCLAIMER:
                        showDisclaimer();
                        break;
                }
                return false;
            }
        });

    }

    private void runTimerShow(int counter) {
        mTimeCounter = counter;
        mButton_logo.setBackground(null);
        String stime = String.valueOf(mTimeCounter);
        mButton_logo.setText(stime);

        mUIHandler.sendEmptyMessage(MSG_UPDATE_TIMER_COUNT);
    }

    public static void startTimerShow(int counter) {
        mTimeCounter = 0;
        mUIHandler.removeMessages(MSG_UPDATE_TIMER_COUNT);
        mUIHandler.removeMessages(MSG_START_TIMERSHOW);
        Message.obtain(mUIHandler, MSG_START_TIMERSHOW, counter).sendToTarget();
    }


    private void setListener() {
        mButton_logo.setOnClickListener(this);
        mButton_quit.setOnClickListener(this);
        mButton_config.setOnClickListener(this);
        mButton_info.setOnClickListener(this);
//        mButton_showbtn.setOnClickListener(this);
        mButton_autoclick.setOnClickListener(this);
    }

    private void handleSingleClick() {

        if (MyTouchAssistService.mTouchServiceInstance == null) {
            Toast.makeText(mContext, R.string.str_grantaccessisbility, Toast.LENGTH_SHORT).show();
            mUIHandler.postDelayed(()->{
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
                mContext.startActivity(intent);
            }, 500);
            return;
        }
        MyTouchAssistService.mRunning = !MyTouchAssistService.mRunning;
        if (MyTouchAssistService.mRunning) {
            if (Utils.mAllConfigList == null || Utils.mAllConfigList.size()==0) {
                if (Utils.mAllConfigList == null || Utils.mAllConfigList.size()==0) {
                    Toast.makeText(mContext, R.string.str_noconfiguration, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            MyTouchAssistService.mTouchServiceInstance.start();
            mButton_autoclick.setBackgroundResource(R.drawable.pauseicon);
            mActionRunning = true;
            Toast.makeText(mContext, R.string.str_autoclick_started, Toast.LENGTH_SHORT).show();
        } else {
            mUIHandler.removeMessages(MSG_UPDATE_TIMER_COUNT);
            mButton_logo.setText("");
            mButton_autoclick.setBackgroundResource(R.drawable.playicon);
            mButton_logo.setBackgroundResource(R.drawable.mainmenu);
            MyTouchAssistService.mTouchServiceInstance.pause();
            mActionRunning = false;
            Toast.makeText(mContext, R.string.str_autoclick_stopped, Toast.LENGTH_SHORT).show();
        }
    }

    private void handleDBClick() {
        Utils.showOtherButtons(true);
        mUIHandler.sendEmptyMessageDelayed(MSG_HIDE_OTHER_BUTTONS, 2000);
        mConfigShowing = true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

        case R.id.logo:
            handleDBClick();
            break;
        case R.id.autoclick:
            handleSingleClick();
            break;
//        case R.id.btnshowbtn:
//            if (Utils.isFirsttimeUse(mContext)) {
//                Toast.makeText(mContext, R.string.accept_disclaimer, Toast.LENGTH_SHORT).show();
//                mUIHandler.sendEmptyMessageDelayed(MSG_SHOW_DISCLAIMER, 2000);
//                return;
//            }
//            CustomButtonConfig.showSelectedConfigButtons();
//            break;
        case R.id.btnquit:
            Utils.shutdown();
            MobclickAgent.onKillProcess(mContext);
            System.exit(0);
            break;
        case R.id.btnconfig:
            if (MyTouchAssistService.mRunning) {
                Toast.makeText(mContext, R.string.stop_action_first, Toast.LENGTH_SHORT).show();
                return;
            }
//            mUIHandler.removeMessages(MSG_HIDE_OTHER_BUTTONS);
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.setClass(mContext, TouchConfigActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
            Utils.mMainLayout.setVisibility(View.GONE);
            mContext.startActivity(intent);
            break;
        case R.id.btninfo:
            if (MyTouchAssistService.mRunning) {
                Toast.makeText(mContext, R.string.stop_action_first, Toast.LENGTH_SHORT).show();
                return;
            }
            showInfoDialog();
            break;
        }
    }

    private void showInfoDialog() {

    }
    private void showDisclaimer() {

    }
}
