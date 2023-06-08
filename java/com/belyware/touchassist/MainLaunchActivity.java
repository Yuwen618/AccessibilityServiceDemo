package com.belyware.touchassist;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.belyware.touchassist.R;

public class MainLaunchActivity extends Activity {
    private static final int MSG_SHOWOVERLAY = 10002;

    public static int OVERLAY_PERMISSION_REQ_CODE = 999;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MSG_SHOWOVERLAY:
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + MainLaunchActivity.this.getPackageName()));
                    startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //StatConfig.setDebugEnable(true);
        //StatService.registerActivityLifecycleCallbacks(this.getApplication());
        //XiaomiUpdateAgent.update(this);
/*
        Intent intent = new Intent(MainActivity.this,MainService.class);
        Toast.makeText(MainActivity.this,"DebugAssist enabled.",Toast.LENGTH_SHORT).show();
        startService(intent);
        finish();*/


        setTheme(android.R.style.Theme_NoDisplay);
        setContentView(R.layout.invisibleactivity);

        if (Settings.canDrawOverlays(MainLaunchActivity.this))
        {
            Intent intent = new Intent(MainLaunchActivity.this,MainService.class);
            startService(intent);
        }else
        {
            Toast.makeText(MainLaunchActivity.this,R.string.str_grantoverlay,Toast.LENGTH_LONG).show();
            mHandler.sendEmptyMessageDelayed(MSG_SHOWOVERLAY, 4000);
        }
        finish();
    }

    private void loadInstruction() {
        WebView webview = (WebView) findViewById(R.id.webview);
        if (Utils.isCN) {
            webview.loadUrl("file:////android_asset/term_cn.html");
        } else {
            webview.loadUrl("file:////android_asset/term_en.html");
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                // SYSTEM_ALERT_WINDOW permission not granted...
                Toast.makeText(this, R.string.need_overlay_permission, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.overlay_granted, Toast.LENGTH_SHORT).show();
                // Already hold the SYSTEM_ALERT_WINDOW permission, do addview or something.
                Intent intent = new Intent(MainLaunchActivity.this,MainService.class);
                startService(intent);
            }
            finish();
        }
    }

}
