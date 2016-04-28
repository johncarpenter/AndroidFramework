/*
 * Copyright 2016 2LinesSoftware Inc
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.twolinessoftware.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.joda.time.DateTime;

import nl.qbusict.cupboard.convert.EntityConverter;
import nl.qbusict.cupboard.convert.FieldConverter;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

/**
 *
 */
public class ApplicationDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "application.db";
    private static final int DATABASE_VERSION = 1;

    public ApplicationDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    static {
        // register our models
        // cupboard().register(User.class);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // this will ensure that all tables are created
        cupboard().withDatabase(db).createTables();
        // add indexes and other database tweaks in this method if you want

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // this will upgrade tables, adding columns and new tables.
        // Note that existing columns will not be converted
        cupboard().withDatabase(db).upgradeTables();
        // do migration work if you have an alteration to make to your schema here

    }

    public static class JodaTimeConverter implements FieldConverter<DateTime> {

        public JodaTimeConverter() {
        }

        @Override
        public DateTime fromCursorValue(Cursor cursor, int index) {
            return new DateTime(cursor.getLong(index));
        }

        @Override
        public void toContentValue(DateTime dateTime, String key, ContentValues values) {
            values.put(key, dateTime.getMillis());
        }

        @Override
        public EntityConverter.ColumnType getColumnType() {
            return EntityConverter.ColumnType.INTEGER;
        }
    }

}