package de.androidcrypto.firebaseplayground;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.List;
import java.util.Objects;

public class DeleteAuthUserProfileActivity extends AppCompatActivity {

    /**
     * https://firebase.google.com/docs/auth/android/manage-users
     */

    TextView actionRequirements;
    com.google.android.material.textfield.TextInputEditText signedInUser;
    com.google.android.material.textfield.TextInputLayout oldUserPasswordLayout;
    com.google.android.material.textfield.TextInputEditText oldUserPassword;
    com.google.android.gms.common.SignInButton signInGoogle;

    static final String TAG = "DeleteAuthUserProfile";
    // get the data from auth
    private static String authUserId = "", authUserEmail = "", authDisplayName = "", authPhotoUrl = "";
    private String providerId = ""; // eg password or google.com
    //private String googleIdToken = "";

    View storedView; // for snackbar
    private FirebaseAuth mAuth;
    AuthCredential authCredential;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_auth_user_profile);

        signedInUser = findViewById(R.id.etDeleteAuthUserProfileSignedInUser);
        progressBar = findViewById(R.id.pbDeleteAuthUserProfile);
        actionRequirements = findViewById(R.id.tvtDeleteAuthUserProfileRequirement);

        oldUserPassword = findViewById(R.id.etDeleteAuthUserProfileOldPassword);
        oldUserPasswordLayout = findViewById(R.id.etDeleteAuthUserProfileOldPasswordLayout);

        // don't show the keyboard on startUp
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        Button backToMain = findViewById(R.id.btnDeleteAuthUserProfileToMain);

        Button deleteUser = findViewById(R.id.btnDeleteAuthUserProfileDeleteUser);
        deleteUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "delete the current user");
                storedView = view;
                // avoid deleting account on accident
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("You are going to delete the current user");
                builder.setMessage("Are you sure ?");
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // delete the user
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user == null) return;
                        Log.i(TAG, "delete the current user, providerId is " + providerId);
                        /**
                         * workflow for Email-password user
                         */

                        if (providerId.equals("password")) {
                            Log.i(TAG, "delete the current user - workflow Email-password");
                            String oldPassword = oldUserPassword.getText().toString();
                            if (oldPassword.length() < 6) {
                                Toast.makeText(getApplicationContext(),
                                        "the user password is too short, please change",
                                        Toast.LENGTH_SHORT).show();
                                //runChangePassword.setVisibility(View.GONE);
                                return;
                            }
                            authCredential = EmailAuthProvider.getCredential(authUserEmail, oldPassword);
                            userDelete();
                        }

                        if (providerId.equals("google.com")) {
                            Log.i(TAG, "delete the current user - workflow Google");
                            GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(getApplicationContext());

                            if (signInAccount != null) {
                                Log.i(TAG, "googleSignInAccount.getId: " + signInAccount.getId());
                                authCredential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null);
                                Log.i(TAG, "authCredential: " + authCredential.getSignInMethod() + " zza: " + authCredential.zza().getProvider());
                                userDelete();
                            } else {
                                Log.i(TAG, "googleSignInAccount.getId: NULL");

                            }

                        }


                        // workflow for


                        // Get auth credentials from the user for re-authentication. The example below shows
                        // email and password credentials but there are multiple possible providers,
                        // such as GoogleAuthProvider or FacebookAuthProvider.

/*
                        // Prompt the user to re-provide their sign-in credentials
                        user.reauthenticate(credential)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            user.delete()
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                oldUserPassword.setText("");
                                                                Log.i(TAG, "user account deleted");
                                                                signedInUser.setText("the user was deleted");
                                                                String snackbarInfo = "the user account was deleted. " +
                                                                        "This playground does NOT delete the user in database and storage units !";
                                                                Snackbar snackbar = Snackbar
                                                                        .make(view, snackbarInfo, Snackbar.LENGTH_LONG);
                                                                snackbar.show();
                                                            } else {
                                                                Log.e(TAG, "error on deleting the users auth profile");
                                                                Toast.makeText(getApplicationContext(),
                                                                        "error on deleting the users auth profile",
                                                                        Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                        } else {
                                            Log.i(TAG, "user was not deleted");
                                            Toast.makeText(getApplicationContext(),
                                                    "error on deleting the users auth profile (wrong password ?)",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                        Log.i(TAG, "user re-authenticated");
                                    }
                                });
                        } else {
                            Log.i(TAG, "user is signed in with Google");
                            // the only other auth provider is google.com in this example
                            // delete the user immediately
                            user.delete()
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e(TAG, "execption on deletion: " + e);
                                        }
                                    })
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                oldUserPassword.setText("");
                                                Log.i(TAG, "user account deleted");
                                                signedInUser.setText("the user was deleted");
                                                String snackbarInfo = "the user account was deleted. " +
                                                        "This playground does NOT delete in database and storage units !";
                                                Snackbar snackbar = Snackbar
                                                        .make(view, snackbarInfo, Snackbar.LENGTH_LONG);
                                                snackbar.show();
                                            } else {
                                                Log.e(TAG, "error on deleting the users auth profile");
                                                Toast.makeText(getApplicationContext(),
                                                        "error on deleting the users auth profile",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                        }*/
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                        dialog.dismiss();
                        return;
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        signInGoogle = findViewById(R.id.btnDeleteAuthUserProfileSignInGoogle);
        signInGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "sign in a user with a Google account");
                Intent intent = new Intent(DeleteAuthUserProfileActivity.this, SignInGoogleActivity.class);
                startActivity(intent);
                //finish();
            }
        });

        backToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DeleteAuthUserProfileActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * code taken from https://stackoverflow.com/questions/61797048/error-while-trying-to-re-authenticate-on-firebase-using-google-sign-in-credentia
     */

    private void userDelete() {
        Log.i(TAG, "userDelete: Deleting user...");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i(TAG, "onSuccess: User deleted");
                Log.i(TAG, "userDelete: Signing out...");
                oldUserPassword.setText("");
                Log.i(TAG, "user account deleted");
                signedInUser.setText("the user was deleted");
                String snackbarInfo = "the user account was deleted. " +
                        "This playground does NOT delete the user in database and storage units !";
                Snackbar snackbar = Snackbar
                        .make(storedView, snackbarInfo, Snackbar.LENGTH_LONG);
                snackbar.show();
                signOut(); //Signing out from Firebase and Google log-in
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "onFailure: Error deleting user, probably need to reauthenticate");
                Log.e(TAG, "onFailure: Error:", e);
                reAuthenticateUser(); //reauth and delete user
            }
        });
    }

    private void reAuthenticateUser() {
        Log.i(TAG, "reAuthenticateUser: Re-Rauthenticating");
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.reauthenticate(authCredential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.i(TAG, "Reauthenticated");
                    Log.i(TAG, "onComplete: Try to delete user again...");
                    user.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i(TAG, "onSuccess: user deleted successfully");
                            Log.i(TAG, "onComplete: signing out...");
                            Log.i(TAG, "user account deleted");
                            signedInUser.setText("the user was deleted");
                            String snackbarInfo = "the user account was deleted on re-authentication. " +
                                    "This playground does NOT delete the user in database and storage units !";
                            Snackbar snackbar = Snackbar
                                    .make(storedView, snackbarInfo, Snackbar.LENGTH_LONG);
                            snackbar.show();
                            signOut();
                        }
                    });
                } else {
                    Log.e(TAG, "onComplete: ERROR deleting user: task fail", task.getException());
                    if (providerId.equals("password")) {
                        Toast.makeText(getApplicationContext(),
                                "could not delete the current user (wrong password ?)",
                                Toast.LENGTH_SHORT).show();
                        signInGoogle.setVisibility(View.GONE);
                    }
                    if (providerId.equals("google.com")) {
                        Toast.makeText(getApplicationContext(),
                                "please sign-in to your Google account again and delete a second time",
                                Toast.LENGTH_SHORT).show();
                        signInGoogle.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    private void signOut() {
        //sign out from firebase
        FirebaseAuth.getInstance().signOut();
        //sign out from "google sign in"
        /*
        googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //Get back to login screen
                Intent intent = new Intent(getContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                getActivity().finish();
            }
        });

         */
    }

    /**
     * general code
     */

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            reload();
            checkForSignInProvider();
        } else {
            signedInUser.setText("no user is signed in");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkForSignInProvider();
    }

    private void checkForSignInProvider() {
        // get the providerId
        // get provider that was used for sign in
        // https://stackoverflow.com/a/66118499/8166854
        // So, if (strProvider.equals("password")) then the authentication is by Email + Password,
        // if (strProvider.equals("google.com")) then the authentication is via Google,
        // if (strProvider.equals("facebook.com")) then the authentication is via Facebook.
        mAuth.getAccessToken(false).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
            @Override
            public void onSuccess(GetTokenResult getTokenResult) {
                providerId = getTokenResult.getSignInProvider();
                String messageString = "You are going to delete the current user from the auth database.";
                Log.i(TAG, "providerId: " + providerId);
                if (providerId.equals("password")) {
                    oldUserPasswordLayout.setVisibility(View.VISIBLE);
                    messageString += "\nAs this is a sensitive process Firebase Auth may require the user's ";
                    messageString += "password to re-authenticate.";
                    messageString += "\nPlease provide the password to proceed";
                    actionRequirements.setText(messageString);
                    actionRequirements.setVisibility(View.VISIBLE);
                } else {
                    oldUserPasswordLayout.setVisibility(View.GONE);
                    messageString += "\nAs this is a sensitive process and you signed-in with a Google account ";
                    messageString += "Firebase Auth may require that the user ";
                    messageString += "is sign out and then sign-in to re-authenticate.";
                    messageString += "\nPlease check for a button showing up below";
                    actionRequirements.setText(messageString);
                    actionRequirements.setVisibility(View.VISIBLE);
                }

            }
        });
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
            authUserEmail = user.getEmail();
            if (user.getDisplayName() != null) {
                authDisplayName = Objects.requireNonNull(user.getDisplayName()).toString();
            } else {
                authDisplayName = "";
            }
            if (user.getPhotoUrl() != null) {
                authPhotoUrl = Objects.requireNonNull(user.getPhotoUrl()).toString();
            } else {
                authPhotoUrl = "";
            }

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