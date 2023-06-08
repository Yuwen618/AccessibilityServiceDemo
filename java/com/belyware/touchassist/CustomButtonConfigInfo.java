package com.belyware.touchassist;

import java.util.ArrayList;

public class CustomButtonConfigInfo {
    public String mCfgName;
    public ArrayList<ButtonItem> mButtonList;

    public CustomButtonConfigInfo(String name) {
        mCfgName = name;
        mButtonList = new ArrayList<ButtonItem>();
    }

    public static class ButtonItem {
        public String mBtnName;
        public int mX,mY;
        public int mWidth, mHeight;
        public int mAction;
        public int mTargetX1, mTargetY1, mTargetX2, mTargetY2;

        public ButtonItem() {

        }
        public ButtonItem(String name, int startX, int startY, int w, int h) {
            mBtnName = name;
            mX = startX;
            mY = startY;
            mWidth = w;
            mHeight = h;
        }
    }
}
