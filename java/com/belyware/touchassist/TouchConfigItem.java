package com.belyware.touchassist;

import java.util.ArrayList;

public final class TouchConfigItem {
    public final static int ACTION_CLICK = 101;
    public final static int ACTION_SWIPE = 102;
    public final static int ACTION_DELAY = 103;
    public final static int ACTION_LONGCLICK = 104;
    public final static int ACTION_DOUBLECLICK = 105;

    public final static int REPEAT_NONE = 201;
    public final static int REPEAT_ALWAYS = 202;

    public String mName;
    public ArrayList<ActionItem> mActions;
    public int mRepeatType;

    public TouchConfigItem(String name) {
        mName = name;
        mActions = new ArrayList<ActionItem>();
        mRepeatType = REPEAT_ALWAYS;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setActions(ArrayList<ActionItem> actions) {
        mActions = actions;
    }

    public void setRepeatType(int type) {
        mRepeatType = type;
    }

    public static class ActionItem {
        public int mAction;
        public int mDelayTime;
        public boolean mRandom;
        public int mRandDelay_Low,mRandDelay_High;
        public int mX1,mY1,mX2,mY2;

        public ActionItem(int action, int delay) {
            mAction = action;
            mDelayTime = delay;
        }
        public ActionItem(int action, boolean random, int low, int high) {
            mAction = action;
            mRandom = random;
            mRandDelay_Low = low;
            mRandDelay_High = high;
        }

        public ActionItem(int action, int x, int y) {
            mAction = action;
            mX1 = x;
            mY1 = y;
        }

        public ActionItem(int action, int x1, int y1, int x2, int y2) {
            mAction = action;
            mX1 = x1;
            mY1 = y1;
            mX2 = x2;
            mY2 = y2;
        }

        public ActionItem(int action, int delay, int x1, int y1, int x2, int y2) {
            mAction = action;
            mDelayTime = delay;
            mX1 = x1;
            mY1 = y1;
            mX2 = x2;
            mY2 = y2;
        }

        public ActionItem(int action, boolean rand, int delay, int low, int high, int x1, int y1, int x2, int y2) {
            mAction = action;
            mDelayTime = delay;
            mX1 = x1;
            mY1 = y1;
            mX2 = x2;
            mY2 = y2;
            mRandom = rand;
            mRandDelay_Low = low;
            mRandDelay_High = high;
        }
    }
}
