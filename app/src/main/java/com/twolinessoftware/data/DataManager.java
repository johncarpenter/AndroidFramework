package com.twolinessoftware.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import javax.inject.Inject;

/**
 *
 */
public class DataManager {

    private final SQLiteDatabase mDatabase;

    private final Context mContext;

    public SQLiteDatabase getDatabase() {
        return mDatabase;
    }

    @Inject
    public DataManager(Context context, SQLiteDatabase database) {
        mDatabase = database;
        mContext = context;
    }


}
