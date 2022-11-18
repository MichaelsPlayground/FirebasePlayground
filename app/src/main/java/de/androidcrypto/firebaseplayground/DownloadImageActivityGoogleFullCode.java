package de.androidcrypto.firebaseplayground;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import de.androidcrypto.firebaseplayground.models.MessageModel;

public class DownloadImageActivityGoogleFullCode extends AppCompatActivity {

    com.google.android.material.textfield.TextInputEditText signedInUser, selectedImage;
    com.google.android.material.textfield.TextInputEditText edtMessage, edtRoomId, userPhotoUrl, userPublicKey, userName;
    com.google.android.material.textfield.TextInputLayout edtMessageLayout;
    TextView warningNoData;

    static final String TAG = "SelectImage";
    // get the data from auth
    private static String authUserId = "", authUserEmail, authDisplayName, authPhotoUrl;
    private static String selectedImageFileReference = "";
    private static String receiveUserId = "", receiveUserEmail = "", receiveUserDisplayName = "";

    private DatabaseReference mDatabaseReference;
    private FirebaseAuth mAuth;
    ProgressBar progressBar;

    private static final String KEY_FILE_URI = "key_file_uri";
    private static final String KEY_DOWNLOAD_URL = "key_download_url";

    private BroadcastReceiver mBroadcastReceiver;
    private Uri mDownloadUrl = null;
    private Uri mFileUri = null;
    private String mFileName = null;
    private ActivityResultLauncher<String[]> intentLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_image);

        signedInUser = findViewById(R.id.etDownloadImageSignedInUser);
        selectedImage = findViewById(R.id.etDownloadImageSelectedImage);
        progressBar = findViewById(R.id.pbDownloadImage);

        edtMessageLayout = findViewById(R.id.etDownloadImageMessageLayout);
        edtMessage = findViewById(R.id.etDownloadImageMessage);
        edtRoomId = findViewById(R.id.etDownloadImageRoomId);

        // Restore instance state
        if (savedInstanceState != null) {
            mFileUri = savedInstanceState.getParcelable(KEY_FILE_URI);
            mDownloadUrl = savedInstanceState.getParcelable(KEY_DOWNLOAD_URL);
        }
        onNewIntent(getIntent());

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
                    //case MyUploadService.UPLOAD_COMPLETED:
                    //case MyUploadService.UPLOAD_ERROR:
                    //    onUploadResultIntent(intent);
                    //    break;
                }
            }
        };


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
        // Initialize Firebase Database
        // https://fir-playground-1856e-default-rtdb.europe-west1.firebasedatabase.app/
        // if the database location is not us we need to use the reference:
        mDatabaseReference = FirebaseDatabase.getInstance("https://fir-playground-1856e-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        // the following can be used if the database server location is us
        //mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        Intent intent = getIntent();
        selectedImageFileReference = intent.getStringExtra("FILEREFERENCE");
        mFileName = intent.getStringExtra("FILENAME");
        //mFileUri = intent.getStringExtra("FILEURI");
        if (selectedImageFileReference != null) {
            Log.i(TAG, "selectedImageFileReference: " + selectedImageFileReference);
            selectedImage.setText(selectedImageFileReference + "\nURI :" + intent.getStringExtra("FILEURI"));
            beginDownload();
        }

        Button selectImage = findViewById(R.id.btnDownloadImageSelectImage);
        Button backToMain = findViewById(R.id.btnDownloadImageToMain);

        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DownloadImageActivityGoogleFullCode.this, ListImagesActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Button downloadImage = findViewById(R.id.btnDownloadImageDownloadImage);
        downloadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "download image FileReference: " + selectedImageFileReference);
            }
        });

        backToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DownloadImageActivityGoogleFullCode.this, MainActivity.class);
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
                showProgressBar("send");
                /*
                // todo get the real uids, remove these line
                if (authUserId.equals("VgNGhMth85Y0Szg6FxLMcWkEpmA3")) {
                    receiveUserId = "0QCS5u2UnxYURlbntvVTA6ZTbaO2";
                } else {
                    authUserId = "0QCS5u2UnxYURlbntvVTA6ZTbaO2";
                    receiveUserId = "VgNGhMth85Y0Szg6FxLMcWkEpmA3";
                }
                */
                // get the roomId by comparing 2 UID strings
                String roomId = getRoomId(authUserId, receiveUserId);
                String messageString = edtMessage.getText().toString();
                edtRoomId.setText(roomId);
                Log.i(TAG, "message: " + messageString);
                Log.i(TAG, "roomId: " + roomId);
                // now we are going to send data to the database
                long actualTime = new Date().getTime();
                /*
                retrieve the time string in GMT
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String millisInString  = dateFormat.format(new Date());
                 */
                MessageModel messageModel = new MessageModel(authUserId, messageString, actualTime, false);
                mDatabaseReference.child("messages").child(roomId).push().setValue(messageModel);
                // without push there is no new chatId key
                // mDatabaseReference.child("messages").child(roomId).setValue(messageModel);
                Toast.makeText(getApplicationContext(),
                        "message written to database",
                        Toast.LENGTH_SHORT).show();
                edtMessage.setText("");
            }
        });

        Button send = findViewById(R.id.btnDownloadImageSend);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // todo get the real uids, remove the line
                receiveUserId = "0QCS5u2UnxYURlbntvVTA6ZTbaO2";
                // get the roomId by comparing 2 UID strings
                String roomId = getRoomId(authUserId, receiveUserId);
                String messageString = edtMessage.getText().toString();
                edtRoomId.setText(roomId);
                Log.i(TAG, "message: " + messageString);
                Log.i(TAG, "roomId: " + roomId);
                // now we are going to send data to the database
                long actualTime = new Date().getTime();
                /*
                retrieve the time string in GMT
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String millisInString  = dateFormat.format(new Date());
                 */
                MessageModel messageModel = new MessageModel(authUserId, messageString, actualTime, false);
                mDatabaseReference.child("messages").child(roomId).push().setValue(messageModel);
                // without push there is no new chatId key
                // mDatabaseReference.child("messages").child(roomId).setValue(messageModel);
                Toast.makeText(getApplicationContext(),
                        "message written to database",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void beginDownload() {
        Log.i(TAG, "begin download for URI: " + mFileName);
        // Get path
        //String path = "photos/" + mFileUri.getLastPathSegment();
        String path = "photos/" + mFileName;

        // Kick off MyDownloadService to download the file
        Intent intent = new Intent(this, MyDownloadService.class)
                .putExtra(MyDownloadService.EXTRA_DOWNLOAD_PATH, path)
                .setAction(MyDownloadService.ACTION_DOWNLOAD);
        startService(intent);

        // Show loading spinner
        showProgressBar(getString(R.string.progress_downloading));
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

    }

    @Override
    public void onStart() {
        super.onStart();
        updateUI(mAuth.getCurrentUser());

        // Register receiver for uploads and downloads
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.registerReceiver(mBroadcastReceiver, MyDownloadService.getIntentFilter());
        manager.registerReceiver(mBroadcastReceiver, MyUploadService.getIntentFilter());
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
    }

    private void showMessageDialog(String title, String message) {
        AlertDialog ad = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .create();
        ad.show();
    }


    /**
     * service methods
     */

    // compare two strings and build a new string: if a < b: ab if a > b: ba, if a = b: ab
    private String getRoomId(String a, String b) {
        int compare = a.compareTo(b);
        if (compare > 0) return b + a;
        else return a + b;
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
        hideProgressBar();
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

    private void showProgressBar(String caption) {
        //binding.caption.setText(caption);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        //binding.caption.setText("");
        progressBar.setVisibility(View.INVISIBLE);
    }

}