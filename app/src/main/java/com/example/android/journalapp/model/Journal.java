package com.example.android.journalapp.model;

public class Journal {

    public static final String TABLE_NAME = "journal";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_ENTRY = "entry";
    public static final String COLUMN_DATESTAMP = "datestamp";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_TITLE + " TEXT,"
                    + COLUMN_ENTRY + " TEXT,"
                    + COLUMN_DATESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                    + COLUMN_TIMESTAMP + " TEXT"
                    + ")";

    private int journalId;
    private String journalTitle;
    private String journalText;
    private String dateStamp;
    private String timeStamp;

    public Journal() {
    }

    public Journal(int journalId, String journalTitle, String journalText, String dateStamp, String timeStamp) {
        this.journalId = journalId;
        this.journalTitle = journalTitle;
        this.journalText = journalText;
        this.dateStamp = dateStamp;
        this.timeStamp = timeStamp;
    }

    public int getJournalId() {
        return journalId;
    }

    public void setJournalId(int journalId) {
        this.journalId = journalId;
    }

    public String getJournalTitle() {
        return journalTitle;
    }

    public void setJournalTitle(String journalTitle) {
        this.journalTitle = journalTitle;
    }

    public String getJournalText() {
        return journalText;
    }

    public void setJournalText(String journalText) {
        this.journalText = journalText;
    }

    public String getDateStamp() {
        return dateStamp;
    }

    public void setDateStamp(String dateStamp) {
        this.dateStamp = dateStamp;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
