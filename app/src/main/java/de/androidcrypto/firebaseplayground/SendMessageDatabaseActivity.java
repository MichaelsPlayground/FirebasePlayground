package de.androidcrypto.firebaseplayground;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;
import java.util.Objects;

import de.androidcrypto.firebaseplayground.models.MessageModel;

public class SendMessageDatabaseActivity extends AppCompatActivity {

    com.google.android.material.textfield.TextInputEditText signedInUser, receiveUser;
    com.google.android.material.textfield.TextInputEditText edtMessage, edtRoomId, userPhotoUrl, userPublicKey, userName;
    com.google.android.material.textfield.TextInputLayout edtMessageLayout;
    TextView warningNoData;

    static final String TAG = "SendMessageRtDatabase";
    // get the data from auth
    private static String authUserId = "", authUserEmail, authDisplayName, authPhotoUrl;
    private static String receiveUserId = "", receiveUserEmail = "", receiveUserDisplayName = "";

    private DatabaseReference mDatabaseReference;
    private FirebaseAuth mAuth;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message_database);

        signedInUser = findViewById(R.id.etSendMessageDatabaseSignedInUser);
        receiveUser = findViewById(R.id.etSendMessageDatabaseReceiveUser);
        progressBar = findViewById(R.id.pbSendMessageDatabase);

        edtMessageLayout = findViewById(R.id.etSendMessageDatabaseMessageLayout);
        edtMessage = findViewById(R.id.etSendMessageDatabaseMessage);
        edtRoomId = findViewById(R.id.etSendMessageDatabaseRoomId);
/*
        warningNoData = findViewById(R.id.tvDatabaseUserNoData);
        userId = findViewById(R.id.etDatabaseUserUserId);
        userEmail = findViewById(R.id.etDatabaseUserUserEmail);
        userPhotoUrl = findViewById(R.id.etDatabaseUserPhotoUrl);
        userPublicKey = findViewById(R.id.etDatabaseUserPublicKey);
        userName = findViewById(R.id.etDatabaseUserUserName);
*/
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
        receiveUser.setText(receiveUserString);
        Log.i(TAG, "receiveUser: " + receiveUserString);

        //       Button loadData = findViewById(R.id.btnDatabaseUserLoad);
        //       Button savaData = findViewById(R.id.btnDatabaseUserSave);

        Button selectRecipient = findViewById(R.id.btnSendMessageDatabaseSelectRecipient);
        Button backToMain = findViewById(R.id.btnSendMessageDatabaseToMain);

        selectRecipient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "select receipient");
                //Intent intent = new Intent(SendMessageDatabaseActivity.this, ListUserDatabaseActivity.class);
                Intent intent = new Intent(SendMessageDatabaseActivity.this, SelectUserDatabaseActivity.class);
                intent.putExtra("CALLER_ACTIVITY", "SEND_MESSAGE_DATABASE");
                startActivity(intent);
                finish();
            }
        });

        backToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SendMessageDatabaseActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

/*
static data:
authUser:
Email: michael.telefon08@gmail.com
UID: VgNGhMth85Y0Szg6FxLMcWkEpmA3
Display Name: Michael Fehr

receiveUser:
Email: klaus.zwang.1934@gmail.com
UID: 0QCS5u2UnxYURlbntvVTA6ZTbaO2
Display Name: klaus.zwang.1934@gmail.com
 */

        edtMessageLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "send message");
                // check for selected receipient
                if (TextUtils.isEmpty(receiveUserId)) {
                    Log.i(TAG, "no receipient selected, abort");
                    Toast.makeText(getApplicationContext(),
                            "please select a receipient first",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                showProgressBar();

                /*
                // todo get the real uids, remove these lines
                if (authUserId.equals("VgNGhMth85Y0Szg6FxLMcWkEpmA3")) {
                    receiveUserId = "0QCS5u2UnxYURlbntvVTA6ZTbaO2";
                } else {
                    authUserId = "0QCS5u2UnxYURlbntvVTA6ZTbaO2";
                    receiveUserId = "VgNGhMth85Y0Szg6FxLMcWkEpmA3";
                }
                */
                // get the roomId by comparing 2 UID strings
                String roomId = getRoomId(authUserId, receiveUserId);
                String messageString = edtMessage.getText().toString();
                edtRoomId.setText(roomId);
                Log.i(TAG, "message: " + messageString + " send to roomId: " + roomId);
                // now we are going to send data to the database
                long actualTime = new Date().getTime();
                /*
                retrieve the time string in GMT
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String millisInString  = dateFormat.format(new Date());
                 */
                MessageModel messageModel = new MessageModel(authUserId, messageString, actualTime, false);
                mDatabaseReference.child("messages").child(roomId).push().setValue(messageModel);
                // without push there is no new chatId key
                // mDatabaseReference.child("messages").child(roomId).setValue(messageModel);
                Toast.makeText(getApplicationContext(),
                        "message written to database",
                        Toast.LENGTH_SHORT).show();
                edtMessage.setText("");
                hideProgressBar();
            }
        });

        /*
        Button send = findViewById(R.id.btnSendMessageDatabaseSend);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "old send method");
                // todo get the real uids, remove the line
                receiveUserId = "0QCS5u2UnxYURlbntvVTA6ZTbaO2";
                // get the roomId by comparing 2 UID strings
                String roomId = getRoomId(authUserId, receiveUserId);
                String messageString = edtMessage.getText().toString();
                edtRoomId.setText(roomId);
                Log.i(TAG, "message: " + messageString);
                Log.i(TAG, "roomId: " + roomId);
                // now we are going to send data to the database
                long actualTime = new Date().getTime();
                MessageModel messageModel = new MessageModel(authUserId, messageString, actualTime, false);
                mDatabaseReference.child("messages").child(roomId).push().setValue(messageModel);
                // without push there is no new chatId key
                // mDatabaseReference.child("messages").child(roomId).setValue(messageModel);
                Toast.makeText(getApplicationContext(),
                        "message written to database",
                        Toast.LENGTH_SHORT).show();
            }
        });
        */

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

    /**
     * basic
     */

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
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