package de.androidcrypto.firebaseplayground;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class SignInGoogleActivity extends AppCompatActivity {

    com.google.android.material.textfield.TextInputEditText connectionStatus, signedInUser;
    static final String TAG = "SignInGoogle";
    private FirebaseAuth mAuth;
    private SignInClient signInClient;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_google);

        progressBar = findViewById(R.id.pbSignInGoogle);

        connectionStatus = findViewById(R.id.etSignInGoogleStatus);
        signedInUser = findViewById(R.id.etSignInGoogleSignedInUser);

        // Configure Google Sign In
        signInClient = Identity.getSignInClient(getApplicationContext());
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Display One-Tap Sign In if user isn't logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            oneTapSignIn();
        }

        com.google.android.gms.common.SignInButton signInBtn = findViewById(R.id.btnSignInGoogleSignIn);
        Button signUpBtn = findViewById(R.id.btnSignInGoogleSignUp);
        Button signOutBtn = findViewById(R.id.btnSignInGoogleSignOut);
        Button backToMain = findViewById(R.id.btnSignInGoogleToMain);

        signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "SignIn with an Google account");
                signIn();
            }
        });

        // if the user has an account go to the sign in activity
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignInGoogleActivity.this, SignUpEmailPasswordActivity.class);
                startActivity(intent);
                finish();
            }
        });

        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "sign out the current user");
                signOut();
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
        updateUI(currentUser);
    }

    private void oneTapSignIn() {
        // Configure One Tap UI
        BeginSignInRequest oneTapRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                        BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                                .setSupported(true)
                                .setServerClientId(getString(R.string.default_web_client_id))
                                .setFilterByAuthorizedAccounts(true)
                                .build()
                )
                .build();

        // Display the One Tap UI
        signInClient.beginSignIn(oneTapRequest)
                .addOnSuccessListener(new OnSuccessListener<BeginSignInResult>() {
                    @Override
                    public void onSuccess(BeginSignInResult beginSignInResult) {
                        launchSignIn(beginSignInResult.getPendingIntent());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // No saved credentials found. Launch the One Tap sign-up flow, or
                        // do nothing and continue presenting the signed-out UI.
                    }
                });
    }

    private void launchSignIn(PendingIntent pendingIntent) {
        try {
            IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder(pendingIntent)
                    .build();
            signInLauncher.launch(intentSenderRequest);
        } catch (Exception e) {
            Log.e(TAG, "Couldn't start Sign In: " + e.getLocalizedMessage());
        }
    }

    private final ActivityResultLauncher<IntentSenderRequest> signInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartIntentSenderForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    handleSignInResult(result.getData());
                }
            }
    );

    private void handleSignInResult(Intent data) {
        try {
            // Google Sign In was successful, authenticate with Firebase
            SignInCredential credential = signInClient.getSignInCredentialFromIntent(data);
            String idToken = credential.getGoogleIdToken();
            Log.d(TAG, "firebaseAuthWithGoogle:" + credential.getId());
            firebaseAuthWithGoogle(idToken);
        } catch (ApiException e) {
            // Google Sign In failed, update UI appropriately
            Log.w(TAG, "Google sign in failed", e);
            updateUI(null);
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        showProgressBar();
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(SignInGoogleActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            //Snackbar.make(mBinding.mainLayout, "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            View view = findViewById(R.id.signin_google_layout);
                            Snackbar.make(view, "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                        hideProgressBar();
                    }
                });
    }

    private void signIn() {
        GetSignInIntentRequest signInRequest = GetSignInIntentRequest.builder()
                .setServerClientId(getString(R.string.default_web_client_id))
                .build();

        signInClient.getSignInIntent(signInRequest)
                .addOnSuccessListener(new OnSuccessListener<PendingIntent>() {
                    @Override
                    public void onSuccess(PendingIntent pendingIntent) {
                        launchSignIn(pendingIntent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Google Sign-in failed", e);
                    }
                });
    }

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        signInClient.signOut().addOnCompleteListener(SignInGoogleActivity.this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        hideProgressBar();
        if (user != null) {
            String status = String.format("User UID: %s", user.getUid()) +
                    " is verified: " + user.isEmailVerified();
            connectionStatus.setText(status);
            String userData = String.format("Email: %s", user.getEmail());
            signedInUser.setText(userData);
        } else {
            connectionStatus.setText(R.string.signed_out);
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