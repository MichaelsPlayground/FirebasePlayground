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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ListMessagesDatabaseActivity extends AppCompatActivity {

    com.google.android.material.textfield.TextInputEditText signedInUser, receiveUser;
    com.google.android.material.textfield.TextInputEditText userId, userEmail, userPhotoUrl, userPublicKey, userName;
    TextView warningNoData;

    static final String TAG = "ListMessagesDatabase";
    // get the data from auth
    private static String authUserId = "", authUserEmail, authDisplayName, authPhotoUrl;
    private static String receiveUserId = "", receiveUserEmail = "", receiveUserDisplayName = "";

    ListView userListView;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_messages_database);

        signedInUser = findViewById(R.id.etListMessagesDatabaseSignedInUser);
        receiveUser = findViewById(R.id.etListMessagesReceiveUser);
        progressBar = findViewById(R.id.pbListMessagesDatabase);

        userListView = findViewById(R.id.lvListMessagesDatabase);

        // don't show the keyboard on startUp
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // Initialize Firebase Database
        // https://fir-playground-1856e-default-rtdb.europe-west1.firebasedatabase.app/
        // if the database location is not us we need to use the reference:
        mDatabase = FirebaseDatabase.getInstance("https://fir-playground-1856e-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        // the following can be used if the database server location is us
        //mDatabase = FirebaseDatabase.getInstance().getReference();

        Intent intent = getIntent();
        receiveUserId = intent.getStringExtra("UID");
        if (receiveUserId != null) {
            Log.i(TAG, "selectedUid: " + receiveUserId);
        }
        receiveUserEmail = intent.getStringExtra("EMAIL");
        if (receiveUserEmail != null) {
            Log.i(TAG, "selectedEmail: " + receiveUserEmail);
        }
        receiveUserDisplayName = intent.getStringExtra("DISPLAYNAME");
        if (receiveUserDisplayName != null) {
            Log.i(TAG, "selectedDisplayName: " + receiveUserDisplayName);
        }
        String receiveUserString = "Email: " + receiveUserEmail;
        receiveUserString += "\nUID: " + receiveUserId;
        receiveUserString += "\nDisplay Name: " + receiveUserDisplayName;
        receiveUser.setText(receiveUserString);
        Log.i(TAG, "receiveUser: " + receiveUserString);

        Button listMessages = findViewById(R.id.btnListMessagesDatabaseRun);
        listMessages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "list messages start");
                if (TextUtils.isEmpty(receiveUserId)) {
                    Log.i(TAG, "no receipient selected, abort");
                    Toast.makeText(getApplicationContext(),
                            "please select a receipient first",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                String roomId = getRoomId(authUserId, receiveUserId);
                Log.i(TAG, "listing messages in roomId: " + roomId);

                DatabaseReference messagesRef = mDatabase.child("messages").child(roomId);
                System.out.println("messagesRef.getKey: " + messagesRef.getKey());

                List<String> arrayList = new ArrayList<>();
                List<String> uidList = new ArrayList<>();
                List<String> emailList = new ArrayList<>();
                List<String> displayNameList = new ArrayList<>();
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(ListMessagesDatabaseActivity.this, android.R.layout.simple_list_item_1, arrayList);
                userListView.setAdapter(arrayAdapter);
                messagesRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                        Log.i(TAG, "onChildAdded:" + dataSnapshot.getKey());
                        System.out.println("ds existsf: " + dataSnapshot.exists());
                        final String message = Objects.requireNonNull(dataSnapshot.child("message").getValue()).toString();
                        final boolean messageEncrypted = (boolean) Objects.requireNonNull(dataSnapshot.child("messageEncrypted").getValue());
                        final long messageTime = (long) Objects.requireNonNull(dataSnapshot.child("messageTime").getValue());
                        final String senderId = Objects.requireNonNull(dataSnapshot.child("senderId").getValue()).toString();

                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String timeString  = dateFormat.format(messageTime);
                        String dataList = message + " from " + senderId + " at " + timeString + " is enc: " + messageEncrypted;
                        arrayList.add(dataList);
                        arrayAdapter.notifyDataSetChanged();

                        hideProgressBar();
                        /*
                        uidList.add(uid);
                        emailList.add(email);
                        displayNameList.add(displayName);
                         */
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
                        Log.i(TAG, "onCancelled error: " + error.toString());
                    }

                    @Override
                    protected void finalize() throws Throwable {
                        hideProgressBar();
                        super.finalize();
                    }

                });

                userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        Toast.makeText(getApplicationContext(),
                                "click on position " + position,
                                Toast.LENGTH_SHORT).show();
                        /*
                        String uidSelected = uidList.get(position);
                        String emailSelected = emailList.get(position);
                        String displayNameSelected = displayNameList.get(position);
                        Intent intent = new Intent(ListMessagesActivity.this, SendMessageActivity.class);
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

        Button selectRecipient = findViewById(R.id.btnListMessagesDatabaseSelectRecipient);
        selectRecipient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "select receipient");
                //Intent intent = new Intent(SendMessageDatabaseActivity.this, ListUserDatabaseActivity.class);
                Intent intent = new Intent(ListMessagesDatabaseActivity.this, SelectUserDatabaseActivity.class);
                intent.putExtra("CALLER_ACTIVITY", "LIST_MESSAGE_DATABASE");
                startActivity(intent);
                finish();
            }
        });

        Button backToMain = findViewById(R.id.btnListMessagesDatabaseToMain);
        backToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ListMessagesDatabaseActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * service methods
     */

    // compare two strings and build a new string: if a < b: ab if a > b: b_a, if a = b: a_b
    private String getRoomId(String a, String b) {
        int compare = a.compareTo(b);
        if (compare > 0) return b + "_" + a;
        else return a + "_" + b;
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