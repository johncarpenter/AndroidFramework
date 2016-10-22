package com.twolinessoftware.adapters;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import rx.subjects.ReplaySubject;
import timber.log.Timber;

/**
 *
 */
public class RxAdapter<S> extends RecyclerView.Adapter<LayoutViewBinder<S>> {

    private int mChildLayoutResId;
    private Observable<List<S>> mDataSource;
    private ListChangesSubscriber mListChangesSubscriber;
    private ReplaySubject<LayoutViewBinder> mPublishSubjectData;
    private List<S> mEntries;


    private final Handler mRefreshHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            notifyDataSetChanged();
        }
    };

    public RxAdapter(@LayoutRes int childLayoutResId, Observable<List<S>> data) {
        mChildLayoutResId = childLayoutResId;
        mPublishSubjectData = ReplaySubject.create();
        mListChangesSubscriber = new ListChangesSubscriber();
        mEntries = new ArrayList<>();
        updateQuery(data);
    }

    public Observable<LayoutViewBinder> asObservable() {
        return mPublishSubjectData
                .doOnUnsubscribe(this::cleanup)
                .asObservable();
    }

    public void updateQuery(Observable<List<S>> newQuery) {
        Timber.v("Updating data query");
        mDataSource = newQuery;
        registerObservers();
    }

    public void refresh() {
        mRefreshHandler.sendEmptyMessage(0);
    }

    public LayoutViewBinder<S> getNewLayoutViewBinder(View view) {
        return new LayoutViewBinder<S>(view);
    }

    private class ListChangesSubscriber extends Subscriber<List<S>> {

        @Override
        public void onCompleted() {
            Timber.v("Refresh completed");
        }

        @Override
        public void onError(Throwable e) {
            Timber.e("Error loading data: " + Log.getStackTraceString(e));
        }

        @Override
        public void onNext(List<S> items) {

            Timber.v("Updating list with " + items.size() + " items");

            mEntries.clear();
            mEntries.addAll(items);

            refresh();

        }
    }

    private void cleanup() {
        Timber.v("Removing last subscriber");
        unregisterObservers();
    }

    private void registerObservers() {
        unregisterObservers();
        Timber.v("Registering data listeners");
        mListChangesSubscriber = new ListChangesSubscriber();

        mDataSource
                .subscribeOn(Schedulers.newThread())
                .subscribe(mListChangesSubscriber);
    }

    private void unregisterObservers() {

        if (mListChangesSubscriber != null) {
            Timber.v("Cleaning up data listeners");
            mListChangesSubscriber.unsubscribe();
        }
    }

    @Override
    public LayoutViewBinder<S> onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(mChildLayoutResId, parent, false);
        return getNewLayoutViewBinder(v);
    }

    @Override
    public void onBindViewHolder(LayoutViewBinder holder, int position) {
        S item = mEntries.get(position);

        holder.setPositionIndex(position);
        holder.setData(item);
        mPublishSubjectData.onNext(holder);
    }

    @Override
    public int getItemCount() {
        return mEntries.size();
    }
}
