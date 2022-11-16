package de.androidcrypto.firebaseplayground;

import android.content.Intent;
import android.os.Bundle;
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

public class ListUserNotificationActivity extends AppCompatActivity {

    com.google.android.material.textfield.TextInputEditText signedInUser;
    com.google.android.material.textfield.TextInputEditText userId, userEmail, userPhotoUrl, userPublicKey, userName;
    TextView warningNoData;

    static final String TAG = "ListUserNotification";
    // get the data from auth
    private static String authUserId = "", authUserEmail, authDisplayName, authPhotoUrl;
    ListView userListView;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_user_notification);

        signedInUser = findViewById(R.id.etDatabaseUserSignedInUser);
        progressBar = findViewById(R.id.pbDatabaseUser);

        userListView = findViewById(R.id.lvListUser);

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
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(ListUserNotificationActivity.this, android.R.layout.simple_list_item_1, arrayList);
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
                        // here we are trying to get the device token for the receipient

                        mDatabase.child("userstoken").child(uidSelected).child("device_token").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                if (!task.isSuccessful()) {
                                    Log.e(TAG, "Error getting data for userstoken", task.getException());
                                }
                                else {
                                    String deviceToken = String.valueOf(task.getResult().getValue());
                                    if (deviceToken.equals("null")) deviceToken = "";
                                    Log.i(TAG, "Value:" + String.valueOf(task.getResult().getValue()));
                                    if (deviceToken == null) {
                                        Intent intent = new Intent(ListUserNotificationActivity.this, SendNotificationActivity.class);
                                        intent.putExtra("UID", uidSelected);
                                        intent.putExtra("EMAIL", emailSelected);
                                        intent.putExtra("DISPLAYNAME", displayNameSelected);
                                        intent.putExtra("DEVICETOKEN", "");
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Intent intent = new Intent(ListUserNotificationActivity.this, SendNotificationActivity.class);
                                        intent.putExtra("UID", uidSelected);
                                        intent.putExtra("EMAIL", emailSelected);
                                        intent.putExtra("DISPLAYNAME", displayNameSelected);
                                        intent.putExtra("DEVICETOKEN", deviceToken);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            }
                        });
                    }
                });
            }
        });

        Button backToMain = findViewById(R.id.btnDatabaseUserToMain);

        backToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ListUserNotificationActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
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