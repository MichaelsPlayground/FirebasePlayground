package de.androidcrypto.firebaseplayground;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class ChangeAuthUserPasswordActivity extends AppCompatActivity {

    /**
     * https://firebase.google.com/docs/auth/android/manage-users
     */

    com.google.android.material.textfield.TextInputEditText signedInUser;
    com.google.android.material.textfield.TextInputLayout oldUserPasswordLayout, newUserPasswordLayout;
    com.google.android.material.textfield.TextInputEditText oldUserPassword, newUserPassword;

    static final String TAG = "ChangeAuthUserPassword";
    // get the data from auth
    private static String authUserId = "", authUserEmail = "", authDisplayName = "", authPhotoUrl = "";

    private FirebaseAuth mAuth;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_auth_user_password);

        signedInUser = findViewById(R.id.etChangeAuthUserPasswordSignedInUser);
        progressBar = findViewById(R.id.pbChangeAuthUserPassword);

        oldUserPassword = findViewById(R.id.etChangeAuthUserPasswordOldPassword);
        oldUserPasswordLayout = findViewById(R.id.etChangeAuthUserPasswordOldPasswordLayout);
        newUserPassword = findViewById(R.id.etChangeAuthUserPasswordNewPassword);
        newUserPasswordLayout = findViewById(R.id.etChangeAuthUserPasswordNewPasswordLayout);

        // don't show the keyboard on startUp
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        Button backToMain = findViewById(R.id.btnChangeAuthUserPasswordToMain);

        Button changePassword = findViewById(R.id.btnChangeAuthUserPasswordChangePassword);
        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "change password of the current user");
                String oldPassword = oldUserPassword.getText().toString();
                if (oldPassword.length() < 6) {
                    Toast.makeText(getApplicationContext(),
                            "the old user password is too short, please change",
                            Toast.LENGTH_SHORT).show();
                    //runChangePassword.setVisibility(View.GONE);
                    return;
                }
                String newPassword = newUserPassword.getText().toString();
                if (newPassword.length() < 6) {
                    Toast.makeText(getApplicationContext(),
                            "the new user password is too short, please change",
                            Toast.LENGTH_SHORT).show();
                    //runChangePassword.setVisibility(View.GONE);
                    return;
                }
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                // Get auth credentials from the user for re-authentication. The example below shows
                // email and password credentials but there are multiple possible providers,
                // such as GoogleAuthProvider or FacebookAuthProvider.
                AuthCredential credential = EmailAuthProvider
                        .getCredential(authUserEmail, oldPassword);

                // Prompt the user to re-provide their sign-in credentials
                user.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    user.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.i(TAG, "user password updated");
                                                oldUserPassword.setText("");
                                                newUserPassword.setText("");
                                                String snackbarInfo = "the user password was changed";
                                                Snackbar snackbar = Snackbar
                                                        .make(view, snackbarInfo, Snackbar.LENGTH_LONG);
                                                snackbar.show();
                                            } else {
                                                Log.i(TAG, "Error password not updated");
                                            }
                                        }
                                    });
                                } else {
                                    Log.i(TAG, "password was not changed (wrong password ?)");
                                    Toast.makeText(getApplicationContext(),
                                            "error on changing the users auth password (wrong password ?)",
                                            Toast.LENGTH_SHORT).show();
                                }
                                Log.i(TAG, "user re-authenticated");
                            }
                        });
            }
        });

        backToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ChangeAuthUserPasswordActivity.this, MainActivity.class);
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