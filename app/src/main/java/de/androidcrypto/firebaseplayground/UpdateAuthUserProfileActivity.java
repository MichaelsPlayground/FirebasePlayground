package de.androidcrypto.firebaseplayground;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import de.androidcrypto.firebaseplayground.models.UserModel;

public class UpdateAuthUserProfileActivity extends AppCompatActivity {

    /**
     * https://firebase.google.com/docs/auth/android/manage-users
     */

    com.google.android.material.textfield.TextInputEditText signedInUser;
    com.google.android.material.textfield.TextInputEditText userId, userEmail, userPhotoUrl, userName;
    TextView warningNoData;

    static final String TAG = "UpdateAuthUserProfile";
    // get the data from auth
    private static String authUserId = "", authUserEmail = "", authDisplayName = "", authPhotoUrl = "";

    private FirebaseAuth mAuth;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_auth_user_profile);

        signedInUser = findViewById(R.id.etUpdateAuthUserProfileSignedInUser);
        progressBar = findViewById(R.id.pbUpdateAuthUserProfile);

        warningNoData = findViewById(R.id.tvUpdateAuthUserProfileNoData);
        userId = findViewById(R.id.etUpdateAuthUserProfileUserId);
        userEmail = findViewById(R.id.etUpdateAuthUserProfileUserEmail);
        userPhotoUrl = findViewById(R.id.etUpdateAuthUserProfilePhotoUrl);
        userName = findViewById(R.id.etUpdateAuthUserProfileUserName);


        // don't show the keyboard on startUp
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        Button loadData = findViewById(R.id.btnUpdateAuthUserProfileLoad);
        Button saveUserNameData = findViewById(R.id.btnUpdateAuthUserProfileSaveUserName);
        Button backToMain = findViewById(R.id.btnUpdateAuthUserProfileToMain);
/*
        loadData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                warningNoData.setVisibility(View.GONE);
                showProgressBar();
                Log.i(TAG, "load user data from database for user id: " + authUserId);
                if (!authUserId.equals("")) {
                    mDatabase.child("users").child(authUserId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            hideProgressBar();
                            if (!task.isSuccessful()) {
                                Log.e(TAG, "Error getting data", task.getException());
                            } else {
                                // check for a null value means no user data were saved before
                                UserModel userModel = task.getResult().getValue(UserModel.class);
                                Log.i(TAG, String.valueOf(userModel));
                                if (userModel == null) {
                                    Log.i(TAG, "userModel is null, show message");
                                    warningNoData.setVisibility(View.VISIBLE);
                                    // get data from user
                                    userId.setText(authUserId);
                                    userEmail.setText(authUserEmail);
                                    userName.setText(usernameFromEmail(authUserEmail));
                                    userPublicKey.setText("not in use");
                                    userPhotoUrl.setText(authPhotoUrl);
                                } else {
                                    Log.i(TAG, "userModel email: " + userModel.getUserMail());
                                    warningNoData.setVisibility(View.GONE);
                                    // get data from user
                                    userId.setText(authUserId);
                                    userEmail.setText(userModel.getUserMail());
                                    userName.setText(userModel.getUserName());
                                    userPublicKey.setText(userModel.getUserPublicKey());
                                    userPhotoUrl.setText(userModel.getUserPhotoUrl());
                                }
                            }
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(),
                            "sign in a user before loading",
                            Toast.LENGTH_SHORT).show();
                    hideProgressBar();
                }
            }
        });
*/
        saveUserNameData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgressBar();
                Log.i(TAG, "save user name and photo url to auth database for user id: " + authUserId);
                if (!authUserId.equals("")) {
                    String dispName = userName.getText().toString();
                    String photoUrl = userPhotoUrl.getText().toString();
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(dispName)
                            .setPhotoUri(Uri.parse(photoUrl))
                            .build();
                    user.updateProfile(profileUpdates)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "user profile updated.");
                                        Snackbar snackbar = Snackbar
                                                .make(view, "data written to database", Snackbar.LENGTH_SHORT);
                                        snackbar.show();
                                        reload();
                                    }
                                }
                            });

                } else {
                    Toast.makeText(getApplicationContext(),
                            "sign in a user before saving",
                            Toast.LENGTH_SHORT).show();
                }
                hideProgressBar();
            }
        });

        Button sendEmailAddressVerificationMail = findViewById(R.id.btnUpdateAuthUserProfileSendVerificationMail);
        sendEmailAddressVerificationMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "send an email to verify the email address");
                FirebaseAuth auth = FirebaseAuth.getInstance();
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    if (user.isEmailVerified()) {
                        Log.i(TAG, "user email address is already verified");
                        Toast.makeText(getApplicationContext(),
                                "user email is already verified, no email is send",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // email address is not verified yet
                    user.sendEmailVerification()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.i(TAG, "verification email sent");
                                        String snackbarInfo = "a verification email is sent to " +
                                                user.getEmail() + ". Please click on the link in the email to verifiy.";
                                        Snackbar snackbar = Snackbar
                                                .make(view, snackbarInfo, Snackbar.LENGTH_LONG);
                                        snackbar.show();
                                    }
                                }
                            });
                }
            }
        });

        backToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UpdateAuthUserProfileActivity.this, MainActivity.class);
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
            userId.setText(authUserId);
            authUserEmail = user.getEmail();
            userEmail.setText(authUserEmail);
            if (user.getDisplayName() != null) {
                authDisplayName = Objects.requireNonNull(user.getDisplayName()).toString();
            } else {
                authDisplayName = "";
            }
            userName.setText(authDisplayName);
            if (user.getPhotoUrl() != null) {
                authPhotoUrl = Objects.requireNonNull(user.getPhotoUrl()).toString();
            } else {
                authPhotoUrl = "";
            }
            userPhotoUrl.setText(authPhotoUrl);

            String userData = "user id: " + authUserId + "\n";
            userData += String.format("email: %s", authUserEmail) + "\n";
            userData += String.format("email address is verified: %s", user.isEmailVerified()) + "\n";
            if (user.getDisplayName() != null) {
                userData += String.format("display name: %s", authDisplayName) + "\n";
            } else {
                userData += "no display name available" + "\n";
            }
            if (user.getPhotoUrl() != null) {
                userData += String.format("photo url: %s", Objects.requireNonNull(user.getPhotoUrl()).toString());
            } else {
                userData += "no photo url available";
            }
            signedInUser.setText(userData);
            if (user.isEmailVerified()) {
//                mBinding.verifyEmailButton.setVisibility(View.GONE);
            } else {
//                mBinding.verifyEmailButton.setVisibility(View.VISIBLE);
            }
        } else {
            signedInUser.setText(null);
        }
    }

    // try to read the entry for the signed-in user in RealtimeDatabase
    private void readUserDatabase(FirebaseUser user) {


    }

    /*
        public void writeNewUser(String userId, String name, String email, String photoUrl, String publicKey) {
            UserModel user = new UserModel(name, email, photoUrl, publicKey);
            mDatabase.child("users").child(userId).setValue(user);
        }
    */
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