package de.androidcrypto.firebaseplayground;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

public class SendNotificationActivity extends AppCompatActivity {

    com.google.android.material.textfield.TextInputEditText signedInUser, deviceToken;
    com.google.android.material.textfield.TextInputEditText receipientDeviceToken;
    com.google.android.material.textfield.TextInputEditText edtNotification;
    com.google.android.material.textfield.TextInputLayout edtNotificationLayout;

    static final String TAG = "SendNotification";
    // get the data from auth
    private static String authUserId = "", authUserEmail, authDisplayName, authPhotoUrl;
    private static String receiveUserId = "", receiveUserEmail = "", receiveUserDisplayName = "", receiveUserDeviceToken;

    private DatabaseReference mDatabaseReference;
    private FirebaseAuth mAuth;
    ProgressBar progressBar;

    private static String NOTIFICATION_TITLE = "Firebase Playground";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_notification);

        signedInUser = findViewById(R.id.etSendNotificationSignedInUser);
        deviceToken = findViewById(R.id.etSendNotificationOwnDeviceToken);
        receipientDeviceToken = findViewById(R.id.etSendNotificationReceipientDeviceToken);
        progressBar = findViewById(R.id.pbSendNotification);

        edtNotificationLayout = findViewById(R.id.etSendNotificationNotificationLayout);
        edtNotification = findViewById(R.id.etSendNotificationNotification);

        // don't show the keyboard on startUp
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // Initialize Firebase Database
        // https://fir-playground-1856e-default-rtdb.europe-west1.firebasedatabase.app/
        // if the database location is not us we need to use the reference:
        mDatabaseReference = FirebaseDatabase.getInstance("https://fir-playground-1856e-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        // the following can be used if the database server location is us
        //mDatabaseReference = FirebaseDatabase.getInstance().getReference();

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

        receiveUserDeviceToken = intent.getStringExtra("DEVICETOKEN");
        if (receiveUserDeviceToken != null) {
            Log.i(TAG, "selectedDeviceToken: " + receiveUserDeviceToken);
        } else {
            receiveUserDeviceToken = "";
        }
        receipientDeviceToken.setText(receiveUserDeviceToken);
        Log.i(TAG, "receiveUser: " + receiveUserString);

        Button backToMain = findViewById(R.id.btnSendNotificationToMain);
        backToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SendNotificationActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Button getDeviceToken = findViewById(R.id.btnSendNotificationGetOwnDeviceToken);
        getDeviceToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseMessaging.getInstance().getToken()
                        .addOnCompleteListener(new OnCompleteListener<String>() {
                            @Override
                            public void onComplete(@NonNull Task<String> task) {
                                if (!task.isSuccessful()) {
                                    Log.i(TAG, "Fetching FCM registration token failed", task.getException());
                                    return;
                                }

                                // Get new FCM registration token
                                String deviceTokenString = task.getResult();
                                String currentUserID = mAuth.getCurrentUser().getUid();
                                mDatabaseReference.child("userstoken").child(currentUserID).child("device_token").setValue(deviceTokenString)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Log.i(TAG, "device token retrieved and saved in database");

                                            }
                                        });

                                // Log and toast
                                String msg = "token: " + deviceTokenString;
                                Log.i(TAG, msg);
                                deviceToken.setText(deviceTokenString);
                            }
                        });
            }
        });

        Button selectReceipient = findViewById(R.id.btnSendNotificationSelectReceipient);
        selectReceipient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SendNotificationActivity.this, ListUserNotificationActivity.class);
                startActivity(intent);
                finish();
            }
        });

        edtNotificationLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "send notification");

                // check for receipientDeviceToken
                if (receiveUserDeviceToken.equals("")) {
                    Toast.makeText(getApplicationContext(),
                            "select a user with a device token",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                String notificationMessage = edtNotification.getText().toString();
                FcmNotificationsSender fcmNotificationsSender = new FcmNotificationsSender(receiveUserDeviceToken, NOTIFICATION_TITLE, notificationMessage);
                fcmNotificationsSender.sendNotifications();
            }
        });
    }

    /**
     * basic
     */

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
            if (user.getDisplayName() != null) {
                authDisplayName = Objects.requireNonNull(user.getDisplayName()).toString();
            } else {
                authDisplayName = "no display name available";
            }
            String userData = String.format("Email: %s", authUserEmail);
            userData += "\nUID: " + authUserId;
            userData += "\nDisplay Name: " + authDisplayName;
            signedInUser.setText(userData);
            Log.i(TAG, "authUser: " + userData);
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

}