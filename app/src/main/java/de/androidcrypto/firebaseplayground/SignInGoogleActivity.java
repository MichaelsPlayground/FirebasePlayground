package de.androidcrypto.firebaseplayground;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class SignInGoogleActivity extends AppCompatActivity {

    com.google.android.material.textfield.TextInputEditText connectionStatus, signedInUser, eMail, password;
    static final String TAG = "SignInGoogle";
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_google);

        connectionStatus = findViewById(R.id.etSignInGoogleStatus);
        signedInUser = findViewById(R.id.etSignInGoogleSignedInUser);
        eMail = findViewById(R.id.etSignInGoogleEmail);
        password = findViewById(R.id.etSignInGooglePassword);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // todo remove fixed data
        //eMail.setText("q@q.com");
        //password.setText("123456");

        Button signIn = findViewById(R.id.btnSignInGoogleSignIn);
        Button signUp = findViewById(R.id.btnSignInGoogleSignUp);
        Button signOut = findViewById(R.id.btnSignInGoogleSignOut);
        Button backToMain = findViewById(R.id.btnSignInGoogleToMain);

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // todo validate input to avoid empty fields
                String emailData = Objects.requireNonNull(eMail.getText()).toString();
                String passwordData = Objects.requireNonNull(password.getText()).toString();
                String logString = String.format("SignInp with Email %s and password %s", emailData, passwordData);
                Log.i(TAG, logString);
/*
                mAuth.signInWithEmailAndPassword(emailData, passwordData)
                        .addOnCompleteListener(SignInEmailPasswordActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "signInWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    updateUI(user);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(view.getContext(), "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                    updateUI(null);
                                }
                            }
                        });*/
            }
        });

        // if the user has an account go to the sign in activity
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignInGoogleActivity.this, SignUpEmailPasswordActivity.class);
                startActivity(intent);
                finish();
            }
        });

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "sign out the current user");
                mAuth.signOut();
                updateUI(null);
            }
        });

        backToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignInGoogleActivity.this, MainActivity.class);
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
        if(currentUser != null){
            reload();
        } else {
            connectionStatus.setText(R.string.no_user_signed_in);
            signedInUser.setText(null);
        }
    }

    private void reload() {
        Objects.requireNonNull(mAuth.getCurrentUser()).reload().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    updateUI(mAuth.getCurrentUser());
                    Toast.makeText(getApplicationContext(),
                            "Reload successful!",
                            Toast.LENGTH_SHORT).show();
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
            String status = String.format("User UID: %s", user.getUid()) +
                    " is verified: " + user.isEmailVerified();
            connectionStatus.setText(status);
            String userData = String.format("Email: %s", user.getEmail());
            signedInUser.setText(userData);
            /*
            mBinding.status.setText(getString(R.string.emailpassword_status_fmt,
                    user.getEmail(), user.isEmailVerified()));
            mBinding.detail.setText(getString(R.string.firebase_status_fmt, user.getUid()));

            mBinding.emailPasswordButtons.setVisibility(View.GONE);
            mBinding.emailPasswordFields.setVisibility(View.GONE);
            mBinding.signedInButtons.setVisibility(View.VISIBLE);


             */
            if (user.isEmailVerified()) {
//                mBinding.verifyEmailButton.setVisibility(View.GONE);
            } else {
//                mBinding.verifyEmailButton.setVisibility(View.VISIBLE);
            }
        } else {
            connectionStatus.setText(R.string.signed_out);
            signedInUser.setText(null);
 /*
            mBinding.detail.setText(null);

            mBinding.emailPasswordButtons.setVisibility(View.VISIBLE);
            mBinding.emailPasswordFields.setVisibility(View.VISIBLE);
            mBinding.signedInButtons.setVisibility(View.GONE);

  */
        }
    }
}