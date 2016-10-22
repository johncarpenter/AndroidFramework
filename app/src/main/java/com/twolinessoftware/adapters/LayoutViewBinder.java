package com.twolinessoftware.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 *
 */
public class LayoutViewBinder<T> extends RecyclerView.ViewHolder {

    private T mData;

    private int mPosition;

    private View mView;

    public LayoutViewBinder(View itemView) {
        super(itemView);
        mView = itemView;
    }

    public final void setData(T data) {
        mData = data;
    }


    public T getData() {
        return mData;
    }

    public View getView() {
        return mView;
    }

    public int getPositionIndex() {
        return mPosition;
    }

    public void setPositionIndex(int position) {
        mPosition = position;
    }
}
