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
        cupboard().register(User.class);
        cupboard().register(Clinic.class);
        cupboard().register(Doctor.class);
        cupboard().register(Appointment.class);
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
            values.put(key,dateTime.getMillis());
        }

        @Override
        public EntityConverter.ColumnType getColumnType() {
            return EntityConverter.ColumnType.INTEGER;
        }
    }

}