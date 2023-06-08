package com.belyware.touchassist;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.Collections;
import java.util.List;

public abstract class DragListViewAdapter<T> extends BaseAdapter {

    public List<T> mDragDatas;

    public void setData(List<T> dataList) {
        this.mDragDatas = dataList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDragDatas == null ? 0 : mDragDatas.size();
    }

    @Override
    public T getItem(int position) {
        return mDragDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getItemView(position, convertView, parent);
    }

    public abstract View getItemView(int position, View convertView, ViewGroup parent);

    public void swapData(int from, int to){
        Collections.swap(mDragDatas, from, to);
        notifyDataSetChanged();
    }

    public void deleteData(int position) {
        mDragDatas.remove(position);
        notifyDataSetChanged();
    }
}