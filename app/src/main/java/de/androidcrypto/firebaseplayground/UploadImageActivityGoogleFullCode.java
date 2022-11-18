package de.androidcrypto.firebaseplayground;

import static de.androidcrypto.firebaseplayground.MyUploadService.ACTION_UPLOAD;
import static de.androidcrypto.firebaseplayground.MyUploadService.EXTRA_FILE_URI;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;
import java.util.Objects;

public class UploadImageActivityGoogleFullCode extends AppCompatActivity {

    com.google.android.material.textfield.TextInputEditText signedInUser, receiveUser;
    com.google.android.material.textfield.TextInputEditText edtMessage, edtLinkToImage, userPhotoUrl, userPublicKey, userName;
    com.google.android.material.textfield.TextInputLayout edtMessageLayout;
    TextView warningNoData;

    static final String TAG = "UploadImage";
    // get the data from auth
    private static String authUserId = "", authUserEmail, authDisplayName, authPhotoUrl;
    private static String receiveUserId = "", receiveUserEmail = "", receiveUserDisplayName = "";

    private static final String KEY_FILE_URI = "key_file_uri";
    private static final String KEY_DOWNLOAD_URL = "key_download_url";

    private BroadcastReceiver mBroadcastReceiver;
    private FirebaseAuth mAuth;

    private Uri mDownloadUrl = null;
    private Uri mFileUri = null;
    private ActivityResultLauncher<String[]> intentLauncher;

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);

        signedInUser = findViewById(R.id.etUploadImageSignedInUser);
        //receiveUser = findViewById(R.id.etUploadImageReceiveUser);
        progressBar = findViewById(R.id.pbUploadImage);

        //edtMessageLayout = findViewById(R.id.etUploadImageMessageLayout);
        //edtMessage = findViewById(R.id.etUploadImageMessage);
        edtLinkToImage = findViewById(R.id.etUploadImageLinkToImage);
/*
        warningNoData = findViewById(R.id.tvDatabaseUserNoData);
        userId = findViewById(R.id.etDatabaseUserUserId);
        userEmail = findViewById(R.id.etDatabaseUserUserEmail);
        userPhotoUrl = findViewById(R.id.etDatabaseUserPhotoUrl);
        userPublicKey = findViewById(R.id.etDatabaseUserPublicKey);
        userName = findViewById(R.id.etDatabaseUserUserName);
*/
        // don't show the keyboard on startUp
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Restore instance state
        if (savedInstanceState != null) {
            mFileUri = savedInstanceState.getParcelable(KEY_FILE_URI);
            mDownloadUrl = savedInstanceState.getParcelable(KEY_DOWNLOAD_URL);
        }
        onNewIntent(getIntent());

        intentLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(), fileUri -> {
                    if (fileUri != null) {
                        Log.i(TAG, "intentLauncher with URI: " + fileUri.toString());
                        uploadFromUri(fileUri);
                    } else {
                        Log.w(TAG, "File URI is null");
                    }
                });

        // Local broadcast receiver
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "onReceive:" + intent);
                hideProgressBar();

                switch (intent.getAction()) {
                    case MyDownloadService.DOWNLOAD_COMPLETED:
                        // Get number of bytes downloaded
                        long numBytes = intent.getLongExtra(MyDownloadService.EXTRA_BYTES_DOWNLOADED, 0);

                        // Alert success
                        showMessageDialog(getString(R.string.success), String.format(Locale.getDefault(),
                                "%d bytes downloaded from %s",
                                numBytes,
                                intent.getStringExtra(MyDownloadService.EXTRA_DOWNLOAD_PATH)));
                        break;
                    case MyDownloadService.DOWNLOAD_ERROR:
                        // Alert failure
                        showMessageDialog("Error", String.format(Locale.getDefault(),
                                "Failed to download from %s",
                                intent.getStringExtra(MyDownloadService.EXTRA_DOWNLOAD_PATH)));
                        break;
                    case MyUploadService.UPLOAD_COMPLETED:
                    case MyUploadService.UPLOAD_ERROR:
                        onUploadResultIntent(intent);
                        break;
                }
            }
        };

        // Initialize Firebase Database
        // https://fir-playground-1856e-default-rtdb.europe-west1.firebasedatabase.app/
        // if the database location is not us we need to use the reference:
        //mDatabaseReference = FirebaseDatabase.getInstance("https://fir-playground-1856e-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        // the following can be used if the database server location is us
        //mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        // Initialize Firebase Storage
        //mStorage = FirebaseStorage.getInstance();
        // gs://fir-playground-1856e.appspot.com
        //mStorageReference = mStorage.getReference();
        // Create a child reference
        // imagesRef now points to "images"
        //StorageReference imagesReference = mStorageReference.child("images");

        Intent intent = getIntent();
        receiveUserId = intent.getStringExtra("UID");
        if (receiveUserId != null) {
            Log.i(TAG, "selectedUid: " + receiveUserId);
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
        receiveUser.setText(receiveUserString);
        Log.i(TAG, "receiveUser: " + receiveUserString);

        //       Button loadData = findViewById(R.id.btnDatabaseUserLoad);
        //       Button savaData = findViewById(R.id.btnDatabaseUserSave);

        Button selectImage = findViewById(R.id.btnUploadImageSelectImage);
        Button backToMain = findViewById(R.id.btnUploadImageToMain);

        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "select an image from local to store");
                showProgressBar("uploading");
                // Pick an image from storage
                intentLauncher.launch(new String[]{ "image/*" });
            }
        });

        backToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UploadImageActivityGoogleFullCode.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

/*
static data:
authUser:
Email: michael.telefon08@gmail.com
UID: VgNGhMth85Y0Szg6FxLMcWkEpmA3
Display Name: Michael Fehr

receiveUser:
Email: klaus.zwang.1934@gmail.com
UID: 0QCS5u2UnxYURlbntvVTA6ZTbaO2
Display Name: klaus.zwang.1934@gmail.com
 */

        edtMessageLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        Button send = findViewById(R.id.btnUploadImageSend);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    /**
     * gets the filename of the image
     * https://developer.android.com/training/secure-file-sharing/retrieve-info.html
     * https://stackoverflow.com/a/38304115/8166854
     */
    private String queryNameFromUri(ContentResolver resolver, Uri uri) {
        Cursor returnCursor =
                resolver.query(uri, null, null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        return name;
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // Check if this Activity was launched by clicking on an upload notification
        if (intent.hasExtra(MyUploadService.EXTRA_DOWNLOAD_URL)) {
            onUploadResultIntent(intent);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        updateUIStorage(mAuth.getCurrentUser());

        // Register receiver for uploads and downloads
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.registerReceiver(mBroadcastReceiver, MyDownloadService.getIntentFilter());
        manager.registerReceiver(mBroadcastReceiver, MyUploadService.getIntentFilter());

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            reload();
        } else {
            signedInUser.setText("no user is signed in");
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        // Unregister download receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putParcelable(KEY_FILE_URI, mFileUri);
        out.putParcelable(KEY_DOWNLOAD_URL, mDownloadUrl);
        //out.putParcelable(KEY_FILE_NAME, mFileName);
    }

    private void uploadFromUri(Uri fileUri) {
        Log.i(TAG, "uploadFromUri:src:" + fileUri.toString());

        // Save the File URI
        mFileUri = fileUri;

        // Clear the last download, if any
        updateUIStorage(mAuth.getCurrentUser());
        mDownloadUrl = null;

        // Start MyUploadService to upload the file, so that the file is uploaded
        // even if this Activity is killed or put in the background
        startService(new Intent(this, MyUploadService.class)
                .putExtra(EXTRA_FILE_URI, fileUri)
                .setAction(ACTION_UPLOAD));

        // Show loading spinner
        showProgressBar(getString(R.string.progress_uploading));
    }

    private void onUploadResultIntent(Intent intent) {
        // Got a new intent from MyUploadService with a success or failure
        mDownloadUrl = intent.getParcelableExtra(MyUploadService.EXTRA_DOWNLOAD_URL);
        mFileUri = intent.getParcelableExtra(EXTRA_FILE_URI);

        updateUIStorage(mAuth.getCurrentUser());
    }

    private void updateUIStorage(FirebaseUser user) {
        // Signed in or Signed out
        if (user != null) {
            //binding.layoutSignin.setVisibility(View.GONE);
            //binding.layoutStorage.setVisibility(View.VISIBLE);
        } else {
            //binding.layoutSignin.setVisibility(View.VISIBLE);
            //binding.layoutStorage.setVisibility(View.GONE);
        }

        // Download URL and Download button
        if (mDownloadUrl != null) {
            edtLinkToImage.setText(mDownloadUrl.toString());
            //binding.layoutDownload.setVisibility(View.VISIBLE);
        } else {
            edtLinkToImage.setText(null);
            //binding.layoutDownload.setVisibility(View.GONE);
        }
    }

    private void showMessageDialog(String title, String message) {
        AlertDialog ad = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .create();
        ad.show();
    }

    private void showProgressBar(String caption) {
        //binding.caption.setText(caption);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        //binding.caption.setText("");
        progressBar.setVisibility(View.INVISIBLE);
    }

    /**
     * basic
     */

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
            signedInUser.setText(userData);
            Log.i(TAG, "authUser: " + userData);
        } else {
            signedInUser.setText(null);
        }
    }
}