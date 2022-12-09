package de.androidcrypto.firebaseplayground;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.androidcrypto.firebaseplayground.models.UserFirestoreModel;

public class ListUserFirestoreRecyclerViewActivity extends AppCompatActivity {

    // improvement: https://www.freecodecamp.org/news/how-to-implement-swipe-for-options-in-recyclerview/
    

    com.google.android.material.textfield.TextInputEditText signedInUser;
    SwitchMaterial listOnlineUserOnly;
    //com.google.android.material.textfield.TextInputEditText userId, userEmail, userPhotoUrl, userPublicKey, userName;
    TextView warningNoData;

    static final String TAG = "ListUserFirestoreRv";
    // get the data from auth
    private static String authUserId = "", authUserEmail, authDisplayName, authPhotoUrl;
    //ListView userListView;
    // Recycler View object
    RecyclerView userRecyclerView;

    // Array list for recycler view data source
    private static List<String> arrayList = new ArrayList<>();
    //public static List<String> levelList = new ArrayList<>();
    // Layout Manager
    RecyclerView.LayoutManager RecyclerViewLayoutManager;
    // adapter class object
    ListUserFirestoreRecyclerViewAdapter adapter;
    // Linear Layout Manager
    LinearLayoutManager HorizontalLayout;

    //FirebaseFirestore firebaseFirestore;

    private FirebaseAuth mAuth;
    FirebaseFirestore firestoreDatabase = FirebaseFirestore.getInstance();
    private static final String CHILD_USERS = "users";
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_user_firestore_recyclerview);

        signedInUser = findViewById(R.id.etFirestoreDatabaseRvUserSignedInUser);
        progressBar = findViewById(R.id.pbFirestoreDatabaseRvUser);
        listOnlineUserOnly = findViewById(R.id.swFirestoreDatabaseRvUserListOnlineOnly);

        //userListView = findViewById(R.id.lvListUserFirestore);
        userRecyclerView = findViewById(R.id.rvListUserFirestore);

        RecyclerViewLayoutManager = new LinearLayoutManager(getApplicationContext());
        // Set LayoutManager on Recycler View
        userRecyclerView.setLayoutManager(RecyclerViewLayoutManager);
        // calling constructor of adapter
        // with source list as a parameter
        adapter = new ListUserFirestoreRecyclerViewAdapter(arrayList);

        // Set Horizontal Layout Manager
        // for Recycler view
        LinearLayoutManager verticalLayout = new LinearLayoutManager(ListUserFirestoreRecyclerViewActivity.this, LinearLayoutManager.VERTICAL, false);
        userRecyclerView.setLayoutManager(verticalLayout);

        // Set adapter on recycler view
        userRecyclerView.setAdapter(adapter);

        // don't show the keyboard on startUp
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        Button listUser = findViewById(R.id.btnListUserFirestoreRvRun);
        listUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "list user on firestore database run");
                showProgressBar();

                //List<String> arrayList = new ArrayList<>();
                List<String> uidList = new ArrayList<>();
                List<String> emailList = new ArrayList<>();
                List<String> displayNameList = new ArrayList<>();

                if (listOnlineUserOnly.isChecked()) {
                    // list online user only
                    CollectionReference onlineUserReference = firestoreDatabase.collection(CHILD_USERS);
                    Query query = onlineUserReference.whereEqualTo("userOnline", true);
                    query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            Log.i(TAG, "listUser on firestore complete");
                            if (task.isSuccessful()) {
                                Log.i(TAG, "listUser on firestore complete and successful");
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    UserFirestoreModel userFirestoreModel = document.toObject(UserFirestoreModel.class);
                                    final String displayName;
                                    if (TextUtils.isEmpty(userFirestoreModel.getUserName())) {
                                        displayName = "";
                                    } else {
                                        displayName = userFirestoreModel.getUserName();
                                    }
                                    arrayList.add(userFirestoreModel.getUserMail() + " " + displayName);
                                    uidList.add(document.getId());
                                    emailList.add(userFirestoreModel.getUserMail());
                                    displayNameList.add(displayName);
                                }
                                //ListView usersListView = (ListView) findViewById(R.id.lvListUserFirestore);
                                //ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(ListUserFirestoreRecyclerViewActivity.this, android.R.layout.simple_list_item_1, arrayList);
                                //usersListView.setAdapter(arrayAdapter);
                                //Set Adapter here
                                adapter = new ListUserFirestoreRecyclerViewAdapter(arrayList);
                                // Set adapter on recycler view
                                userRecyclerView.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }

                        }
                    });
                } else {
                    // list all user regardles online status
                    firestoreDatabase.collection(CHILD_USERS).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            Log.i(TAG, "listUser on firestore complete");
                            if (task.isSuccessful()) {
                                Log.i(TAG, "listUser on firestore complete and successful");
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    UserFirestoreModel userFirestoreModel = document.toObject(UserFirestoreModel.class);
                                    final String displayName;
                                    if (TextUtils.isEmpty(userFirestoreModel.getUserName())) {
                                        displayName = "";
                                    } else {
                                        displayName = userFirestoreModel.getUserName();
                                    }
                                    arrayList.add(userFirestoreModel.getUserMail() + " " + displayName);
                                    adapter.notifyDataSetChanged();
                                    uidList.add(document.getId());
                                    emailList.add(userFirestoreModel.getUserMail());
                                    displayNameList.add(displayName);
                                }
                                //ListView usersListView = (ListView) findViewById(R.id.lvListUserFirestore);
                                //ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(ListUserFirestoreRecyclerViewActivity.this, android.R.layout.simple_list_item_1, arrayList);
                                //usersListView.setAdapter(arrayAdapter);
                                //Set Adapter here
                                adapter = new ListUserFirestoreRecyclerViewAdapter(arrayList);
                                // Set adapter on recycler view
                                userRecyclerView.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }

                        }
                    });
                }
                hideProgressBar();

                /*
                userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        String uidSelected = uidList.get(position);
                        String emailSelected = emailList.get(position);
                        String displayNameSelected = displayNameList.get(position);
                        Intent intent = new Intent(ListUserFirestoreRecyclerViewActivity.this, SendMessageActivity.class);
                        intent.putExtra("UID", uidSelected);
                        intent.putExtra("EMAIL", emailSelected);
                        intent.putExtra("DISPLAYNAME", displayNameSelected);
                        startActivity(intent);
                        finish();
                    }
                });*/
            }
        });

        Button backToMain = findViewById(R.id.btnFirestoreDatabaseRvUserToMain);
        backToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ListUserFirestoreRecyclerViewActivity.this, MainActivity.class);
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