package de.androidcrypto.firebaseplayground;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import de.androidcrypto.firebaseplayground.models.Message2Model;
import de.androidcrypto.firebaseplayground.models.MessageModel;
import de.androidcrypto.firebaseplayground.models.UserModel;

public class GenerateTestMessagesDatabaseActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener {

    // implement from https://www.geeksforgeeks.org/endless-recyclerview-in-android/
    // (Kotlin) https://proandroiddev.com/firestore-pagination-with-realtime-updates-android-323d336a6772
    // (Kotlin) https://medium.com/firebase-developers/update-queries-without-changing-recyclerview-adapter-using-firebaseui-android-32098b3082b2
    // (Kotlin) https://github.com/PatilShreyas/FirebaseRecyclerUpdateQuery-Demo

    static final String TAG = "GenTextMessDatabase";

    TextView header;
    com.google.android.material.textfield.TextInputEditText edtMessage;
    com.google.android.material.textfield.TextInputLayout edtMessageLayout;
    RecyclerView messageRecyclerView;

    private static String authUserId = "", authUserEmail = "", authDisplayName = "";
    private static String receiveUserId = "", receiveUserEmail = "", receiveUserDisplayName = "";
    private static String roomId = "";

    private DatabaseReference mDatabaseReference;
    private DatabaseReference messagesDatabase;
    private DatabaseReference roomIdDatabase;
    private FirebaseAuth mFirebaseAuth;
    //FirebaseRecyclerAdapter firebaseRecyclerAdapter;

    private final List<MessageModel> messagesList=new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_test_messages_database);

        header = findViewById(R.id.tvGenerateTestMessagesDatabaseHeader);
        edtMessageLayout = findViewById(R.id.etGenerateTestMessagesDatabaseMessageLayout);
        edtMessage = findViewById(R.id.etGenerateTestMessagesDatabaseMessage);
        messageRecyclerView = findViewById(R.id.rvGenerateTestMessagesDatabase);

        // start with a disabled ui
        enableUiOnSignIn(false);

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        // set the persistance first but in MainActivity
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        loadSignedInUserData(mFirebaseAuth.getCurrentUser().getUid());

        // read data received from ListUserOnDatabase
        Intent intent = getIntent();
        receiveUserId = intent.getStringExtra("UID");
        if (receiveUserId != null) {
            Log.i(TAG, "selectedUid: " + receiveUserId);
        } else {
            receiveUserId = "";
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
        //receiveUser.setText(receiveUserString);
        Log.i(TAG, "receiveUser: " + receiveUserString);
        // get own data
        authUserEmail = intent.getStringExtra("AUTH_EMAIL");
        authDisplayName = intent.getStringExtra("AUTH_DISPLAYNAME");

        // Initialize Firebase Auth
        // mFirebaseAuth = FirebaseAuth.getInstance();
        // Initialize Firebase Database
        // https://fir-playground-1856e-default-rtdb.europe-west1.firebasedatabase.app/
        // if the database location is not us we need to use the reference:
        //mDatabase = FirebaseDatabase.getInstance("https://fir-playground-1856e-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        // the following can be used if the database server location is us
        //mDatabase = FirebaseDatabase.getInstance().getReference();
        // see loadSignedInUserData as we use a new instance there

        // Create a instance of the database and get its reference
        mDatabaseReference = FirebaseDatabase.getInstance("https://fir-playground-1856e-default-rtdb.europe-west1.firebasedatabase.app").getReference();
        //messagesDatabase = mDatabaseReference.child("messages");
        messagesDatabase = mDatabaseReference.child("messages2");
        messagesDatabase.keepSynced(true);

        edtMessageLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //showProgressBar();
                Log.i(TAG, "clickOnIconEnd");
                String messageString = edtMessage.getText().toString();
                Log.i(TAG, "message: " + messageString);
                // now we are going to send data to the database

                // retrieve the time string in GMT
                //SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                //String millisInString  = dateFormat.format(new Date());
                //for (int i = 0; i < 25; i++) {
                for (int i = 0; i < 3; i++) {

                    long actualTime = new Date().getTime();
                    // MessageModel(String senderId, String message, long messageTime, boolean messageEncrypted) {
                    Message2Model messageModel = new Message2Model( authUserId, messageString + " " + String.valueOf(i), actualTime, String.valueOf(actualTime), false);
                    messagesDatabase.child(roomId).push().setValue(messageModel);
                    System.out.println("i: " + i + " model: " + messageModel.toMap().toString());
                    messageModel = new Message2Model( "b", "Re: " + messageString + " " + String.valueOf(i), actualTime, String.valueOf(actualTime), false);
                    messagesDatabase.child(roomId).push().setValue(messageModel);
                }
                // without push there is no new chatId key
                // mDatabaseReference.child("messages").child(roomId).setValue(messageModel);
                Toast.makeText(getApplicationContext(),
                        "messages written to database: " + messageString,
                        Toast.LENGTH_SHORT).show();
                edtMessage.setText("");
            }
        });
    }

    /**
     * service methods
     */

    // compare two strings and build a new string: if a < b: ab if a > b: ba, if a = b: ab
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
        FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();
        if(currentUser != null){

            receiveUserId = "zzzzz"; // for test purposes
            if (!receiveUserId.equals("")) {
                Log.i(TAG, "onStart prepare database for chat");
                reload();
                enableUiOnSignIn(true);
                setDatabaseForRoomId(currentUser.getUid(), receiveUserId);

                //attachRecyclerViewAdapter();
            } else {
                header.setText("you need to select a receiveUser first");
                Log.i(TAG, "you need to select a receiveUser first");
            }
        } else {
            //signedInUser.setText("no user is signed in");
            authUserId = "";
            enableUiOnSignIn(false);

        }
        // startListening begins when a user is logged in
        //firebaseRecyclerAdapter.startListening();
        FirebaseAuth.getInstance().addAuthStateListener(this);
    }

    private void setDatabaseForRoomId(String ownUid, String receiveUserId) {
        Log.i(TAG, "setDatabaseForRoomId");
        roomId = getRoomId(ownUid, receiveUserId);
        Log.i(TAG, "room Id is " + roomId);
        roomIdDatabase = messagesDatabase.child(roomId);



    }


    @Override
    protected void onStop() {
        super.onStop();
        //if (firebaseRecyclerAdapter != null) {   }
        FirebaseAuth.getInstance().removeAuthStateListener(this);
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth auth) {
        if (isSignedIn()) {
            //getAuthUserCredentials();
        } else {
            Toast.makeText(this, "you need to sign in before chatting", Toast.LENGTH_SHORT).show();
            //auth.signInAnonymously().addOnCompleteListener(new SignInResultNotifier(this));
        }
    }

    private boolean isSignedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    private void loadSignedInUserData(String mAuthUserId) {
        if (!mAuthUserId.equals("")) {
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase.child("users").child(mAuthUserId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    //hideProgressBar();
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Error getting data", task.getException());
                    } else {
                        // check for a null value means no user data were saved before
                        UserModel userModel = task.getResult().getValue(UserModel.class);
                        Log.i(TAG, String.valueOf(userModel));
                        if (userModel == null) {
                            Log.i(TAG, "userModel is null, show message");
                        } else {
                            Log.i(TAG, "userModel email: " + userModel.getUserMail());
                            authUserId = mAuthUserId;
                            authUserEmail = userModel.getUserMail();
                            authDisplayName = userModel.getUserName();
                        }
                    }
                }
            });
        } else {
            Toast.makeText(getApplicationContext(),
                    "sign in a user before loading",
                    Toast.LENGTH_SHORT).show();
            //hideProgressBar();
        }
    }

    private void reload() {
        Objects.requireNonNull(mFirebaseAuth.getCurrentUser()).reload().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    updateUI(mFirebaseAuth.getCurrentUser());
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
        //hideProgressBar();
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
            //signedInUser.setText(userData);
            Log.i(TAG, "authUser: " + userData);
        } else {
            //signedInUser.setText(null);
            authUserId = "";
        }
    }

    private void enableUiOnSignIn(boolean userIsSignedIn) {
        if (!userIsSignedIn) {
            header.setText("you need to be signed in before starting a chat");
            edtMessageLayout.setEnabled(userIsSignedIn);
        } else {
            edtMessageLayout.setEnabled(userIsSignedIn);
        }
    }
}