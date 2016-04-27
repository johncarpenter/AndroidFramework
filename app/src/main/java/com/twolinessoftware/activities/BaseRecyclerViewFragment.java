/*
 * Copyright (c) 2016. Petrofeed Inc
 *
 *  All Rights Reserved.
 *
 *  NOTICE:  All information contained herein is, and remains
 *  the property of Petrofeed Inc and its suppliers,
 *  if any.  The intellectual and technical concepts contained
 *  herein are proprietary to Petrofeed Inc
 *  and its suppliers and may be covered by U.S. and Foreign Patents,
 *  patents in process, and are protected by trade secret or copyright law.
 *  Dissemination of this information or reproduction of this material
 *  is strictly forbidden unless prior written permission is obtained
 *  from Petrofeed Inc.
 *
 */

package com.twolinessoftware.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.twolinessoftware.R;
import com.twolinessoftware.adapters.AdapterLifecycleInterface;

import butterknife.Bind;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by John on 2015-04-01.
 */
public abstract class BaseRecyclerViewFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    @Bind(R.id.refresh_layout)
    protected SwipeRefreshLayout mSwipeRefreshLayout;

    @Bind(R.id.list_master)
    RecyclerView mMasterListView;

    @Bind(R.id.layout_empty)
    ViewGroup mEmptyLayoutView;

    private boolean mOnViewCreatedCalled;

    protected RecyclerView.Adapter mAdapter;

    protected RecyclerView.LayoutManager mLayoutManager;

    @SuppressLint("HandlerLeak")
    public final Handler mDelayShowEmptyHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            setEmptyViewVisibile((mAdapter.getItemCount() == 0));
        }
    };
    private boolean m_emptyViewEnabled;
    private EmptyDataObserver m_dataObserver;

    @Override
    protected int setContentView() {
        return R.layout.fragment_listview;
    }


    private class EmptyDataObserver extends RecyclerView.AdapterDataObserver {
        @Override
        public void onChanged() {
            setRefreshing(false);

            // Item loading may be delayed, lets not show the empty right away
            mDelayShowEmptyHandler.sendEmptyMessageDelayed(0, 250);
        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {


        mSwipeRefreshLayout.setOnRefreshListener(this);
        mOnViewCreatedCalled = true;
        setRefreshable(false);

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        if ( !mOnViewCreatedCalled ) {
            throw new RuntimeException("RecyclerFragment requires super.onViewCreated");
        }

        if ( mAdapter != null ) {
            ((AdapterLifecycleInterface) mAdapter).onResume();
            m_dataObserver = new EmptyDataObserver();
            mAdapter.registerAdapterDataObserver(m_dataObserver);
        }

    }

    @Override
    public void onPause() {
        super.onPause();

        if ( mAdapter != null ) {
            ((AdapterLifecycleInterface) mAdapter).onPause();
        }
        mDelayShowEmptyHandler.removeMessages(0);

        if ( mAdapter != null && m_dataObserver != null ) {
            mAdapter.unregisterAdapterDataObserver(m_dataObserver);
        }

    }

    public void setEmptyViewEnable(boolean enabled) {
        if ( !mOnViewCreatedCalled ) {
            throw new RuntimeException("RecyclerFragment requires super.onViewCreated");
        }
        m_emptyViewEnabled = enabled;
    }

    public void setRefreshable(boolean refreshable) {

        if ( !mOnViewCreatedCalled ) {
            throw new RuntimeException("RecyclerFragment requires super.onViewCreated");
        }

        mSwipeRefreshLayout.setEnabled(refreshable);
    }

    public void setRefreshing(boolean refreshing) {

        if ( !mOnViewCreatedCalled ) {
            throw new RuntimeException("RecyclerFragment requires super.onViewCreated");
        }

        new Handler(Looper.getMainLooper()).post(() -> {
            if ( mSwipeRefreshLayout != null ) {
                mSwipeRefreshLayout.setRefreshing(refreshing);
            }
        });
    }

    public void setEmptyViewVisibile(boolean visible) {
        if ( m_emptyViewEnabled ) {
            Observable.create(subscriber -> {
                        mMasterListView.setVisibility(visible ? View.GONE : View.VISIBLE);
                        mEmptyLayoutView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        subscriber.onCompleted();
                    }
            )
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe();
        }
    }

    public ViewGroup getEmptyView() {
        return mEmptyLayoutView;
    }

    @Override
    public void onRefresh() {

    }

}
