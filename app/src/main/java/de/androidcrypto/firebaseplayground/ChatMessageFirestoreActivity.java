package de.androidcrypto.firebaseplayground;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import de.androidcrypto.firebaseplayground.models.MessageModel;

public class ChatMessageFirestoreActivity extends AppCompatActivity {

    static final String TAG = "ChatMessageFirestore";

    TextView chatHeader;
    com.google.android.material.textfield.TextInputEditText edtMessage;
    com.google.android.material.textfield.TextInputLayout edtMessageLayout;
    RecyclerView chatList;
    private ArrayList<MessageModel> chatArrayList;
    private ChatFirestoreRvAdapter chatRvAdapter;
    LinearLayoutManager linearLayoutManager;

    private static String authUserId = "", authUserEmail, authDisplayName, authPhotoUrl;
    private static String receiveUserId = "", receiveUserEmail = "", receiveUserDisplayName = "";
    private static String roomId = "";

    private FirebaseAuth firebaseAuth;
    FirebaseFirestore firestoreDatabase = FirebaseFirestore.getInstance();
    private static final String CHILD_MESSAGES = "messages";
    private static final String CHILD_MESSAGES_SUB = "mess";

    // creating variables for our recycler view,
    // array list, adapter, firebase firestore
    // and our progress bar.
    //private RecyclerView courseRV;


    ProgressBar loadingPB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_message_firestore);

        chatHeader = findViewById(R.id.tvChatFirestoreHeader);
        edtMessageLayout = findViewById(R.id.etChatFirestoreMessageLayout);
        edtMessage = findViewById(R.id.etChatFirestoreMessage);
        chatList = findViewById(R.id.rvChatFirestore);

        chatList.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setReverseLayout(false);
        linearLayoutManager.setStackFromEnd(true);
        chatList.setLayoutManager(linearLayoutManager);

        // creating our new array list
        chatArrayList = new ArrayList<>();

        // don't show the keyboard on startUp
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        authUserId = firebaseAuth.getCurrentUser().getUid();

        // todo disable message send functionality when no user is selected

        // get the receiveUser from SelectUserFirestoreActivity
        Intent intent = getIntent();
        receiveUserId = intent.getStringExtra("UID");
        if (receiveUserId != null) {
            roomId = getRoomId(authUserId, receiveUserId);
            Log.i(TAG, "selectedUid: " + receiveUserId);
            Log.i(TAG, "we chat in roomId: " + roomId);
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
        chatHeader.setText("Chat with " + receiveUserDisplayName);
        Log.i(TAG, "receiveUser: " + receiveUserString);

        // adding our array list to our recycler view adapter class.
        chatRvAdapter = new ChatFirestoreRvAdapter(chatArrayList, this);

        // setting adapter to our recycler view.
        chatList.setAdapter(chatRvAdapter);

        //linearLayoutManager.smoothScrollToPosition(chatList, null, chatRvAdapter.getItemCount() - 1);

        // below line is use to get the data from Firebase Firestore.
        // previously we were saving data on a reference of Courses
        // now we will be getting the data from the same reference.
        // the message is nested in a structure like
        // messages - roomId - "messages" - random id - single message

        /*
        // unsorted list
        firestoreDatabase.collection(CHILD_MESSAGES)
                .document(roomId)
                .collection(CHILD_MESSAGES_SUB).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
         */


        CollectionReference collectionReference = firestoreDatabase
                .collection(CHILD_MESSAGES)
                .document(roomId)
                .collection(CHILD_MESSAGES_SUB);
        // sort list by message time
        Query query = collectionReference.orderBy("messageTime", Query.Direction.ASCENDING);

/*
        // this is a onetime snapshot - no updates
        query
        //collectionReference
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        // after getting the data we are calling on success method
                        // and inside this method we are checking if the received
                        // query snapshot is empty or not.
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // if the snapshot is not empty we are
                            // hiding our progress bar and adding
                            // our data in a list.
                            //loadingPB.setVisibility(View.GONE);
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot documentSnapshot : list) {
                                // after getting this list we are passing
                                // that list to our object class.
                                MessageModel chat = documentSnapshot.toObject(MessageModel.class);

                                // and we will pass this object class
                                // inside our arraylist which we have
                                // created for recycler view.
                                chatArrayList.add(chat);
                            }
                            // after adding the data to recycler view.
                            // we are calling recycler view notifuDataSetChanged
                            // method to notify that data has been changed in recycler view.
                            chatRvAdapter.notifyDataSetChanged();
                        } else {
                            // if the snapshot is empty we are displaying a toast message.
                            Toast.makeText(ChatMessageFirestoreActivity.this, "No data found in Database", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // if we do not get any data or any error we are displaying
                        // a toast message that we do not get any data
                        Toast.makeText(ChatMessageFirestoreActivity.this, "Fail to get the data.", Toast.LENGTH_SHORT).show();
                    }
                });
*/

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }
                if (snapshot != null) {
                    //Log.d(TAG, "Current data: " + snapshot.getQuery()..getData());
                    if (!snapshot.isEmpty()) {
                        // if the snapshot is not empty we are
                        // hiding our progress bar and adding
                        // our data in a list.
                        //loadingPB.setVisibility(View.GONE);
                        List<DocumentSnapshot> list = snapshot.getDocuments();
                        for (DocumentSnapshot documentSnapshot : list) {
                            // after getting this list we are passing
                            // that list to our object class.
                            MessageModel chat = documentSnapshot.toObject(MessageModel.class);
                            // and we will pass this object class
                            // inside our arraylist which we have
                            // created for recycler view.
                            chatArrayList.add(chat);
                        }
                        // after adding the data to recycler view.
                        // we are calling recycler view notifuDataSetChanged
                        // method to notify that data has been changed in recycler view.
                        chatRvAdapter.notifyDataSetChanged();
                        linearLayoutManager.scrollToPosition(chatRvAdapter.getItemCount() - 1);
                    } else {
                        // if the snapshot is empty we are displaying a toast message.
                        Toast.makeText(ChatMessageFirestoreActivity.this, "No data found in Database", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });


/*
        Query query = FirebaseFirestore.getInstance()
                .collection(CHILD_MESSAGES).orderBy("timestamp", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<MessageModel> options = new FirestoreRecyclerOptions.Builder<Chat>().setQuery(query, Chat.class).build();
        chatadapter = new ChatAdapter(options);
        chatadapter.registerAdapterDataObserver(    new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                recyclerView.scrollToPosition(chatadapter.getItemCount() - 1);
            }
        });
        recyclerView.setAdapter(chatadapter);

 */
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
}