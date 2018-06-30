package com.example.android.journalapp.activity;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.android.journalapp.MyDividerItemDecoration;
import com.example.android.journalapp.R;
import com.example.android.journalapp.RecyclerTouchListener;
import com.example.android.journalapp.adapter.JournalsAdapter;
import com.example.android.journalapp.database.DatabaseHelper;
import com.example.android.journalapp.model.Journal;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;
    private JournalsAdapter mAdapter;
    private List<Journal> journalList = new ArrayList<>();
    private TextView noJournalsView;
    private CoordinatorLayout coordinatorLayout;
    private View navHeader;
    private ImageView userImage;
    private TextView userName;
    private TextView userEmail;
    private TextView loginTv;
    private SearchView searchView;
    private DatabaseHelper db;

    // Progress dialog
    private ProgressDialog pDialog;

    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]

    private GoogleSignInClient mGoogleSignInClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        coordinatorLayout = findViewById(R.id.coordinator_layout);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        noJournalsView = findViewById(R.id.empty_journals_view);

        db = new DatabaseHelper(this);

        journalList.addAll(db.getAllJournals());

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(HomeActivity.this, NewJournalActivity.class);

                startActivity(i);

            }
        });

        mAdapter = new JournalsAdapter(this, journalList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(mAdapter);

        toggleEmptyNotes();


        /*
          On long press on RecyclerView item, open alert dialog
          with options to choose
           Delete
          */
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this,
                recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {

                Journal journal = journalList.get(position);

                Intent i = new Intent(HomeActivity.this, ReadActivity.class);

                // Create object of SharedPreferences.
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(HomeActivity.this);
                //now get Editor
                SharedPreferences.Editor editor = sharedPref.edit();
                //put your value
                editor.putString("dateStamp", journal.getDateStamp());
                editor.putString("timeStamp", journal.getTimeStamp());
                editor.putString("journalTitle", journal.getJournalTitle());
                editor.putString("journalText", journal.getJournalText());
                editor.putInt("position", position);
                //commits your edits
                editor.apply();

                startActivity(i);

            }

            @Override
            public void onLongClick(View view, int position) {
                showActionsDialog(position);
            }
        }));

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Navigation view header
        navHeader = navigationView.getHeaderView(0);
        userName = navHeader.findViewById(R.id.user_name);
        userEmail = navHeader.findViewById(R.id.user_email);
        loginTv = navHeader.findViewById(R.id.login_tv);
        userImage = navHeader.findViewById(R.id.user_image);

        loginTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);

        // [START config_signin]
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // [END config_signin]

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]
    }

    /**
     * Deleting journal from SQLite and removing the
     * item from the list by its position
     */
    private void deleteJournal(int position) {
        // deleting the journal from db
        db.deleteJournal(journalList.get(position));

        // removing the journal from the list
        journalList.remove(position);
        mAdapter.notifyItemRemoved(position);

        toggleEmptyNotes();
    }

    /**
     * Toggling list and empty journals view
     */
    private void toggleEmptyNotes() {
        // you can check journalList.size() > 0

        if (db.getJournalsCount() > 0) {
            noJournalsView.setVisibility(View.GONE);
        } else {
            noJournalsView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Opens dialog with Edit - Delete options
     * Delete - 0
     */
    private void showActionsDialog(final int position) {
        CharSequence colors[] = new CharSequence[]{"Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                deleteJournal(position);

            }
        });
        builder.show();
    }


    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }
    // [END on_start_check_user]


    // [START onactivityresult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // [START_EXCLUDE]
                updateUI(null);
                // [END_EXCLUDE]
            }
        }
    }
    // [END onactivityresult]


    // [START auth_with_google]
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]
        showpDialog();
        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Snackbar.make(coordinatorLayout, "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // [START_EXCLUDE]
                        hidepDialog();
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END auth_with_google]

    // [START signin]
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signin]

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                    }
                });
    }


    private void updateUI(FirebaseUser user) {
        hidepDialog();
        if (user != null) {

            // name, email
            userName.setText(user.getDisplayName());
            userEmail.setText(user.getEmail());

            // Loading profile image
            Glide.with(this).load(user.getPhotoUrl())
//                    .crossFade()
                    .thumbnail(0.5f)
                    .apply(RequestOptions.circleCropTransform())
//                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(userImage);

            loginTv.setVisibility(View.GONE);

        } else {

            // name, email
            userName.setText("");
            userEmail.setText("");

            // Loading ic_launcher
            Glide.with(this).load(R.drawable.journal_logo)
                    .thumbnail(0.5f)
                    .apply(RequestOptions.circleCropTransform())
                    .into(userImage);

            loginTv.setVisibility(View.VISIBLE);


        }
    }

    private void showpDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hidepDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.app_bar_search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                mAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                mAdapter.getFilter().filter(query);
                return false;
            }
        });
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.app_bar_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_new_journal) {

            Intent i = new Intent(HomeActivity.this, NewJournalActivity.class);

            startActivity(i);

        } else if (id == R.id.nav_sign_out) {

            signOut();

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}
