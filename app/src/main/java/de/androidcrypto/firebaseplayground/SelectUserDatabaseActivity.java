package de.androidcrypto.firebaseplayground;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SelectUserDatabaseActivity extends AppCompatActivity {

    /**
     * This class is similar to ListUserDatabase but it checks for an incoming intent to
     * select where to return:
     * intentExtra "CALLER_ACTIVITY" == "SEND_MESSAGE_DATABASE" => return to SendMessageDatabaseActivity
     * intentExtra "CALLER_ACTIVITY" == "LIST_MESSAGE_DATABASE" => return to ListMessageDatabaseActivity
     */
    Intent returnIntent;

    com.google.android.material.textfield.TextInputEditText signedInUser;
    com.google.android.material.textfield.TextInputEditText userId, userEmail, userPhotoUrl, userPublicKey, userName;
    TextView warningNoData;

    static final String TAG = "SelectUserDatabase";
    // get the data from auth
    private static String authUserId = "", authUserEmail, authDisplayName, authPhotoUrl;
    private ListView userListView;
    private List<String> arrayList = new ArrayList<>();
    private List<String> uidList = new ArrayList<>();
    private List<String> emailList = new ArrayList<>();
    private List<String> displayNameList = new ArrayList<>();

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_user_database);

        signedInUser = findViewById(R.id.etDatabaseUserSignedInUser);
        progressBar = findViewById(R.id.pbDatabaseUser);

        userListView = findViewById(R.id.lvListUser);

        // don't show the keyboard on startUp
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        /**
         * see comment at class definition
         */
        Intent intent = getIntent();
        String callerActivity = intent.getStringExtra("CALLER_ACTIVITY");
        // the activity was called directly so it will return to MainActivity
        returnIntent = new Intent(SelectUserDatabaseActivity.this, MainActivity.class); // default
        if (TextUtils.isEmpty(callerActivity)) {
            Log.i(TAG, "The activity was called directly and will return to MainActivity");
        }
        if (callerActivity.equals("SEND_MESSAGE_DATABASE")) {
            Log.i(TAG, "The activity was called from SendMessageDatabase and will return to the caller activity");
            returnIntent = new Intent(SelectUserDatabaseActivity.this, SendMessageDatabaseActivity.class);
        }
        if (callerActivity.equals("LIST_MESSAGE_DATABASE")) {
            Log.i(TAG, "The activity was called from ListMessagesDatabase and will return to the caller activity");
            returnIntent = new Intent(SelectUserDatabaseActivity.this, ListMessagesDatabaseActivity.class);
        }
        if (callerActivity.equals("CHAT_MESSAGE_DATABASE")) {
            Log.i(TAG, "The activity was called from MainActivity and will return to  ChatMessagesDatabase activity");
            returnIntent = new Intent(SelectUserDatabaseActivity.this, ChatMessageDatabaseActivity.class);
        }

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // Initialize Firebase Database
        // https://fir-playground-1856e-default-rtdb.europe-west1.firebasedatabase.app/
        // if the database location is not us we need to use the reference:
        mDatabase = FirebaseDatabase.getInstance("https://fir-playground-1856e-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        // the following can be used if the database server location is us
        //mDatabase = FirebaseDatabase.getInstance().getReference();

        // run directly
        listUser();
        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String uidSelected = uidList.get(position);
                String emailSelected = emailList.get(position);
                String displayNameSelected = displayNameList.get(position);
                returnIntent.putExtra("UID", uidSelected);
                returnIntent.putExtra("EMAIL", emailSelected);
                returnIntent.putExtra("DISPLAYNAME", displayNameSelected);
                startActivity(returnIntent);
                finish();
                        /*
                        Intent intent = new Intent(SelectUserDatabaseActivity.this, SendMessageDatabaseActivity.class);
                        intent.putExtra("UID", uidSelected);
                        intent.putExtra("EMAIL", emailSelected);
                        intent.putExtra("DISPLAYNAME", displayNameSelected);
                        startActivity(intent);
                        finish();
                         */
            }
        });



        Button listUser = findViewById(R.id.btnListUserRun);
        listUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgressBar();
                DatabaseReference usersRef = mDatabase.child("users");
                List<String> arrayList = new ArrayList<>();
                List<String> uidList = new ArrayList<>();
                List<String> emailList = new ArrayList<>();
                List<String> displayNameList = new ArrayList<>();
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(SelectUserDatabaseActivity.this, android.R.layout.simple_list_item_1, arrayList);
                userListView.setAdapter(arrayAdapter);
                usersRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                        final String email = Objects.requireNonNull(dataSnapshot.child("userMail").getValue()).toString();
                        final String displayName;
                        if (dataSnapshot.child("userName").getValue() != null) {
                            displayName = dataSnapshot.child("userMail").getValue().toString();
                        } else {
                            displayName = "";
                        }
                        final String uid = dataSnapshot.getKey().toString();
                        arrayList.add(email + " " + displayName);
                        arrayAdapter.notifyDataSetChanged();
                        uidList.add(uid);
                        emailList.add(email);
                        displayNameList.add(displayName);
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                hideProgressBar();

                userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        String uidSelected = uidList.get(position);
                        String emailSelected = emailList.get(position);
                        String displayNameSelected = displayNameList.get(position);
                        returnIntent.putExtra("UID", uidSelected);
                        returnIntent.putExtra("EMAIL", emailSelected);
                        returnIntent.putExtra("DISPLAYNAME", displayNameSelected);
                        startActivity(returnIntent);
                        finish();
                        /*
                        Intent intent = new Intent(SelectUserDatabaseActivity.this, SendMessageDatabaseActivity.class);
                        intent.putExtra("UID", uidSelected);
                        intent.putExtra("EMAIL", emailSelected);
                        intent.putExtra("DISPLAYNAME", displayNameSelected);
                        startActivity(intent);
                        finish();
                         */
                    }
                });
            }
        });

        Button backToMain = findViewById(R.id.btnDatabaseUserToMain);

        backToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SelectUserDatabaseActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void listUser() {
        showProgressBar();
        DatabaseReference usersRef = mDatabase.child("users");
        arrayList = new ArrayList<>();
        uidList = new ArrayList<>();
        emailList = new ArrayList<>();
        displayNameList = new ArrayList<>();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(SelectUserDatabaseActivity.this, android.R.layout.simple_list_item_1, arrayList);
        userListView.setAdapter(arrayAdapter);
        usersRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                final String email = Objects.requireNonNull(dataSnapshot.child("userMail").getValue()).toString();
                final String displayName;
                if (dataSnapshot.child("userName").getValue() != null) {
                    displayName = dataSnapshot.child("userMail").getValue().toString();
                } else {
                    displayName = "";
                }
                final String uid = dataSnapshot.getKey().toString();
                arrayList.add(email + " " + displayName);
                arrayAdapter.notifyDataSetChanged();
                uidList.add(uid);
                emailList.add(email);
                displayNameList.add(displayName);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        hideProgressBar();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            reload();
        } else {
            signedInUser.setText("no user is signed in");
        }
    }

    private void reload() {
        Objects.requireNonNull(mAuth.getCurrentUser()).reload().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    updateUI(mAuth.getCurrentUser());
                    /*
                    Toast.makeText(getApplicationContext(),
                            "Reload successful!",
                            Toast.LENGTH_SHORT).show();
                     */
                } else {
                    Log.e(TAG, "reload", task.getException());
                    Toast.makeText(getApplicationContext(),
                            "Failed to reload user.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateUI(FirebaseUser user) {
        hideProgressBar();
        if (user != null) {
            authUserId = user.getUid();
            authUserEmail = user.getEmail();
            String userData = String.format("Email: %s", authUserEmail);
            signedInUser.setText(userData);
        } else {
            signedInUser.setText(null);
        }
    }

    public void showProgressBar() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    public void hideProgressBar() {
        if (progressBar != null) {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    public String usernameFromEmail(String email) {
        if (email.contains("@")) {
            return email.split("@")[0];
        } else {
            return email;
        }
    }
}