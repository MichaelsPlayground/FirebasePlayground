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
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    com.google.android.material.textfield.TextInputEditText signedInUser;

    static final String TAG = "Main";

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signedInUser = findViewById(R.id.etMainSignedInUser);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        Button signUpWithEmailAndPassword = findViewById(R.id.btnMainSignUpEmailPassword);
        signUpWithEmailAndPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SignUpEmailPasswordActivity.class);
                startActivity(intent);
            }
        });

        Button signInWithEmailAndPassword = findViewById(R.id.btnMainSignInEmailPassword);
        signInWithEmailAndPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SignInEmailPasswordActivity.class);
                startActivity(intent);
            }
        });

        Button signOut = findViewById(R.id.btnMainSignOut);
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "sign out the current user");
                mAuth.signOut();
                signedInUser.setText(null);
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