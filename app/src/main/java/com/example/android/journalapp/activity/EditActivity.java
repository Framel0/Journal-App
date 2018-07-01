package com.example.android.journalapp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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

public class EditActivity extends AppCompatActivity {


    private List<Journal> journalList = new ArrayList<>();
    private DatabaseHelper db;

    private int position;

    private EditText entryTitle;
    private EditText entryText;

    private TextView wordWatcher;
    private TextView charWatcher;

    private static long wordCount(String line) {
        long numWords = 0;
        int index = 0;
        boolean prevWhiteSpace = true;
        while (index < line.length()) {
            char c = line.charAt(index++);
            boolean currWhiteSpace = Character.isWhitespace(c);
            if (prevWhiteSpace && !currWhiteSpace) {
                numWords++;
            }
            prevWhiteSpace = currWhiteSpace;
        }
        return numWords;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = new DatabaseHelper(this);

        journalList.addAll(db.getAllJournals());


        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(EditActivity.this);

        String dateStamp = sharedPref.getString("dateStamp", "");
        String timeStamp = sharedPref.getString("timeStamp", "");
        String journalTitle = sharedPref.getString("journalTitle", "");
        String journalText = sharedPref.getString("journalText", "");
        position = sharedPref.getInt("position", 0);

        TextView date = findViewById(R.id.text_journal_date);
        TextView time = findViewById(R.id.text_journal_time);

        entryTitle = findViewById(R.id.edit_entry_title);
        entryText = findViewById(R.id.edit_entry_text);


        date.setText(formatDate(dateStamp));
        time.setText(timeStamp);

        entryTitle.setText(journalTitle);
        entryText.setText(journalText);

        charWatcher = findViewById(R.id.text_char_watcher);
        wordWatcher = findViewById(R.id.text_word_watcher);

        entryText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                charWatcher.setText(String.format("%s%s", getString(R.string.characters_word), String.valueOf(s.length())));
                wordWatcher.setText(String.format(Locale.getDefault(), "%s%d", getString(R.string.words_word), wordCount(s.toString())));
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                charWatcher.setText(String.format("%s%s", getString(R.string.characters_word), String.valueOf(s.length())));
                wordWatcher.setText(String.format(Locale.getDefault(), "%s%d", getString(R.string.words_word), wordCount(s.toString())));
            }

            @Override
            public void afterTextChanged(Editable s) {

                charWatcher.setText(String.format("%s%s", getString(R.string.characters_word), String.valueOf(s.length())));
                wordWatcher.setText(String.format(Locale.getDefault(), "%s%d", getString(R.string.words_word), wordCount(s.toString())));
            }
        });

        ImageButton doneBtn = findViewById(R.id.btn_done);


        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Show toast message when no text is entered
                if (TextUtils.isEmpty(entryTitle.getText().toString())) {

                    Toast.makeText(getApplicationContext(), R.string.enter_title_toast, Toast.LENGTH_SHORT).show();

                } else if (TextUtils.isEmpty(entryText.getText().toString())) {

                    Toast.makeText(getApplicationContext(), R.string.enter_text_toast, Toast.LENGTH_SHORT).show();

                } else {

                    // create new journal
                    updateJournal(entryTitle.getText().toString().trim(), entryText.getText().toString().trim(), position);

                    startActivity(new Intent(EditActivity.this, HomeActivity.class));
                    finish();

                    Toast.makeText(getApplicationContext(), R.string.journal_saved_toast, Toast.LENGTH_SHORT).show();

                }

            }
        });
    }

    /**
     * Updating journal in db and updating
     * item in the list by its position
     */
    private void updateJournal(String title, String text, int position) {
        Journal n = journalList.get(position);
        // updating journal text
        n.setJournalTitle(title);
        n.setJournalText(text);

        // updating journal in db
        db.updateJournal(n);

    }


}
