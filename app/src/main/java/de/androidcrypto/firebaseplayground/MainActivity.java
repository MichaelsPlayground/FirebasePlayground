package de.androidcrypto.firebaseplayground;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "123";
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

        // todo enable buttons when a user is signed in

        // todo use Async Methods for sending notifications (Google)
        // the error happens while doing network operations on MainThread
        // solution: https://stackoverflow.com/questions/25093546/android-os-networkonmainthreadexception-at-android-os-strictmodeandroidblockgua
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        /**
         * authentication sign-in/out section
         */

        Button signUpWithEmailAndPassword = findViewById(R.id.btnMainSignUpEmailPassword);
        signUpWithEmailAndPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "sign up a user with email and password");
                Intent intent = new Intent(MainActivity.this, SignUpEmailPasswordActivity.class);
                startActivity(intent);
                //finish();
            }
        });

        Button signInWithEmailAndPassword = findViewById(R.id.btnMainSignInEmailPassword);
        signInWithEmailAndPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "sign in a user with email and password");
                Intent intent = new Intent(MainActivity.this, SignInEmailPasswordActivity.class);
                startActivity(intent);
                //finish();
            }
        });

        com.google.android.gms.common.SignInButton signInGoogle = findViewById(R.id.btnMainSignInGoogle);
        signInGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "sign in a user with a Google account");
                Intent intent = new Intent(MainActivity.this, SignInGoogleActivity.class);
                startActivity(intent);
                //finish();
            }
        });

        Button signOut = findViewById(R.id.btnMainSignOut);
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "sign out the current user");
                mAuth.signOut();
                signedInUser.setText(null);
            }
        });

        /**
         * authentication section
         */

        Button updateAuthUserProfile = findViewById(R.id.btnMainUpdateAuthUserProfile);
        updateAuthUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "update the auth user profile");
                Intent intent = new Intent(MainActivity.this, UpdateAuthUserProfileActivity.class);
                startActivity(intent);
                //finish();
            }
        });

        Button changeAuthUserPassword = findViewById(R.id.btnMainChangeAuthUserPassword);
        changeAuthUserPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, " change the auth user password");
                Intent intent = new Intent(MainActivity.this, ChangeAuthUserPasswordActivity.class);
                startActivity(intent);
                //finish();
            }
        });

        Button deleteAuthUserProfile = findViewById(R.id.btnMainDeleteAuthUserProfile);
        deleteAuthUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, " delete the auth user profile");
                Intent intent = new Intent(MainActivity.this, DeleteAuthUserProfileActivity.class);
                startActivity(intent);
                //finish();
            }
        });

        /**
         * database section
         */

        Button databaseUserProfile = findViewById(R.id.btnMainDatabaseUser);
        databaseUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "show the user profile on Realtime Database");
                Intent intent = new Intent(MainActivity.this, DatabaseUserActivity.class);
                startActivity(intent);
                //finish();
            }
        });

        Button updateUserImage = findViewById(R.id.btnMainUpdateUserImage);
        updateUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "update the user profile");
                Intent intent = new Intent(MainActivity.this, UpdateUserImageActivity.class);
                //Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                //finish();
            }
        });

        Button sendMessage = findViewById(R.id.btnMainSendMessage);
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "send a message to another user");
                Intent intent = new Intent(MainActivity.this, SendMessageActivity.class);
                startActivity(intent);
                //finish();
            }
        });

        Button listMessages = findViewById(R.id.btnMainListMessages);
        listMessages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "list messages on database");
                Intent intent = new Intent(MainActivity.this, ListMessagesActivity.class);
                startActivity(intent);
                //finish();
            }
        });

        /**
         * firestore database section
         */

        Button firestoreDatabaseUserProfile = findViewById(R.id.btnMainFirestoreDatabaseUser);
        firestoreDatabaseUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "show the user profile on Firestore Database");
                Intent intent = new Intent(MainActivity.this, FirestoreDatabaseUserActivity.class);
                startActivity(intent);
                //finish();
            }
        });

        /**
         * storage section
         */

        Button uploadImage = findViewById(R.id.btnMainUploadImage);
        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "upload an image to Storage");
                Intent intent = new Intent(MainActivity.this, UploadImageActivity.class);
                startActivity(intent);
                //finish();
            }
        });

        Button listImages = findViewById(R.id.btnMainListImages);
        listImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "list images on storage");
                Intent intent = new Intent(MainActivity.this, ListImagesActivity.class);
                startActivity(intent);
                //finish();
            }
        });

        Button downloadImage = findViewById(R.id.btnMainDownloadImage);
        downloadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "download an image from Storage");
                Intent intent = new Intent(MainActivity.this, DownloadImageActivity.class);
                startActivity(intent);
                //finish();
            }
        });

        /**
         * service section
         */

        Button listUser = findViewById(R.id.btnMainListUser);
        listUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "list user on database");
                Intent intent = new Intent(MainActivity.this, ListUserActivity.class);
                startActivity(intent);
                //finish();
            }
        });

        // we need this to receive notifications
        createNotificationChannel();

        Button sendNotification = findViewById(R.id.btnMainSendNotification);
        sendNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "send notification");
                Intent intent = new Intent(MainActivity.this, SendNotificationActivity.class);
                startActivity(intent);
                //finish();
            }
        });


    }

    public void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "CH NAME";
            String description = "CH description";
            //CharSequence name = getString(R.string.channel_name);
            //String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
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
            String userData = String.format("Email: %s", user.getEmail())
                    + String.format("\nemail address is verified: %s", user.isEmailVerified());
            if (user.getDisplayName() != null) {
                userData += String.format("\ndisplay name: %s", Objects.requireNonNull(user.getDisplayName()).toString());
            } else {
                userData += "\nno display name available";
            }
            if (user.getPhotoUrl() != null) {
                userData += String.format("\nphoto url: %s", Objects.requireNonNull(user.getPhotoUrl()).toString());
            } else {
                userData += "\nno photo url available";
            }
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