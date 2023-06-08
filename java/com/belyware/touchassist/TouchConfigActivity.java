package com.belyware.touchassist;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;


public class TouchConfigActivity extends Activity implements MainService.ClickPosChangeListener {


    private int ACTION_MAX_LIMIT = 5;
    Spinner mSpinner_Configlist;
    SpinnerAdapter mSpinnerAdapter_Configlist;
    ArrayList<TouchConfigItem> mConfigItems;
    String[] mSpinnerItems;
    ArrayAdapter<String> spinnerAdapter;
    ListView mActionListView;

    ArrayList<TouchConfigItem.ActionItem> mCurrentActionItems;

    ActionListAdapter mActionAdapter;
    View mActionConfigView;
    int mCurrentAppliedConfig;

    boolean mIsAdding;

    RadioButton btn_timer;
    RadioButton btn_click;
    RadioButton btn_swipe;
    RadioButton btn_dbclick;
    RadioButton btn_fixedtimer;
    RadioButton btn_randomtimer;
    Spinner spinner_start;
    Spinner spinner_end;

    LinearLayout layout_timer;
    LinearLayout layout_click;
    LinearLayout layout_swipe;
    TextView tv_timer;
    TextView tv_click_x;
    TextView tv_click_y;
    TextView tv_swipe_x1;
    TextView tv_swipe_y1;
    TextView tv_swipe_x2;
    TextView tv_swipe_y2;

    TextView tv_randstart;
    TextView tv_randend;

    Button btn_deleteConfig;
    Button btn_addAction;
    Button btn_apply;
    Button btn_selectButtons, btn_selectActions;

    ImageView imgView_touch;

    LinearLayout mLayout_ActionCfg, mLayout_ButtonCfg;

    int[] delayTimer = {
        1,//1
            30,//30 seconds
            60,//1 minute
            120,//2 minutes
            300,//5 minutes
            600//10 minutes
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.touchconfig);
        initConfiglist();
        initViews();
        Utils.addRemoveClickChangeListener(this, true);
    }

    private void initViews() {
        mLayout_ActionCfg = (LinearLayout) findViewById(R.id.layout_actionconfig);
//        mLayout_ButtonCfg = (LinearLayout)findViewById(R.id.layout_buttonconfig);
//        CustomButtonConfig.mSpinner_BtnConfig = (Spinner) findViewById(R.id.btnconfiglist);
//        CustomButtonConfig.mButtonListView = (ListView)findViewById(R.id.custombuttonlist);
//        CustomButtonConfig.mDelButton = (Button)findViewById(R.id.btnconfig_delete);
//        CustomButtonConfig.mAddConfigButton = (Button)findViewById(R.id.btnconfig_additem);
//        CustomButtonConfig.mAddItemButton = (Button)findViewById(R.id.button_addbutton);

        btn_selectActions = (Button)findViewById(R.id.btn_actions);
//        btn_selectButtons = (Button)findViewById(R.id.btn_buttons);

        //CustomButtonConfig.initialize(this);
        onSelectAction(null);
    }

    public void onSelectAction(View v) {
        mLayout_ActionCfg.setVisibility(View.VISIBLE);
//        mLayout_ButtonCfg.setVisibility(View.GONE);
        //CustomButtonConfig.closeWidgetWhenApply();
        btn_selectActions.setBackground(getResources().getDrawable(R.drawable.btnactive));
        btn_selectActions.setTextColor(getResources().getColor(R.color.white));
//        btn_selectButtons.setBackground(getResources().getDrawable(R.drawable.btninactive));
//        btn_selectButtons.setTextColor(getResources().getColor(R.color.black));
    }

    public void onSelectButton(View v) {
        mLayout_ActionCfg.setVisibility(View.GONE);
        mLayout_ButtonCfg.setVisibility(View.VISIBLE);
        btn_selectActions.setTextColor(getResources().getColor(R.color.black));
        btn_selectActions.setBackground(getResources().getDrawable(R.drawable.btninactive));
//        btn_selectButtons.setBackground(getResources().getDrawable(R.drawable.btnactive));
//        btn_selectButtons.setTextColor(getResources().getColor(R.color.white));
    }

    public void onAddConfig(View v) {
        //CustomButtonConfig.addConfig();
    }

    public void onDeleteConfig(View v) {
        //CustomButtonConfig.deleteConfig();
    }


    public void onAddCustomButton(View v) {
        //CustomButtonConfig.addCustomButton();
    }


    public void onChangePos(View v) {
       // Utils.showCustomButton(200, 300, 200, 100, null);
    }

    public void onChangeAction(View v) {

    }


    private void updateConfigList() {
        if (mConfigItems==null || mConfigItems.size()<=0){
            enableControlButton(false);
            mSpinnerItems = new String[0];
            spinnerAdapter = new ArrayAdapter<>(this,
                    R.layout.spinner_item_select, mSpinnerItems);
            spinnerAdapter.setDropDownViewResource(R.layout.spinner_item_drop);
            mSpinner_Configlist.setAdapter(spinnerAdapter);
            return;
        }
        mSpinnerItems = new String[mConfigItems.size()];

        int i = 0;
        for (TouchConfigItem item : mConfigItems) {
            mSpinnerItems[i++] = item.mName;
        }

        spinnerAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item_select, mSpinnerItems);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_item_drop);
        mSpinner_Configlist.setAdapter(spinnerAdapter);
        enableControlButton(true);
    }

    private void initConfiglist() {
        btn_deleteConfig = (Button)findViewById(R.id.button_delete);
        btn_addAction = (Button)findViewById(R.id.button_addaction);
        btn_apply = (Button)findViewById(R.id.button_apply);
        mSpinner_Configlist = (Spinner) findViewById(R.id.configlist);

        mActionListView = (ListView) findViewById(R.id.actionlist);

        mSpinnerItems = new String[mConfigItems.size()];
        int i = 0;
        for (TouchConfigItem item : mConfigItems) {
            mSpinnerItems[i++] = item.mName;
        }
        spinnerAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item_select, mSpinnerItems);


        spinnerAdapter.setDropDownViewResource(R.layout.spinner_item_drop);

/*
        mSpinner_Configlist.setDropDownWidth(400); //下拉宽度
        mSpinner_Configlist.setDropDownHorizontalOffset(0); //下拉的横向偏移*/
        mSpinner_Configlist.setDropDownVerticalOffset(130); //下拉的纵向偏移


        mSpinner_Configlist.setAdapter(spinnerAdapter);
        mSpinner_Configlist.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                    showActionList(i);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });



        mActionAdapter = new ActionListAdapter();
        mActionListView.setAdapter(mActionAdapter);
        mActionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (mCurrentActionItems!= null && i>=0 && i<mCurrentActionItems.size()) {
                    editAction(i);
                }
            }
        });

        if (mConfigItems != null && mConfigItems.size()>0) {
            if (Utils.mCurrentActiveConfigIndex >=0 && (Utils.mCurrentActiveConfigIndex < mConfigItems.size())) {
                showActionList(Utils.mCurrentActiveConfigIndex);
            } else {
                showActionList(0);
            }
            enableControlButton(true);
        } else {
            enableControlButton(false);
            Toast.makeText(this, R.string.str_noconfiguration, Toast.LENGTH_SHORT).show();
        }
    }

    private void enableButtons(boolean enable) {
        if (enable) {

        } else {

        }
    }

    public void addItem(View v) {
        if (mConfigItems != null && mConfigItems.size() >= Utils.MAX_AUTOCLICK_CONFIG_NUM) {
            Toast.makeText(TouchConfigActivity.this, R.string.max_config_reached, Toast.LENGTH_SHORT).show();
            return;
        }

        final EditText et = new EditText(this);
        et.setFilters(new InputFilter[] { new InputFilter.LengthFilter(16) });
        et.setText("");
        new AlertDialog.Builder(this).setTitle(R.string.inputname)
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
                                Toast.makeText(TouchConfigActivity.this, R.string.nullnotallowed, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            TouchConfigItem newItem = new TouchConfigItem(name);
                            mConfigItems.add(newItem);
                            updateConfigList();
                            showActionList(mConfigItems.size()-1);
                        }

                    }
                }).setNegativeButton(R.string.str_cancel, null).show();
    }

    boolean mRepeatOnce;
    public void onClickRepeat(View v) {
        TextView tv_repeat = (TextView)findViewById(R.id.text_repeat);
        mRepeatOnce = !mRepeatOnce;
        String repeat = mRepeatOnce?getResources().getString(R.string.str_repeatone) : getResources().getString(R.string.str_repeatalways);
        tv_repeat.setText(repeat);
        int selPos = mSpinner_Configlist.getSelectedItemPosition();

        if (mConfigItems != null && mConfigItems.size() > selPos && selPos>=0) {
            mConfigItems.get(selPos).mRepeatType = (mRepeatOnce?TouchConfigItem.REPEAT_NONE:TouchConfigItem.REPEAT_ALWAYS);
        }
    }
    private void showActionList(int index) {
        if (mConfigItems != null && mConfigItems.size() > index && index>=0) {
            mSpinner_Configlist.setSelection(index);
            mCurrentActionItems = mConfigItems.get(index).mActions;
            mActionAdapter.setData(mCurrentActionItems);
            enableControlButton(true);
            if (mCurrentActionItems != null && mCurrentActionItems.size()>0) {
                btn_apply.setEnabled(true);
            } else {
                btn_apply.setEnabled(false);
            }
            TextView tv_repeat = (TextView)findViewById(R.id.text_repeat);
            mRepeatOnce = mConfigItems.get(index).mRepeatType == TouchConfigItem.REPEAT_NONE;
            tv_repeat.setText(mRepeatOnce?getResources().getString(R.string.str_repeatone):getResources().getString(R.string.str_repeatalways));
        }
    }
/*
    public void changeName(View v) {
        final int selPos = mSpinner_Configlist.getSelectedItemPosition();
        if (selPos >= 0 && selPos < mConfigItems.size()) {
            final EditText et = new EditText(this);
            et.setText(mConfigItems.get(selPos).mName);
            new AlertDialog.Builder(this).setTitle("修改名称")
                    .setIcon(android.R.drawable.sym_def_app_icon)
                    .setView(et)
                    .setCancelable(false)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mConfigItems.get(selPos).mName = et.getText().toString();
                            updateConfigList();
                            Utils.saveTimerActionConfigs(TouchConfigActivity.this, mConfigItems);
                        }
                    }).setNegativeButton("取消", null).show();
        }
    }
*/
    private void initDialog() {
        mActionConfigView = getLayoutInflater().inflate(R.layout.actionconfig, null);
        initControl();
    }


    public void apply(View v) {
        int selPos = mSpinner_Configlist.getSelectedItemPosition();
        TouchConfigItem currentItem = mConfigItems.get(selPos);
        if (currentItem.mRepeatType == TouchConfigItem.REPEAT_ALWAYS && !actionListHasTimer(currentItem)) {
            Toast.makeText(TouchConfigActivity.this, R.string.needtimer, Toast.LENGTH_SHORT).show();
            return;
        }
        if (selPos < mConfigItems.size()) {
            Utils.mCurrentActiveConfigIndex = selPos;
            MyTouchAssistService.mRunning = false;
            Utils.mMainLayout.setVisibility(View.VISIBLE);
            //CustomButtonConfig.closeWidgetWhenApply();
            finish();
        }
    }

    private boolean actionListHasTimer(TouchConfigItem item) {
        int delay = 0;
        for (TouchConfigItem.ActionItem action : item.mActions) {
            if (action.mAction == TouchConfigItem.ACTION_DELAY) {
                if (action.mRandom) {
                    delay = action.mRandDelay_High - action.mRandDelay_Low;
                } else {
                    delay = action.mDelayTime;
                }
            }
        }
        return (delay > 0);
    }

    public void delete(View v) {
        new AlertDialog.Builder(this).setTitle(R.string.confirmDelete)
                .setCancelable(false)
                .setPositiveButton(R.string.str_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int selPos = mSpinner_Configlist.getSelectedItemPosition();
                        if (selPos>=0 && selPos<mConfigItems.size()) {
                            mConfigItems.remove(selPos);
                            updateConfigList();
                            if (mSpinner_Configlist.getCount() <= 1) {
                                btn_deleteConfig.setEnabled(false);
                            }
                        }
                    }
                }).setNegativeButton(R.string.str_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.mMainLayout.setVisibility(View.VISIBLE);
        //CustomButtonConfig.onDestroy();
    }

    AlertDialog upgradeDialog = null;
    public void addAction(View v) {
        int selPos = mSpinner_Configlist.getSelectedItemPosition();
        if (mConfigItems.get(selPos).mActions.size() >= ACTION_MAX_LIMIT) {
            //handleBCode();
            Toast.makeText(TouchConfigActivity.this, R.string.str_maxactions, Toast.LENGTH_SHORT).show();
            return;
        }
        doAddAction();
    }
    private void handleBCode() {
        /*
            new AlertDialog.Builder(this).setTitle(R.string.str_update)
                    .setMessage(R.string.str_askupgrade)
                    .setCancelable(false)
                    .setPositiveButton(R.string.str_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String uid = Utils.generateUniqueDeviceId(TouchConfigActivity.this);
                            if (TextUtils.isEmpty(uid)) {
                                Toast.makeText(TouchConfigActivity.this, R.string.str_readphone, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            View upgradeView = getLayoutInflater().inflate(R.layout.upgrade, null);
                            upgradeDialog = new AlertDialog.Builder(TouchConfigActivity.this).setTitle(R.string.upgrade_method)
                                    .setView(upgradeView)
                                    .setCancelable(false)
                                    .setPositiveButton(R.string.str_cancel, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            edit_upcode = null;
                                            upgradeDialog = null;
                                        }
                                    }).show();
                            edit_upcode = (EditText) upgradeView.findViewById(R.id.text_upgradecode);
                            TextView tt_bcode2 = (TextView) upgradeView.findViewById(R.id.text_bcode);
                            String first8uid = uid.substring(0, 8);
                            String t2 = String.format(getResources().getString(R.string.bcode2), first8uid);
                            newBCode = first8uid;
                            tt_bcode2.setText(t2);
                        }
                    }).setNegativeButton(R.string.str_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            }).show();*/
    }

    private void doAddAction() {
        final String[] randArray = getResources().getStringArray(R.array.timerlist);
        wheel_selIndex_start = 0;
        wheel_selIndex_end = 1;
        initDialog();
//        spinner_start.setSelection(0);
//        spinner_end.setSelection(0);

        new AlertDialog.Builder(this).setTitle(R.string.str_new)
                .setIcon(android.R.drawable.sym_def_app_icon)
                .setView(mActionConfigView)
                .setCancelable(false)
                .setPositiveButton(R.string.str_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        saveAddedActionItem();
                        Utils.hideTouchIcons();
                        mTouchIconIsShwon = false;
                        if (wheel_selIndex_end <= wheel_selIndex_start) {
                            Toast.makeText(getApplicationContext(), R.string.second_time_bigger, Toast.LENGTH_SHORT).show();
                            btn_apply.setEnabled(false);
                            return;
                        } else {
                            btn_apply.setEnabled(true);
                        }
                    }
                }).setNegativeButton(R.string.str_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Utils.hideTouchIcons();
                mTouchIconIsShwon = false;
            }
        }).show();

        onRadioClicked(btn_timer);
        btn_fixedtimer.setChecked(true);
        btn_randomtimer.setChecked(false);
        tv_randstart.setText(randArray[wheel_selIndex_start]);
        tv_randend.setText(randArray[wheel_selIndex_end]);
        mIsAdding = true;
    }


    TouchConfigItem.ActionItem editingItem;
    private void editAction(final int index) {
        initDialog();
        final String[] randArray = getResources().getStringArray(R.array.timerlist);
        int selPos = mSpinner_Configlist.getSelectedItemPosition();
        editingItem = mConfigItems.get(selPos).mActions.get(index);
        switch (editingItem.mAction) {
            case TouchConfigItem.ACTION_CLICK:
                onRadioClicked(btn_click);
                tv_click_x.setText(String.valueOf(editingItem.mX1));
                tv_click_y.setText(String.valueOf(editingItem.mY1));
                break;
            case TouchConfigItem.ACTION_DELAY:
                onRadioClicked(btn_timer);
                if (editingItem.mRandom) {
                    int lowIndex = 0;
                    int highIndex = 0;
                    for (lowIndex=0;lowIndex<delayTimer.length;lowIndex++) {
                        if (delayTimer[lowIndex] == editingItem.mRandDelay_Low) {
                            break;
                        }
                    }
                    for (highIndex=0;highIndex<delayTimer.length;highIndex++) {
                        if (delayTimer[highIndex] == editingItem.mRandDelay_High) {
                            break;
                        }
                    }
                    wheel_selIndex_start = lowIndex;
                    wheel_selIndex_end = highIndex;
                    tv_randstart.setText(randArray[wheel_selIndex_start]);
                    tv_randend.setText(randArray[wheel_selIndex_end]);
                    btn_fixedtimer.setChecked(false);
                    btn_randomtimer.setChecked(true);
                } else {
                    tv_timer.setText(String.valueOf(editingItem.mDelayTime));
                    btn_randomtimer.setChecked(false);
                    btn_fixedtimer.setChecked(true);
                }
                break;
            case TouchConfigItem.ACTION_SWIPE:
                onRadioClicked(btn_swipe);
                tv_swipe_x1.setText(String.valueOf(editingItem.mX1));
                tv_swipe_y1.setText(String.valueOf(editingItem.mY1));
                tv_swipe_x2.setText(String.valueOf(editingItem.mX2));
                tv_swipe_y2.setText(String.valueOf(editingItem.mY2));
                break;
            case TouchConfigItem.ACTION_DOUBLECLICK:

                tv_click_x.setText(String.valueOf(editingItem.mX1));
                tv_click_y.setText(String.valueOf(editingItem.mY1));
                break;
        }
        new AlertDialog.Builder(this).setTitle(R.string.str_edit)
                .setIcon(android.R.drawable.sym_def_app_icon)
                .setView(mActionConfigView)
                .setCancelable(false)
                .setPositiveButton(R.string.str_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        saveEditingActionItem(index);
                        Utils.hideTouchIcons();
                        mTouchIconIsShwon = false;
                        if (wheel_selIndex_end <= wheel_selIndex_start) {
                            Toast.makeText(getApplicationContext(), R.string.second_time_bigger, Toast.LENGTH_SHORT).show();
                            btn_apply.setEnabled(false);
                            return;
                        } else {
                            btn_apply.setEnabled(true);
                        }
                    }
                }).setNegativeButton(R.string.str_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Utils.hideTouchIcons();
                mTouchIconIsShwon = false;
            }
        }).show();
        mIsAdding = false;
    }

    boolean mTouchIconIsShwon=false;
    public void onTouchIconClicked(View v) {
//        Intent intent = new Intent();
//        intent.setClass(this, TouchPointActivity.class);
//        startActivityForResult(intent, 10999);
        if (mTouchIconIsShwon) {
            Utils.hideTouchIcons();
            mTouchIconIsShwon = false;
            return;
        }
        if (btn_click.isChecked()) {
            showClickIcon();
            mTouchIconIsShwon = true;
        } else if (btn_swipe.isChecked()) {
            showSwipeIcon();
            mTouchIconIsShwon = true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == 10999) {
            int x1 = data.getIntExtra("X1", 0);
            int y1 = data.getIntExtra("Y1", 0);
            int x2 = data.getIntExtra("X2", 0);
            int y2 = data.getIntExtra("Y2", 0);

           if (btn_click.isChecked()) {
                tv_click_x.setText(String.valueOf(x1));
                tv_click_y.setText(String.valueOf(y1));
            } else if (btn_swipe.isChecked()) {
                tv_swipe_x1.setText(String.valueOf(x1));
                tv_swipe_y1.setText(String.valueOf(y1));
                tv_swipe_x2.setText(String.valueOf(x2));
                tv_swipe_y2.setText(String.valueOf(y2));
            }

        }
    }

    private void saveEditingActionItem(int index) {
        int selPos = mSpinner_Configlist.getSelectedItemPosition();
        mConfigItems.get(selPos).mActions.remove(index);
        mConfigItems.get(selPos).mActions.add(index, getItemFromUI());
        showActionList(selPos);
    }

    private TouchConfigItem.ActionItem getItemFromUI() {
        int actionType;
        TouchConfigItem.ActionItem newItem = null;
        if (btn_click.isChecked()) {
            actionType = btn_click.isChecked()?TouchConfigItem.ACTION_CLICK:TouchConfigItem.ACTION_DOUBLECLICK;
            int x = Integer.valueOf(tv_click_x.getText().toString());
            int y = Integer.valueOf(tv_click_y.getText().toString());
            newItem = new TouchConfigItem.ActionItem(actionType, x, y);
        } else if (btn_swipe.isChecked()) {
            actionType = TouchConfigItem.ACTION_SWIPE;
            int x1 = Integer.valueOf(tv_swipe_x1.getText().toString());
            int y1 = Integer.valueOf(tv_swipe_y1.getText().toString());
            int x2 = Integer.valueOf(tv_swipe_x2.getText().toString());
            int y2 = Integer.valueOf(tv_swipe_y2.getText().toString());
            newItem = new TouchConfigItem.ActionItem(actionType, x1, y1, x2, y2);
        } else if (btn_timer.isChecked()) {
            actionType = TouchConfigItem.ACTION_DELAY;
            if (btn_randomtimer.isChecked()) {
                int time_low = delayTimer[wheel_selIndex_start];
                int time_high = delayTimer[wheel_selIndex_end];
                newItem = new TouchConfigItem.ActionItem(actionType, true, time_low, time_high);
            } else if (btn_fixedtimer.isChecked()) {
                int time = Integer.valueOf(tv_timer.getText().toString());
                newItem = new TouchConfigItem.ActionItem(actionType, time);
            }
        }
        return newItem;
    }

    private void saveAddedActionItem() {
        int selPos = mSpinner_Configlist.getSelectedItemPosition();
        if (selPos >= 0) {
            mConfigItems.get(selPos).mActions.add(getItemFromUI());
            showActionList(selPos);
        }
    }


    private void initControl() {
        btn_timer = (RadioButton) mActionConfigView.findViewById(R.id.radioButton);
        btn_click = (RadioButton) mActionConfigView.findViewById(R.id.radioButton2);
        btn_swipe = (RadioButton) mActionConfigView.findViewById(R.id.radioButton3);
//        btn_dbclick = (RadioButton) mActionConfigView.findViewById(R.id.radioButton4);
        btn_fixedtimer = (RadioButton) mActionConfigView.findViewById(R.id.radio_fixedDelay);
        btn_randomtimer = (RadioButton) mActionConfigView.findViewById(R.id.radio_randomDelay);

        layout_timer = (LinearLayout) mActionConfigView.findViewById(R.id.layout_timer);
        layout_click = (LinearLayout) mActionConfigView.findViewById(R.id.layout_click);
        layout_swipe = (LinearLayout) mActionConfigView.findViewById(R.id.layout_swipe);


        tv_timer = (TextView) mActionConfigView.findViewById(R.id.text_timer);
        tv_click_x = (TextView) mActionConfigView.findViewById(R.id.text_clickx);
        tv_click_y = (TextView) mActionConfigView.findViewById(R.id.text_clicky);

        tv_swipe_x1 = (TextView) mActionConfigView.findViewById(R.id.text_x1);
        tv_swipe_y1 = (TextView) mActionConfigView.findViewById(R.id.text_y1);
        tv_swipe_x2 = (TextView) mActionConfigView.findViewById(R.id.text_x2);
        tv_swipe_y2 = (TextView) mActionConfigView.findViewById(R.id.text_y2);


        imgView_touch = (ImageView)mActionConfigView.findViewById(R.id.imageView_touch);

        btn_fixedtimer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                btn_randomtimer.setChecked(!isChecked);
            }
        });
        btn_randomtimer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                btn_fixedtimer.setChecked(!isChecked);
            }
        });
        spinner_start = mActionConfigView.findViewById(R.id.spinner_start);
        spinner_end = mActionConfigView.findViewById(R.id.spinner_end);

        tv_randstart = mActionConfigView.findViewById(R.id.rand_start);
        tv_randend = mActionConfigView.findViewById(R.id.rand_end);
    }
    int wheel_selIndex_start = -1;
    int wheel_selIndex_end = 0;
    private void startWheelView(final boolean start) {
        final int offset = 0;
        final String[] randArray = getResources().getStringArray(R.array.timerlist);
        View outerView = LayoutInflater.from(this).inflate(R.layout.wheel_view, null);
        WheelView wv = (WheelView) outerView.findViewById(R.id.wheel_view);
        wv.setItems(Arrays.asList(getResources().getStringArray(R.array.timerlist)));
        wv.setOffset(offset);
        wv.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
            @Override
            public void onSelected(int selectedIndex, String item) {
                Log.d("bely", "[Dialog]selectedIndex: " + selectedIndex + ", item: " + item + ","+start);
                if (start) {
                    wheel_selIndex_start = selectedIndex-offset;
                } else {
                    wheel_selIndex_end = selectedIndex-offset;
                }
            }
        });
        if (start) {
            wv.setSeletion(wheel_selIndex_start);
        } else {
            wv.setSeletion(wheel_selIndex_end);
        }
        new AlertDialog.Builder(this)
                .setTitle(start?R.string.selectmintimer:R.string.selectmaxtimer)
                .setView(outerView)
                .setPositiveButton(R.string.str_ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (start) {
                            tv_randstart.setText(randArray[wheel_selIndex_start]);
                        } else {
                            tv_randend.setText(randArray[wheel_selIndex_end]);
                        }
                        if (wheel_selIndex_end <= wheel_selIndex_start) {
                            Toast.makeText(getApplicationContext(), R.string.second_time_bigger, Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setCancelable(false)
                .show();

    }
    public void onClickRandStart(View v) {
        startWheelView(true);
    }

    public void onClickRandEnd(View v) {
        startWheelView(false);
    }
    private void enableControlButton(boolean enable) {
        btn_deleteConfig.setEnabled(mConfigItems.size()>1);
        if(!enable) {
            btn_apply.setEnabled(enable);
        }
        btn_addAction.setEnabled(enable);
    }

    private View lastCheckedButton;
    public void onRadioClicked(View v) {
        if (lastCheckedButton == v) {
            return;
        }

        ((RadioButton) v).setChecked(true);
        lastCheckedButton = v;
        Utils.hideTouchIcons();
        mTouchIconIsShwon = false;
        if (v == btn_timer) {

            btn_click.setChecked(false);
            btn_swipe.setChecked(false);

            layout_timer.setVisibility(View.VISIBLE);
            layout_click.setVisibility(View.GONE);
            layout_swipe.setVisibility(View.GONE);

            imgView_touch.setVisibility(View.INVISIBLE);

        } else if (v == btn_click) {

            btn_timer.setChecked(false);
            btn_swipe.setChecked(false);
            layout_timer.setVisibility(View.GONE);
            layout_click.setVisibility(View.VISIBLE);
            layout_swipe.setVisibility(View.GONE);

            imgView_touch.setVisibility(View.VISIBLE);

        } else if (v == btn_swipe) {

            btn_click.setChecked(false);
            btn_timer.setChecked(false);

            layout_timer.setVisibility(View.GONE);
            layout_click.setVisibility(View.GONE);
            layout_swipe.setVisibility(View.VISIBLE);

            imgView_touch.setVisibility(View.VISIBLE);
        }
    }

    private void showClickIcon() {
        Utils.showClickIcon(true, Integer.valueOf(tv_click_x.getText().toString()), Integer.valueOf(tv_click_y.getText().toString()) - Utils.mStatusBarHeight, 0);
    }

    private void showSwipeIcon() {
        Utils.showSwipeIcon(true, Integer.valueOf(tv_swipe_x1.getText().toString()), Integer.valueOf(tv_swipe_y1.getText().toString()) - Utils.mStatusBarHeight,
                Integer.valueOf(tv_swipe_x2.getText().toString()), Integer.valueOf(tv_swipe_y2.getText().toString()) - Utils.mStatusBarHeight);
    }



    public void addOrReduce(View v) {


        switch (v.getId()) {
            case R.id.button_reducetimer:
                int time = Integer.valueOf(tv_timer.getText().toString());
                if (time > 1) {
                    time--;
                } else {
                    time = 1;
                }
                tv_timer.setText(String.valueOf(time));
                break;
            case R.id.button_addtimer:
                time = Integer.valueOf(tv_timer.getText().toString());
                time++;
                if (time>1800) {
                    time = 1800;
                }
                tv_timer.setText(String.valueOf(time));
                break;
        }
    }

    @Override
    public void onClickPosChanged(final int x, final int y, int tag) {
        if (tag != 0 || tv_click_x == null) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (btn_click.isChecked()) {
                    tv_click_x.setText(String.valueOf(x));
                    tv_click_y.setText(String.valueOf(y));
                } else if (btn_swipe.isChecked()) {
                    tv_swipe_x1.setText(String.valueOf(x));
                    tv_swipe_y1.setText(String.valueOf(y));
                    Utils.mInitialDrawLine = true;
                    drawSwipeLine();
                }
            }
        });
    }

    private void drawSwipeLine() {
        Utils.drawSwipeLine(Integer.valueOf(tv_swipe_x1.getText().toString()), Integer.valueOf(tv_swipe_y1.getText().toString()),
                Integer.valueOf(tv_swipe_x2.getText().toString()), Integer.valueOf(tv_swipe_y2.getText().toString()));
    }

    @Override
    public void onSwipeEndChange(final int x, final int y, int tag) {
        if (btn_swipe == null) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (btn_swipe.isChecked()) {
                    tv_swipe_x2.setText(String.valueOf(x));
                    tv_swipe_y2.setText(String.valueOf(y));
                    Utils.mInitialDrawLine = true;
                    drawSwipeLine();
                }
            }
        });
    }

    private class ActionListAdapter extends DragListViewAdapter {


        @Override
        public View getItemView(int pos, View v, ViewGroup root) {

            View itemView = getLayoutInflater().inflate(R.layout.actionitem, null);

            TouchConfigItem.ActionItem action = (TouchConfigItem.ActionItem)getItem(pos);

            TextView tv = (TextView) itemView.findViewById(R.id.actionname);

            String info = null;
            switch (action.mAction) {
                case TouchConfigItem.ACTION_CLICK:
                    info = getResources().getString(R.string.str_clickat) + action.mX1 + "," + action.mY1 + ")";
                    break;
                case TouchConfigItem.ACTION_DELAY:
                    if (action.mRandom) {
                        info = getResources().getString(R.string.delay_random) + action.mRandDelay_Low + getResources().getString(R.string.str_to) +action.mRandDelay_High +  getResources().getString(R.string.str_second);
                    } else {
                        info = getResources().getString(R.string.str_delaytime) + action.mDelayTime + getResources().getString(R.string.str_second);
                    }
                    break;
                case TouchConfigItem.ACTION_LONGCLICK:
                    info = "Long Click @ (" + action.mX1 + "," + action.mY1 + ")";
                    break;
                case TouchConfigItem.ACTION_SWIPE:
                    info = getResources().getString(R.string.str_swipe) + action.mX1 + "," + action.mY1 + getResources().getString(R.string.str_swipeto) + action.mX2 + "," + action.mY2 + ")";
                    break;
                case TouchConfigItem.ACTION_DOUBLECLICK:
                    info = "Double Click @ (" + action.mX1 + "," + action.mY1 + ")";
                    break;
            }
            tv.setText(info);
/*
            tv.setTag(pos);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int index = (int) view.getTag();
                    editAction(index);
                }
            });
*/
            ImageView btn_del = itemView.findViewById(R.id.btn_deleteaction);
            btn_del.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final int selPos = mSpinner_Configlist.getSelectedItemPosition();
                    final int actionIndex =(int) view.getTag();
                    if (selPos >= 0 && selPos < mConfigItems.size() && mConfigItems.get(selPos).mActions != null && mConfigItems.get(selPos).mActions.size()>actionIndex) {

                        new AlertDialog.Builder(TouchConfigActivity.this).setTitle(R.string.confirmDelete)
                                .setIcon(android.R.drawable.sym_def_app_icon)
                                .setCancelable(false)
                                .setPositiveButton(R.string.str_ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        mConfigItems.get(selPos).mActions.remove(actionIndex);
                                        showActionList(selPos);
                                        Toast.makeText(TouchConfigActivity.this, R.string.str_itemdeleted, Toast.LENGTH_SHORT).show();

                                    }
                                }).setNegativeButton(R.string.str_cancel, null).show();



                    }
                }
            });
            btn_del.setTag(pos);


            return itemView;
        }

    }



}
