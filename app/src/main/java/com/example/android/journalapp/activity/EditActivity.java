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

public class EditActivity extends AppCompatActivity {


    private List<Journal> journalList = new ArrayList<>();
    private DatabaseHelper db;

    private int position;

    private EditText entryTitleEt;
    private EditText entryTextEt;

    private TextView wordWatcher;
    private TextView charWatcher;

    TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            charWatcher.setText("Chars:" + String.valueOf(s.length()));
            wordWatcher.setText("Words:" + wordCount(s.toString()));
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            charWatcher.setText("Chars:" + String.valueOf(s.length()));
            wordWatcher.setText("Words:" + wordCount(s.toString()));

        }

        @Override
        public void afterTextChanged(Editable s) {

            charWatcher.setText("Chars:" + String.valueOf(s.length()));
            wordWatcher.setText("Words:" + wordCount(s.toString()));

        }
    };

    public static long wordCount(String line) {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = new DatabaseHelper(this);

        journalList.addAll(db.getAllJournals());


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(EditActivity.this);

        String dateStamp = sharedPref.getString("dateStamp", "");
        String timeStamp = sharedPref.getString("timeStamp", "");
        String journalTitle = sharedPref.getString("journalTitle", "");
        String journalText = sharedPref.getString("journalText", "");
        position = sharedPref.getInt("position", 0);

        TextView date = findViewById(R.id.journal_date);
        TextView time = findViewById(R.id.journal_time);

        entryTitleEt = findViewById(R.id.entry_title_et);
        entryTextEt = findViewById(R.id.entry_text_et);


        date.setText(formatDate(dateStamp));
        time.setText(timeStamp);
        entryTitleEt.setText(journalTitle);
        entryTextEt.setText(journalText);

        charWatcher = findViewById(R.id.char_watcher);
        wordWatcher = findViewById(R.id.word_watcher);

        entryTextEt.addTextChangedListener(textWatcher);

        ImageButton doneBtn = findViewById(R.id.done_btn);


        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Show toast message when no text is entered
                if (TextUtils.isEmpty(entryTitleEt.getText().toString())) {

                    Toast.makeText(getApplicationContext(), R.string.enter_title, Toast.LENGTH_SHORT).show();

                } else if (TextUtils.isEmpty(entryTextEt.getText().toString())) {

                    Toast.makeText(getApplicationContext(), R.string.enter_text, Toast.LENGTH_SHORT).show();

                } else {

                    // create new journal
                    updateJournal(entryTitleEt.getText().toString().trim(), entryTextEt.getText().toString().trim(), position);

                    startActivity(new Intent(EditActivity.this, HomeActivity.class));
                    finish();

                    Toast.makeText(getApplicationContext(), R.string.journal_saved, Toast.LENGTH_SHORT).show();

                }

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
