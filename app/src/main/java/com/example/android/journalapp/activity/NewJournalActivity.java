package com.example.android.journalapp.activity;

import android.content.Intent;
import android.os.Bundle;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class NewJournalActivity extends AppCompatActivity {

    private List<Journal> journalList = new ArrayList<>();
    private DatabaseHelper db;

    private TextView journalTime;

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

        }

        @Override
        public void afterTextChanged(Editable s) {

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
        setContentView(R.layout.activity_new_journal);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = new DatabaseHelper(this);

        journalList.addAll(db.getAllJournals());

        Calendar mCalendar = Calendar.getInstance();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView journalDate = findViewById(R.id.journal_date);
        journalTime = findViewById(R.id.journal_time);

        entryTitleEt = findViewById(R.id.entry_title_et);
        entryTextEt = findViewById(R.id.entry_text_et);


        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MMM/yyyy", Locale.UK);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.UK);
        String formattedDate = dateFormat.format(mCalendar.getTime());
        String formattedTime = timeFormat.format(mCalendar.getTime());


        journalDate.setText(formattedDate);
        journalTime.setText(formattedTime);


        charWatcher = findViewById(R.id.char_watcher);
        wordWatcher = findViewById(R.id.word_watcher);

        charWatcher.setText("Chas:0");
        wordWatcher.setText("Words:0");

        entryTextEt.addTextChangedListener(textWatcher);

        ImageButton done = findViewById(R.id.done_btn);

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Show toast message when no text is entered
                if (TextUtils.isEmpty(entryTitleEt.getText().toString())) {

                    Toast.makeText(getApplicationContext(), R.string.enter_title, Toast.LENGTH_SHORT).show();

                } else if (TextUtils.isEmpty(entryTextEt.getText().toString())) {

                    Toast.makeText(getApplicationContext(), R.string.enter_text, Toast.LENGTH_SHORT).show();

                } else {

                    // create new journal
                    createJournal(entryTitleEt.getText().toString().trim(), entryTextEt.getText().toString().trim(), journalTime.getText().toString());

                    startActivity(new Intent(NewJournalActivity.this, HomeActivity.class));
                    finish();

                    Toast.makeText(getApplicationContext(), R.string.journal_saved, Toast.LENGTH_SHORT).show();

                }

            }
        });


    }

    /**
     * Inserting new journal in db
     */
    private void createJournal(String title, String text, String time) {
        // inserting journal in db and getting
        // newly inserted journal id
        long id = db.insertJournal(title, text, time);

        // get the newly inserted journal from db
        Journal n = db.getJournal(id);

        if (n != null) {
            // adding new journal to array list at 0 position
            journalList.add(0, n);

        }
    }

}
