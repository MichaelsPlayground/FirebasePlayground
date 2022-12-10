package de.androidcrypto.firebaseplayground;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.Objects;

import de.androidcrypto.firebaseplayground.models.MessageModel;
import de.androidcrypto.firebaseplayground.models.UserFirestoreModel;

public class SendMessageFirestoreActivity extends AppCompatActivity {

    com.google.android.material.textfield.TextInputEditText signedInUser, receiveUser;
    com.google.android.material.textfield.TextInputEditText edtMessage, edtRoomId, userPhotoUrl, userPublicKey, userName;
    com.google.android.material.textfield.TextInputLayout edtMessageLayout;
    TextView warningNoData;

    static final String TAG = "SendMessageFirestore";
    // get the data from auth
    private static String authUserId = "", authUserEmail, authDisplayName, authPhotoUrl;
    private static String receiveUserId = "", receiveUserEmail = "", receiveUserDisplayName = "";

    //private DatabaseReference mDatabaseReference;
    private FirebaseAuth mAuth;
    FirebaseFirestore firestoreDatabase = FirebaseFirestore.getInstance();
    private static final String CHILD_MESSAGES = "messages";
    private static final String CHILD_MESSAGES_SUB = "mess";
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message_firestore);

        signedInUser = findViewById(R.id.etSendMessageFirestoreSignedInUser);
        receiveUser = findViewById(R.id.etSendMessageFirestoreReceiveUser);
        progressBar = findViewById(R.id.pbSendMessageFirestore);

        edtMessageLayout = findViewById(R.id.etSendMessageFirestoreMessageLayout);
        edtMessage = findViewById(R.id.etSendMessageFirestoreMessage);
        edtRoomId = findViewById(R.id.etSendMessageFirestoreRoomId);

        // don't show the keyboard on startUp
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

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

        Button selectRecipient = findViewById(R.id.btnSendMessageFirestoreSelectRecipient);
        Button backToMain = findViewById(R.id.btnSendMessageFirestoreToMain);

        selectRecipient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "select receipient");
                //Intent intent = new Intent(SendMessageDatabaseActivity.this, ListUserDatabaseActivity.class);
                Intent intent = new Intent(SendMessageFirestoreActivity.this, SelectUserFirestoreActivity.class);
                intent.putExtra("CALLER_ACTIVITY", "SEND_MESSAGE_FIRESTORE");
                startActivity(intent);
                finish();
            }
        });

        backToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SendMessageFirestoreActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

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

                // get the roomId by comparing 2 UID strings
                String roomId = getRoomId(authUserId, receiveUserId);
                String messageString = edtMessage.getText().toString();
                edtRoomId.setText(roomId);
                Log.i(TAG, "message: " + messageString + " send to roomId: " + roomId);
                // now we are going to send data to the database
                long actualTime = new Date().getTime();

                //retrieve the time string in GMT
                //SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                //String millisInString  = dateFormat.format(new Date());

                MessageModel messageModel = new MessageModel(authUserId, messageString, actualTime, false);

                // the message is nested in a structure like
                // messages - roomId - "messages" - random id - single message
                firestoreDatabase.collection(CHILD_MESSAGES)
                        .document(roomId)
                        .collection(CHILD_MESSAGES_SUB).add(messageModel)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.i(TAG, "DocumentSnapshot successfully written for roomId: " + roomId);
                                Toast.makeText(getApplicationContext(),
                                        "message written to database",
                                        Toast.LENGTH_SHORT).show();
                                hideProgressBar();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.i(TAG, "Error writing document for roomId: " + roomId, e);
                                Toast.makeText(getApplicationContext(),
                                        "ERROR on writing message to database",
                                        Toast.LENGTH_SHORT).show();
                                hideProgressBar();
                            }
                        });

/*
                // the message is nested in a structure like
                // messages - roomId - "messages" - timestamp - single message
                DocumentReference messageRef = firestoreDatabase
                        .collection(CHILD_MESSAGES).document(roomId)
                        .collection(CHILD_MESSAGES).document(String.valueOf(actualTime));
                messageRef.set(messageModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.i(TAG, "DocumentSnapshot successfully written for roomId: " + roomId);
                                Toast.makeText(getApplicationContext(),
                                        "message written to database",
                                        Toast.LENGTH_SHORT).show();
                                hideProgressBar();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.i(TAG, "Error writing document for roomId: " + roomId, e);
                                Toast.makeText(getApplicationContext(),
                                        "ERROR on writing message to database",
                                        Toast.LENGTH_SHORT).show();
                                hideProgressBar();
                            }
                        });
*/
                edtMessage.setText("");
                hideProgressBar();
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