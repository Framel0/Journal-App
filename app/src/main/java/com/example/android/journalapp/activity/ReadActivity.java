package com.example.android.journalapp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.example.android.journalapp.R;
import com.example.android.journalapp.database.DatabaseHelper;
import com.example.android.journalapp.model.Journal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ReadActivity extends AppCompatActivity {

    private List<Journal> journalList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DatabaseHelper db = new DatabaseHelper(this);

        journalList.addAll(db.getAllJournals());

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ReadActivity.this);

        String dateStamp = sharedPref.getString("dateStamp", "");
        String timeStamp = sharedPref.getString("timeStamp", "");
        String journalTitle = sharedPref.getString("journalTitle", "");
        String journalText = sharedPref.getString("journalText", "");

        TextView date = findViewById(R.id.text_journal_date);
        TextView time = findViewById(R.id.text_journal_time);

        TextView entryTitle = findViewById(R.id.text_entry_title);
        TextView entryText = findViewById(R.id.text_entry_text);

        date.setText(formatDate(dateStamp));
        time.setText(timeStamp);
        entryTitle.setText(journalTitle);
        entryText.setText(journalText);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(ReadActivity.this, EditActivity.class);

                startActivity(i);

            }
        });
    }

    /**
     * Formatting timestamp to `dd/MMM/yyyy` format
     * Input: 2018-02-21 00:15:42
     * Output: 2/Jun/18
     */
    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.UK);
            Date date = fmt.parse(dateStr);
            SimpleDateFormat fmtOut = new SimpleDateFormat("dd/MMM/yyyy", Locale.UK);
            return fmtOut.format(date);
        } catch (ParseException ignored) {

        }

        return "";
    }


}
