package com.belyware.touchassist;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.belyware.touchassist.Utils.CustomButtonChangeListener;
import java.util.ArrayList;

import static com.belyware.touchassist.TouchConfigItem.ACTION_CLICK;
import static com.belyware.touchassist.TouchConfigItem.ACTION_SWIPE;

public class CustomButtonConfig {

    private static int DEFAULT_X = 300;
    private static int DEFAULT_Y = 300;
    private static int DEFAULT_WIDTH = 300;
    private static int DEFAULT_HEIGHT = 200;

    private static Context mContext;
    public static Spinner mSpinner_BtnConfig;
    private static CustomButtonCfgListAdapter myAdapter;
    private static int mCurrentCfgIndex;
    public static ListView mButtonListView;
    private static int mCurrentConfigIndex;
    static ArrayAdapter<String> spinnerAdapter;
    public static Button mDelButton, mAddConfigButton, mAddItemButton;
    private static ArrayList<CustomButtonConfigInfo> mCustomButtonConfigInfo;

    public static void initialize(Context ctx) {
        mContext = ctx;
        myAdapter = new CustomButtonCfgListAdapter();
        mButtonListView.setAdapter(myAdapter);
        ((DragListView)mButtonListView).enableDrag(false);


        mSpinner_BtnConfig.setAdapter(spinnerAdapter);
        updateButtonConfigList();
        updateCustomBtnList(0);
        mSpinner_BtnConfig.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                updateCustomBtnList(i);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        Utils.registerButtonChangeListener(mButtonChangeListener);
        Utils.addRemoveClickChangeListener(mClickChangeListener, true);
    }

    private static void updateCustomBtnList(int configIndex) {
        closeWidgetWhenApply();
        if (mCustomButtonConfigInfo != null && mCustomButtonConfigInfo.size() > configIndex) {
            mCurrentConfigIndex = configIndex;
            myAdapter.setData(mCustomButtonConfigInfo.get(mCurrentConfigIndex).mButtonList);
        } else {
            myAdapter.setData(null);
        }
    }
    public static void addConfig() {
        if (mCustomButtonConfigInfo != null && mCustomButtonConfigInfo.size() >= Utils.MAX_BUTTON_CONFIG_NUM) {
            Toast.makeText(mContext, R.string.max_configs_reached, Toast.LENGTH_SHORT).show();
            return;
        }
        closeWidgetWhenApply();
        final EditText et = new EditText(mContext);
        et.setMaxLines(1);
        et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16)});
        new AlertDialog.Builder(mContext).setTitle(R.string.inputname)
                .setIcon(android.R.drawable.sym_def_app_icon)
                .setView(et)
                .setCancelable(false)
                .setPositiveButton(R.string.str_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Editable editable = et.getText();
                        if (editable != null) {
                            String name = editable.toString();
                            if (TextUtils.isEmpty(name)) {
                                Toast.makeText(mContext, R.string.nullnotallowed, Toast.LENGTH_SHORT).show();
                                return;
                            }

                            CustomButtonConfigInfo cci = new CustomButtonConfigInfo(name);
                            if (mCustomButtonConfigInfo == null) {
                                mCustomButtonConfigInfo = new ArrayList<>();
                            }
                            mCustomButtonConfigInfo.add(cci);
                            mCurrentConfigIndex = mCustomButtonConfigInfo.indexOf(cci);
                            updateButtonConfigList();
                            updateCustomBtnList(mCurrentConfigIndex);
                            saveConfig();
                        }

                    }
                }).setNegativeButton(R.string.str_cancel, null).show();
    }
    public static void deleteConfig() {
        closeWidgetWhenApply();
        int cfgIndex = mSpinner_BtnConfig.getSelectedItemPosition();
        if (cfgIndex >= 0 && cfgIndex < mCustomButtonConfigInfo.size()) {
            mCustomButtonConfigInfo.remove(cfgIndex);
            mCurrentConfigIndex = cfgIndex - 1;
            if (mCurrentConfigIndex < 0) {
                mCurrentConfigIndex = 0;
            }
            updateButtonConfigList();
            updateCustomBtnList(mCurrentConfigIndex);
            saveConfig();
        }
    }

    public static void addCustomButton() {
        closeWidgetWhenApply();
        CustomButtonConfigInfo.ButtonItem bi = new CustomButtonConfigInfo.ButtonItem();
        bi.mBtnName = mContext.getResources().getString(R.string.str_new);
        bi.mX = DEFAULT_X;
        bi.mY = DEFAULT_Y;
        bi.mTargetX1 = DEFAULT_X;
        bi.mTargetY1 = DEFAULT_Y;
        bi.mWidth = DEFAULT_WIDTH;
        bi.mHeight = DEFAULT_HEIGHT;
        bi.mAction = ACTION_CLICK;
        if (mCurrentConfigIndex >= 0 && mCurrentConfigIndex < mCustomButtonConfigInfo.size()) {
            if (mCustomButtonConfigInfo.get(mCurrentConfigIndex).mButtonList == null) {
                mCustomButtonConfigInfo.get(mCurrentConfigIndex).mButtonList = new ArrayList<CustomButtonConfigInfo.ButtonItem>();
            }
            if (mCustomButtonConfigInfo.get(mCurrentConfigIndex).mButtonList.size() >= Utils.MAX_CUSTOM_BUTTON_NUM) {
                Toast.makeText(mContext, R.string.max_custom_button_reached, Toast.LENGTH_SHORT).show();
            } else {
                mCustomButtonConfigInfo.get(mCurrentConfigIndex).mButtonList.add(bi);
                updateCustomBtnList(mCurrentConfigIndex);
                saveConfig();
            }
        }
    }

    private static void updateButtonConfigList() {
        String[] configNameList;
        if (mCustomButtonConfigInfo != null && mCustomButtonConfigInfo.size() > 0) {
            configNameList = new String[mCustomButtonConfigInfo.size()];
            int i = 0;
            for (CustomButtonConfigInfo config : mCustomButtonConfigInfo) {
                configNameList[i++] = config.mCfgName;
            }
        } else {
            configNameList = new String[1];
            configNameList[0] = mContext.getResources().getString(R.string.str_addfirst);
        }
        spinnerAdapter = new ArrayAdapter<>(mContext,
                R.layout.spinner_item_select, configNameList);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_item_drop);
        mSpinner_BtnConfig.setAdapter(spinnerAdapter);
        if (mCustomButtonConfigInfo != null && mCustomButtonConfigInfo.size() > 0) {
            mDelButton.setEnabled(true);
            mAddItemButton.setEnabled(true);
            mSpinner_BtnConfig.setEnabled(true);
            mSpinner_BtnConfig.setSelection(mCurrentConfigIndex);
        } else {
            mDelButton.setEnabled(false);
            mAddItemButton.setEnabled(false);
            mSpinner_BtnConfig.setEnabled(false);
        }
    }

    private static class CustomButtonCfgListAdapter extends DragListViewAdapter {


        @Override
        public View getItemView(int pos, View v, ViewGroup root) {

            LayoutInflater inf = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View itemView = inf.inflate(R.layout.actionitem, null);

            TextView tv = (TextView) itemView.findViewById(R.id.actionname);

            tv.setText(((CustomButtonConfigInfo.ButtonItem)getItem(pos)).mBtnName);

            ImageView btn_del = itemView.findViewById(R.id.btn_deleteaction);
            btn_del.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final int btnIndex =(int) view.getTag();
                    if (btnIndex >= 0 && btnIndex < mCustomButtonConfigInfo.get(mCurrentCfgIndex).mButtonList.size()) {

                        new AlertDialog.Builder(mContext).setTitle(R.string.confirmDelete)
                                .setIcon(android.R.drawable.sym_def_app_icon)
                                .setCancelable(false)
                                .setPositiveButton(R.string.str_ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        closeWidgetWhenApply();
                                        mCustomButtonConfigInfo.get(mCurrentCfgIndex).mButtonList.remove(btnIndex);
                                        showButtonList(mCurrentCfgIndex);
                                        Toast.makeText(mContext, R.string.str_itemdeleted, Toast.LENGTH_SHORT).show();
                                        saveConfig();

                                    }
                                }).setNegativeButton(R.string.str_cancel, null).show();



                    }
                }
            });
            btn_del.setTag(pos);

            ImageView btn_changePos = (ImageView) itemView.findViewById(R.id.btn_position);
            btn_changePos.setVisibility(View.VISIBLE);
            btn_changePos.setTag(pos);
            btn_changePos.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = (int)view.getTag();
                    if (mCustomButtonConfigInfo.size()> mCurrentCfgIndex &&
                            mCustomButtonConfigInfo.get(mCurrentCfgIndex).mButtonList != null &&
                            mCustomButtonConfigInfo.get(mCurrentCfgIndex).mButtonList.size()>pos) {
                        CustomButtonConfigInfo.ButtonItem bi = mCustomButtonConfigInfo.get(mCurrentCfgIndex).mButtonList.get(pos);
                        closeWidgetWhenApply();
                        if (bi.mAction == ACTION_CLICK) {
                            Utils.showClickIconForCustomButton(bi.mTargetX1, bi.mTargetY1 - Utils.mStatusBarHeight, hashButton(mCurrentCfgIndex, pos), bi.mBtnName);
                        } else if (bi.mAction == ACTION_SWIPE) {
                            Utils.showSwipeButtonsForCustomButton(true,
                                        bi.mTargetX1,
                                    bi.mTargetY1 - Utils.mStatusBarHeight,
                                        bi.mTargetX2,
                                        bi.mTargetY2 - Utils.mStatusBarHeight,
                                    hashButton(mCurrentCfgIndex, pos));
                        }
                    }
                }
            });

            ImageView btn_changeAction = (ImageView) itemView.findViewById(R.id.btn_changeaction);
            btn_changeAction.setTag(pos);
            btn_changeAction.setVisibility(View.VISIBLE);
            btn_changeAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showConfigMenu(view);
                }
            });
            ImageView btn_changesize = (ImageView) itemView.findViewById(R.id.btn_size);
            btn_changesize.setTag(pos);
            btn_changesize.setVisibility(View.VISIBLE);
            btn_changesize.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("bely", "clicked.");
                    int pos = (int)view.getTag();
                    if (mCustomButtonConfigInfo.size()> mCurrentCfgIndex &&
                            mCustomButtonConfigInfo.get(mCurrentCfgIndex).mButtonList != null &&
                            mCustomButtonConfigInfo.get(mCurrentCfgIndex).mButtonList.size()>pos) {
                        closeWidgetWhenApply();
                        CustomButtonConfigInfo.ButtonItem bi = mCustomButtonConfigInfo.get(mCurrentCfgIndex).mButtonList.get(pos);
                        Utils.showCustomButton(bi.mX, bi.mY, bi.mWidth, bi.mHeight,bi.mBtnName,
                                hashButton(mCurrentCfgIndex, pos), null);
                    }
                }
            });
            return itemView;
        }

    }

    public static void onDestroy() {
        //clear button when activity closed.
        Utils.hideAllCustomBtns();
        Utils.addRemoveClickChangeListener(mClickChangeListener, false);
    }
    private static int hashButton(int value1, int value2) {
        String hash = value1 + "|" + value2;
        return hash.hashCode();
    }
    private static void showButtonList(int index) {
        myAdapter.setData(mCustomButtonConfigInfo.get(index).mButtonList);
    }

    private static void showConfigMenu(View v) {
        closeWidgetWhenApply();
        PopupMenu popup = new PopupMenu(mContext, v);//第二个参数是绑定的那个view
        //获取菜单填充器
        MenuInflater inflater = popup.getMenuInflater();
        //填充菜单
        inflater.inflate(R.menu.buttonconfig, popup.getMenu());
        //绑定菜单项的点击事件
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener((int)v.getTag()));
        if (mCurrentCfgIndex >= mCustomButtonConfigInfo.size()) {
            return;
        }
        CustomButtonConfigInfo.ButtonItem bi = mCustomButtonConfigInfo.get(mCurrentCfgIndex).mButtonList.get((int)v.getTag());
        MenuItem menuitem = null;
        String newText;
        switch(bi.mAction) {
            case ACTION_CLICK:
                menuitem = popup.getMenu().findItem(R.id.menu_single_click);
                break;
            case TouchConfigItem.ACTION_SWIPE:
                menuitem = popup.getMenu().findItem(R.id.menu_swipe);
                break;
        }
        newText = "√ " + menuitem.getTitle();
        menuitem.setTitle(newText);
        //显示(这一行代码不要忘记了)
        popup.show();

    }


    private static class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
        private int mMyPos;
        public MyMenuItemClickListener(int pos) {
            mMyPos = pos;
        }
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            closeWidgetWhenApply();
            CustomButtonConfigInfo.ButtonItem bi = mCustomButtonConfigInfo.get(mCurrentCfgIndex).mButtonList.get(mMyPos);
            switch(item.getItemId()) {
                case R.id.menu_name:
                    changeBtnName(mMyPos);
                    break;
                case R.id.menu_single_click:
                    bi.mAction = ACTION_CLICK;
                    saveConfig();
                    break;
                case R.id.menu_swipe:
                    bi.mAction = TouchConfigItem.ACTION_SWIPE;
                    saveConfig();
                    break;
            }
            return false;
        }
    }

    private static void changeBtnName(final int pos) {
        if (pos < 0 || pos >= mCustomButtonConfigInfo.get(mCurrentConfigIndex).mButtonList.size()) {
            return;
        }
        final EditText et = new EditText(mContext);
        et.setMaxLines(1);
        et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16)});
        new AlertDialog.Builder(mContext).setTitle(R.string.inputname)
                .setIcon(android.R.drawable.sym_def_app_icon)
                .setView(et)
                .setCancelable(false)
                .setPositiveButton(R.string.str_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Editable editable = et.getText();
                        if (editable != null) {
                            String name = editable.toString();
                            if (TextUtils.isEmpty(name)) {
                                Toast.makeText(mContext, R.string.nullnotallowed, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            mCustomButtonConfigInfo.get(mCurrentConfigIndex).mButtonList.get(pos).mBtnName = name;
                            myAdapter.notifyDataSetChanged();
                            saveConfig();
                        }

                    }
                }).setNegativeButton(R.string.str_cancel, null).show();
    }

    static CustomButtonChangeListener mButtonChangeListener = new CustomButtonChangeListener(){

        @Override
        public void onButtonPosChanged(int tag, int x, int y) {
            updateButtonPos(tag, x, y);
            saveConfig();
        }

        @Override
        public void onButtonSizeChanged(int tag, int w, int h) {
            updateButtonSize(tag, w, h);
            saveConfig();
        }
    };

    private static void saveConfig() {
    }

    private static void updateButtonPos(int tag, int x, int y) {
        int index = 0;
        int pos = 0;
        for (CustomButtonConfigInfo config : mCustomButtonConfigInfo) {
            for (CustomButtonConfigInfo.ButtonItem bi : config.mButtonList) {
                if (tag == hashButton(index, pos)) {
                    bi.mX = x;
                    bi.mY = y;

                    return;
                }
                pos++;
            }
            index++;
        }
    }

    private static void updateButtonSize(int tag, int w, int h) {
        int index = 0;
        int pos = 0;
        for (CustomButtonConfigInfo config : mCustomButtonConfigInfo) {
            for (CustomButtonConfigInfo.ButtonItem bi : config.mButtonList) {
                if (tag == hashButton(index, pos)) {
                    bi.mWidth = w;
                    bi.mHeight = h;
                    return;
                }
                pos++;
            }
            index++;
        }
    }

    static MainService.ClickPosChangeListener mClickChangeListener = new MainService.ClickPosChangeListener(){


        @Override
        public void onClickPosChanged(int x, int y, int tag) {
            updateClickPos(x, y, tag);
            saveConfig();
        }

        @Override
        public void onSwipeEndChange(int x, int y, int tag) {
            updateSwipeEndPos(x, y, tag);
            saveConfig();
        }
    };

    private static void updateSwipeEndPos(int x, int y, int tag) {
        int index = 0;
        int pos = 0;
        if (mCustomButtonConfigInfo == null) {
            return;
        }
        for (CustomButtonConfigInfo config : mCustomButtonConfigInfo) {
            for (CustomButtonConfigInfo.ButtonItem bi : config.mButtonList) {
                if (tag == hashButton(index, pos)) {
                    bi.mTargetX2 = x;
                    bi.mTargetY2 = y;
                    if (bi.mAction == ACTION_SWIPE) {
                        Utils.drawSwipeLine(bi.mTargetX1, bi.mTargetY1, bi.mTargetX2, bi.mTargetY2);
                    }
                    return;
                }
                pos++;
            }
            index++;
        }
    }
    private static void updateClickPos(int x, int y, int tag) {
        int index = 0;
        int pos = 0;
        if (mCustomButtonConfigInfo == null) {
            return;
        }
        for (CustomButtonConfigInfo config : mCustomButtonConfigInfo) {
            for (CustomButtonConfigInfo.ButtonItem bi : config.mButtonList) {
                if (tag == hashButton(index, pos)) {
                    bi.mTargetX1 = x;
                    bi.mTargetY1 = y;
                    if (bi.mAction == ACTION_SWIPE) {
                        Utils.drawSwipeLine(bi.mTargetX1, bi.mTargetY1, bi.mTargetX2, bi.mTargetY2);
                    }
                    return;
                }
                pos++;
            }
            index++;
        }
    }
    public static void showSelectedConfigButtons() {
        int index = mCurrentCfgIndex;
        int pos = 0;
        if (mCustomButtonConfigInfo != null && mCustomButtonConfigInfo.size() > index) {
            CustomButtonConfigInfo ci = mCustomButtonConfigInfo.get(index);
            for (CustomButtonConfigInfo.ButtonItem bi : ci.mButtonList) {
                int tag = hashButton(index, pos);
                Utils.showCustomButton(bi.mX, bi.mY, bi.mWidth, bi.mHeight, bi.mBtnName, tag, bi);
                pos++;
            }
        } else {
            Toast.makeText(MainApplication.mMyContext, R.string.str_noconfigbutton, Toast.LENGTH_LONG).show();
        }
    }


    public static void closeWidgetWhenApply() {
        int index = mCurrentCfgIndex;
        int pos = 0;
        if (mCustomButtonConfigInfo != null && mCustomButtonConfigInfo.size() > index && index >= 0) {
            CustomButtonConfigInfo ci = mCustomButtonConfigInfo.get(index);
            for (CustomButtonConfigInfo.ButtonItem bi : ci.mButtonList) {
                int tag = hashButton(index, pos);
                Utils.showCustomButton(bi.mX, bi.mY, bi.mWidth, bi.mHeight, bi.mBtnName, tag, bi, true);
                Utils.showClickIconForCustomButton(bi.mX, bi.mY, tag, bi.mBtnName, true);
                Utils.showSwipeButtonsForCustomButton(false, 0, 0, 0, 0, 0);
                pos++;
            }
        }
    }

}
