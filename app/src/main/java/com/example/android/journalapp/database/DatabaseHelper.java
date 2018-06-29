package com.example.android.journalapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.journalapp.model.Journal;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "journal_db";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        // create journals table
        db.execSQL(Journal.CREATE_TABLE);

    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + Journal.TABLE_NAME);

        // Create tables again
        onCreate(db);
    }

    public long insertJournal(String title, String text, String time) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        // `id` and `timestamp` will be inserted automatically.
        // no need to add them
        values.put(Journal.COLUMN_TITLE, title);
        values.put(Journal.COLUMN_ENTRY, text);
        values.put(Journal.COLUMN_TIMESTAMP, time);

        // insert row
        long id = db.insert(Journal.TABLE_NAME, null, values);

        // close db connection
        db.close();

        // return newly inserted row id
        return id;
    }

    public Journal getJournal(long id) {
        // get readable database as we are not inserting anything
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(Journal.TABLE_NAME,
                new String[]{Journal.COLUMN_ID, Journal.COLUMN_TITLE, Journal.COLUMN_ENTRY, Journal.COLUMN_DATESTAMP, Journal.COLUMN_TIMESTAMP},
                Journal.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        // prepare journal object
        Journal journal = new Journal(
                cursor.getInt(cursor.getColumnIndex(Journal.COLUMN_ID)),
                cursor.getString(cursor.getColumnIndex(Journal.COLUMN_TITLE)),
                cursor.getString(cursor.getColumnIndex(Journal.COLUMN_ENTRY)),
                cursor.getString(cursor.getColumnIndex(Journal.COLUMN_DATESTAMP)),
                cursor.getString(cursor.getColumnIndex(Journal.COLUMN_TIMESTAMP)));

        // close the db connection
        cursor.close();

        return journal;
    }

    public List<Journal> getAllJournals() {
        List<Journal> journals = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + Journal.TABLE_NAME + " ORDER BY " +
                Journal.COLUMN_TIMESTAMP + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Journal journal = new Journal();
                journal.setJournalId(cursor.getInt(cursor.getColumnIndex(Journal.COLUMN_ID)));
                journal.setJournalTitle(cursor.getString(cursor.getColumnIndex(Journal.COLUMN_TITLE)));
                journal.setJournalText(cursor.getString(cursor.getColumnIndex(Journal.COLUMN_ENTRY)));
                journal.setDateStamp(cursor.getString(cursor.getColumnIndex(Journal.COLUMN_DATESTAMP)));
                journal.setTimeStamp(cursor.getString(cursor.getColumnIndex(Journal.COLUMN_TIMESTAMP)));

                journals.add(journal);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return journals list
        return journals;
    }

    public int getJournalsCount() {
        String countQuery = "SELECT  * FROM " + Journal.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();


        // return count
        return count;
    }

    public int updateJournal(Journal journal) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Journal.COLUMN_TITLE, journal.getJournalTitle());
        values.put(Journal.COLUMN_ENTRY, journal.getJournalText());

        // updating row
        return db.update(Journal.TABLE_NAME, values, Journal.COLUMN_ID + " = ?",
                new String[]{String.valueOf(journal.getJournalId())});
    }

    public void deleteJournal(Journal journal) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Journal.TABLE_NAME, Journal.COLUMN_ID + " = ?",
                new String[]{String.valueOf(journal.getJournalId())});
        db.close();
    }
}