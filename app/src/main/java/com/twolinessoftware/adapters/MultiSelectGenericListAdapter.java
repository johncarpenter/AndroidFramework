/*
 * Copyright (c) 2015. 2Lines Software,Inc
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.twolinessoftware.adapters;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.twolinessoftware.R;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by John on 2015-04-08.
 */
public abstract class MultiSelectGenericListAdapter<T> extends RecyclerView.Adapter<GenericListViewHolder> implements MultiSelectAdapter<T>, AdapterLifecycleInterface {

    protected final Context m_context;

    private Observable<List<T>> m_query;

    private SparseBooleanArray m_selectedItems = new SparseBooleanArray();

    private SparseBooleanArray m_filteredItems = new SparseBooleanArray();

    protected List<T> m_entries = new ArrayList<>();

    private final Handler m_refreshHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            notifyDataSetChanged();
        }
    };

    private Subscriber<List<T>> m_listChangesSubscriber = new ListChangesSubscriber();


    private class ListChangesSubscriber extends Subscriber<List<T>> {

        @Override
        public void onCompleted() {
            Timber.v("Refresh completed");
        }

        @Override
        public void onError(Throwable e) {
            Timber.e("Error loading data: " + Log.getStackTraceString(e));
        }

        @Override
        public void onNext(List<T> items) {

            onDataLoaded(items);

            //this works as a incremental update, might be useful if the list is large
            Timber.v("Updating list with " + items.size() + " items");


            synchronized (m_entries) {

                m_entries.clear();
                ;
                m_entries.addAll(items);

                m_selectedItems.clear();
                m_filteredItems.clear();

                m_refreshHandler.sendEmptyMessage(0);

            }

               /* synchronized (m_entries) {

                ArrayList<T> removed = new ArrayList<>(m_entries);
                removed.removeAll(items);

                Ln.v("Removing " + removed.size() + " items");

                if(notifyDeleted()){
                    for (T item : removed) {
                        notifyItemRemoved(getPositionForItem(item));
                    }
                }

                // delete the removed items from the current list
                m_entries.removeAll(removed);

                // determine the new items by removing the existing items from the list
                items.removeAll(m_entries);

                Ln.v("Adding " + items.size() + " items");

                int position = m_entries.size();
                m_entries.addAll(items);

                if(notifyInserted()){
                    notifyItemRangeInserted(position, items.size());
                }

                m_selectedItems.clear();
                m_filteredItems.clear();

                m_refreshHandler.sendEmptyMessage(0);
            }*/
        }
    }


    protected MultiSelectGenericListAdapter(Context context) {
        this.m_context = context;
        m_listChangesSubscriber = new ListChangesSubscriber();
    }

    protected MultiSelectGenericListAdapter(Context context, Observable<List<T>> query) {
        this.m_context = context;
        this.m_query = query;
        m_listChangesSubscriber = new ListChangesSubscriber();
    }

    public void setQuery(Observable<List<T>> query) {

        if ( m_listChangesSubscriber != null && !m_listChangesSubscriber.isUnsubscribed() ) {
            this.m_query = query;
            m_listChangesSubscriber.unsubscribe();
            m_listChangesSubscriber = new ListChangesSubscriber();
            registerObservers();
            refresh();
        }

    }

    public void setFilter(String filter) {
        Timber.v("Filtering on " + filter);

        if ( filter == null || filter.isEmpty() ) {
            m_filteredItems.clear();
            refresh();
        } else {
            m_filteredItems.clear();
            Observable.from(m_entries)
                    .filter(contact -> filterPass(contact, filter))
                    .subscribe(contact -> {
                                m_filteredItems.put(getPositionForItem(contact), true);
                            },
                            error -> {
                            },
                            () -> refresh());


        }
    }

    public boolean filterPass(T item, String filter) {
        return true;
    }


    public void refresh() {
        m_refreshHandler.sendEmptyMessage(0);
    }

    private void registerObservers() {

        if ( m_listChangesSubscriber != null ) {
            m_listChangesSubscriber.unsubscribe();
        }
        m_listChangesSubscriber = new ListChangesSubscriber();

        m_query
                .subscribeOn(Schedulers.newThread())
                .subscribe(m_listChangesSubscriber);
    }


    @Override
    public void onPause() {
        m_listChangesSubscriber.unsubscribe();
        onUnsubscribeObservers();
    }

    @Override
    public void onResume() {
        registerObservers();
        onRegisterObservers();
    }

    public void setSelected(int pos, boolean selected) {
        if ( selected ) {
            m_selectedItems.put(pos, true);
        } else if ( m_selectedItems.get(pos, false) ) {
            m_selectedItems.delete(pos);
        }

    }

    public void toggleSelection(int pos) {

        int originalPos = pos;

        if ( isFiltered() ) {
            pos = m_filteredItems.keyAt(pos);
        }

        if ( m_selectedItems.get(pos, false) ) {
            m_selectedItems.delete(pos);
        } else {
            m_selectedItems.put(pos, true);
        }
        notifyItemChanged(originalPos);
    }

    public void clearSelections() {
        m_selectedItems.clear();
        notifyDataSetChanged();
    }

    public int getSelectedItemCount() {
        return m_selectedItems.size();
    }

    public List<T> getSelectedItems() {
        List<T> items =
                new ArrayList<T>(m_selectedItems.size());
        for ( int i = 0; i < m_selectedItems.size(); i++ ) {
            items.add(m_entries.get(m_selectedItems.keyAt(i)));
        }
        return items;
    }

    public List<T> getAllItems() {
        return m_entries;
    }


    public boolean isSelected(int position) {
        if ( isFiltered() ) {
            return m_selectedItems.get(m_filteredItems.keyAt(position));
        } else {
            return m_selectedItems.get(position);
        }
    }

    public int getPositionForItem(T holder) {

        for ( int i = 0; i < m_entries.size(); i++ ) {
            if ( m_entries.get(i) == holder )
                return i;
        }
        return -1;
    }

    @Override
    public GenericListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_listview, parent, false);
        GenericListViewHolder vh = new GenericListViewHolder(v);
        return vh;
    }


    @Override
    public void onBindViewHolder(GenericListViewHolder holder, int position) {
        holder.icon.setActivated(m_selectedItems.get(position, false));

        T item;
        if ( isFiltered() ) {
            item = m_entries.get(m_filteredItems.keyAt(position));
        } else {
            item = m_entries.get(position);
        }
        holder.getView().setTag(item);


        onBindViewHolder(item, holder, position);
    }

    public boolean isFiltered() {
        return m_filteredItems.size() > 0;
    }

    @Override
    public int getItemCount() {
        return isFiltered() ? m_filteredItems.size() : m_entries.size();
    }

    abstract void onBindViewHolder(T item, GenericListViewHolder holder, int position);

    public void onDataLoaded(List<T> data) {
        if ( m_onDataLoadedListener != null ) {
            m_onDataLoadedListener.dataLoaded();
        }
    }

    public void onRegisterObservers() {
    }

    public void onUnsubscribeObservers() {
    }

    public boolean notifyInserted() {
        return false;
    }

    public boolean notifyDeleted() {
        return false;
    }

    /**
     * Data loading listeners
     */
    public interface OnDataLoadedListener {
        void dataLoaded();
    }

    private OnDataLoadedListener m_onDataLoadedListener;


    public OnDataLoadedListener getOnDataLoadedListener() {
        return m_onDataLoadedListener;
    }

    public void setOnDataLoadedListener(OnDataLoadedListener onDataLoadedListener) {
        this.m_onDataLoadedListener = onDataLoadedListener;
    }
}
