package de.androidcrypto.firebaseplayground;

import com.google.auth.oauth2.GoogleCredentials;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.Scanner;


import de.androidcrypto.firebaseplayground.models.UserModel;

public class SendNotificationActivity extends AppCompatActivity {

    com.google.android.material.textfield.TextInputEditText signedInUser, deviceToken;
    com.google.android.material.textfield.TextInputEditText edtNotification, edtRoomId, userPhotoUrl, userPublicKey, userName;
    com.google.android.material.textfield.TextInputLayout edtNotificationLayout;
    com.google.android.material.textfield.TextInputEditText edtNotificationGoogle;
    com.google.android.material.textfield.TextInputLayout edtNotificationGoogleLayout;
    TextView warningNoData;

    static final String TAG = "SendNotification";
    // get the data from auth
    private static String authUserId = "", authUserEmail, authDisplayName, authPhotoUrl;
    private static String receiveUserId = "", receiveUserEmail = "", receiveUserDisplayName = "";

    String CHANNEL_ID = "123";

    private DatabaseReference mDatabaseReference;
    private FirebaseAuth mAuth;
    ProgressBar progressBar;

    private static final String PROJECT_ID = "fir-playground-1856e";
    private static final String BASE_URL = "https://fcm.googleapis.com";
    private static final String FCM_SEND_ENDPOINT = "/v1/projects/" + PROJECT_ID + "/messages:send";
    private static final String MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
    private static final String[] SCOPES = { MESSAGING_SCOPE };
    private static final String TITLE = "FCM Notification";
    private static final String BODY = "Notification from FCM";
    public static final String MESSAGE_KEY = "message";

    private GoogleCredentials googleCredentials;
    private InputStream jasonfile;
    private String beaerertoken;
    private String BEARERTOKEN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_notification);

        signedInUser = findViewById(R.id.etSendNotificationSignedInUser);
        deviceToken = findViewById(R.id.etSendNotificationDeviceToken);
        progressBar = findViewById(R.id.pbSendNotification);

        edtNotificationLayout = findViewById(R.id.etSendNotificationNotificationLayout);
        edtNotification = findViewById(R.id.etSendNotificationNotification);
        edtNotificationGoogleLayout = findViewById(R.id.etSendNotificationNotificationGoogleLayout);
        edtNotificationGoogle = findViewById(R.id.etSendNotificationNotificationGoogle);
        edtRoomId = findViewById(R.id.etSendNotificationRoomId);
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
/*
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
*/
        //       Button loadData = findViewById(R.id.btnDatabaseUserLoad);
        //       Button savaData = findViewById(R.id.btnDatabaseUserSave);





        //Button selectRecipient = findViewById(R.id.btnSendNotificationSelectRecipient);
        Button backToMain = findViewById(R.id.btnSendNotificationToMain);
        backToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SendNotificationActivity.this, MainActivity.class);
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

        /*
        // this is for sending a notification to this device
        edtNotificationLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgressBar();
                // todo get the real uids, remove these line
                if (authUserId.equals("VgNGhMth85Y0Szg6FxLMcWkEpmA3")) {
                    receiveUserId = "0QCS5u2UnxYURlbntvVTA6ZTbaO2";
                } else {
                    authUserId = "0QCS5u2UnxYURlbntvVTA6ZTbaO2";
                    receiveUserId = "VgNGhMth85Y0Szg6FxLMcWkEpmA3";
                }

                // new code following https://developer.android.com/develop/ui/views/notifications/build-notification

                // don't forget that in MainActivity onCreate the createNotificationChannel is called

                String textContent = "N: " + edtNotification.getText().toString();
                String textTitle = "Test";
                //String textContent = "Notification content";
                NotificationCompat.Builder builder = new NotificationCompat.Builder(view.getContext(), CHANNEL_ID)
                        //.setSmallIcon(R.drawable.notification_icon)
                        .setSmallIcon(R.drawable.icon_for_splash)
                        .setContentTitle(textTitle)
                        .setContentText(textContent)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(view.getContext());
                // notificationId is a unique int for each notification that you must define
                int notificationId = 124;
                notificationManager.notify(notificationId, builder.build());
            }
        });
*/

        Button getDeviceToken = findViewById(R.id.btnSendNotificationGetDeviceToken);
        getDeviceToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseMessaging.getInstance().getToken()
                        .addOnCompleteListener(new OnCompleteListener<String>() {
                            @Override
                            public void onComplete(@NonNull Task<String> task) {
                                if (!task.isSuccessful()) {
                                    Log.i(TAG, "Fetching FCM registration token failed", task.getException());
                                    return;
                                }

                                // Get new FCM registration token
                                String deviceTokenString = task.getResult();
                                String currentUserID = mAuth.getCurrentUser().getUid();
                                //mDatabaseReference.child("userstoken").child(currentUserID).setValue("");
                                mDatabaseReference.child("userstoken").child(currentUserID).child("device_token").setValue(deviceTokenString)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Log.i(TAG, "device token retrieved and saved in database");

                                            }
                                        });

                                // Log and toast
                                String msg = "token: " + deviceTokenString;
                                Log.i(TAG, msg);
                                deviceToken.setText(deviceTokenString);
                                //Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

/* static device token
michael.telefon:
fIwQKKS6Seyqv8RGyvlBoD:APA91bG2c8AlJHLTOd-s-dNVARx1gwb-axnrSnCmVHRlzu5HBkYRoesxm3C48m7hVDztLUFfiDZbFh58eyhEM2TL9qU8dqH55uKbg1bgyC0lOOabo6eVbhK18a8PaDROvQ4PKjPqYZCb
klaus:
ddEQFQCsT5y-2NHtsQlBZ4:APA91bH7wU9Cd_7XiyVziWV0nZkGN0Rf1P-wkkgAxhIzpk6Ivo45zFgykEf7t7_2pJ8zPHKEf0jUOGwtqaVIR8gMvTGrI4YznnLAWgBXUda83aMGHdlkEi_CpA1y1c5iBJpXHYQWsl36
 */

        edtNotificationLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "send notification");
                String textContent = "N: " + edtNotification.getText().toString();
                // https://firebase.google.com/docs/cloud-messaging/auth-server
                // https://github.com/firebase/quickstart-java/blob/f75816cd181cdaf49401db3b3b52e4f20f629470/messaging/src/main/java/com/google/firebase/quickstart/Messaging.java#L62-L66

                // https://stackoverflow.com/questions/73539295/service-account-json-open-failed-enoent-no-such-file-or-directory

                //sendCommonMessage(textContent);

                String receiverToken = "ddEQFQCsT5y-2NHtsQlBZ4:APA91bH7wU9Cd_7XiyVziWV0nZkGN0Rf1P-wkkgAxhIzpk6Ivo45zFgykEf7t7_2pJ8zPHKEf0jUOGwtqaVIR8gMvTGrI4YznnLAWgBXUda83aMGHdlkEi_CpA1y1c5iBJpXHYQWsl36";
                String senderName = "Playground";
                String msg = edtNotification.getText().toString(); // todo null check
                FcmNotificationsSender fcmNotificationsSender = new FcmNotificationsSender(receiverToken, senderName
                        , msg, getApplicationContext(), SendNotificationActivity.this);
                fcmNotificationsSender.SendNotifications();

            }
        });

        edtNotificationGoogleLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "send notification (Google)");
                String textContent = "N: " + edtNotification.getText().toString();
                // https://firebase.google.com/docs/cloud-messaging/auth-server
                // https://github.com/firebase/quickstart-java/blob/f75816cd181cdaf49401db3b3b52e4f20f629470/messaging/src/main/java/com/google/firebase/quickstart/Messaging.java#L62-L66

                // https://stackoverflow.com/questions/73539295/service-account-json-open-failed-enoent-no-such-file-or-directory

                //sendCommonMessage(textContent);

                String receiverToken = "ddEQFQCsT5y-2NHtsQlBZ4:APA91bH7wU9Cd_7XiyVziWV0nZkGN0Rf1P-wkkgAxhIzpk6Ivo45zFgykEf7t7_2pJ8zPHKEf0jUOGwtqaVIR8gMvTGrI4YznnLAWgBXUda83aMGHdlkEi_CpA1y1c5iBJpXHYQWsl36";
                String senderName = "Playground G";
                String msg = edtNotification.getText().toString(); // todo null check
                FcmNotificationsSenderGoogle fcmNotificationsSenderGoogle = new FcmNotificationsSenderGoogle(receiverToken, senderName
                        , msg, getApplicationContext(), SendNotificationActivity.this);
                //fcmNotificationsSenderGoogle.SendNotifications();
                JsonObject notificationMessage = FcmNotificationsSenderGoogle.buildNotificationMessage("Title1", msg);
                Log.i(TAG, "notificationMessage: " + notificationMessage.toString());
                prettyPrint(notificationMessage);
                String accessToken = "";
                try {
                    accessToken = getAccessToken();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.i(TAG, "getAccessToken: " + accessToken);

                // new, result is in BEARERTOKEN
                try {
                    getAccessTokenNew();
                    Log.i(TAG, "getAccessTokenNew: " + BEARERTOKEN);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    getAccessTokenNewFull(receiverToken, "Title1", msg);
                    Log.i(TAG, "getAccessTokenNewFull: " + BEARERTOKEN);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // now let's send
                /*
                try {
                    FcmNotificationsSenderGoogle.sendMessage(notificationMessage, BEARERTOKEN);
                } catch (IOException e) {
                    e.printStackTrace();
                }
*/
            }
        });

    }

    /**
     * Retrieve a valid access token that can be use to authorize requests to the FCM REST
     * API.
     *
     * @return Access token.
     * @throws IOException
     */
    // [START retrieve_access_token]
    private static String getAccessToken() throws IOException {
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new FileInputStream("service-account.json"))
                .createScoped(Arrays.asList(SCOPES));
        googleCredentials.refreshAccessToken();
        return googleCredentials.getAccessToken().getTokenValue();
    }

    //String userFcmToken, String title, String body
    private void getAccessTokenNewFull(String userFcmToken, String title, String body) throws IOException {
        jasonfile = getResources().openRawResource(getApplicationContext().getResources().getIdentifier("serviceaccount","raw",getPackageName()));
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    googleCredentials = GoogleCredentials
                            .fromStream(jasonfile)
                            .createScoped(Arrays.asList(SCOPES));
                    //googleCredentials.refreshAccessToken().getTokenValue();
                    beaerertoken = googleCredentials.refreshAccessToken().getTokenValue();
                    Log.i(TAG, "beaerertoken:  " + beaerertoken);
                    BEARERTOKEN = beaerertoken;
                    JsonObject notificationMessage = FcmNotificationsSenderGoogle.buildNotificationMessage(title, body);
                    sendMessage(notificationMessage, beaerertoken);
                } catch (IOException e) {
                    Log.d(TAG, "In error statement");
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void getAccessTokenNew() throws IOException {
        jasonfile = getResources().openRawResource(getApplicationContext().getResources().getIdentifier("serviceaccount","raw",getPackageName()));
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    googleCredentials = GoogleCredentials
                            .fromStream(jasonfile)
                            .createScoped(Arrays.asList(SCOPES));
                    //googleCredentials.refreshAccessToken().getTokenValue();
                    beaerertoken = googleCredentials.refreshAccessToken().getTokenValue();
                    Log.i(TAG, "beaerertoken:  " + beaerertoken);
                    BEARERTOKEN = beaerertoken;
                } catch (IOException e) {
                    Log.d(TAG, "In error statement");
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Create HttpURLConnection that can be used for both retrieving and publishing.
     *
     * @return Base HttpURLConnection.
     * @throws IOException
     */
    public static HttpURLConnection getConnection(String bearerToken) throws IOException {
        Log.i(TAG, "start get connection with bearerToken: " + bearerToken);
        URL url = new URL(BASE_URL + FCM_SEND_ENDPOINT);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestProperty("Authorization", "Bearer " + bearerToken);
        httpURLConnection.setRequestProperty("Content-Type", "application/json; UTF-8");
        return httpURLConnection;
    }

    // is called from MainActivity on create
    /*
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
*/

    /**
     * Retrieve a valid access token that can be use to authorize requests to the FCM REST
     * API.
     *
     * @return Access token.
     * @throws IOException
     */
    private static String getAccessTokenOrg() throws IOException {
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new FileInputStream("service-account.json"))
                .createScoped(Arrays.asList(SCOPES));
        googleCredentials.refreshAccessToken();
        return googleCredentials.getAccessToken().getTokenValue();
    }
/*
    public static String getAccessToken() throws IOException {
        InputStream fis = getApplicationContext().getResources().openRawResource(getApplicationContext().getResources().getIdentifier("serviceaccount","raw",getPackageName()));
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(fis)
                .createScoped(Arrays.asList(SCOPES));
        googleCredentials.refreshAccessToken();
        return googleCredentials.refreshAccessToken().getTokenValue();
    }
*/



    /**
     * Send request to FCM message using HTTP.
     * Encoded with UTF-8 and support special characters.
     *
     * @param fcmMessage Body of the HTTP request.
     * @throws IOException
     */

    /**
     * Send request to FCM message using HTTP.
     * Encoded with UTF-8 and support special characters.
     *
     * @param fcmMessage Body of the HTTP request.
     * @throws IOException
     */
    public static void sendMessage(JsonObject fcmMessage, String bearerToken) throws IOException {
        Log.i(TAG, "start sendMessage");
        HttpURLConnection connection = getConnection(bearerToken);
        Log.i(TAG, "sendMessage connection: " + connection.toString());
        connection.setDoOutput(true);
        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
        writer.write(fcmMessage.toString());
        writer.flush();
        writer.close();
        int responseCode = connection.getResponseCode();
        Log.i(TAG, "sendMessage responseCode: " + responseCode);
        if (responseCode == 200) {
            String response = inputstreamToString(connection.getInputStream());
            System.out.println("Message sent to Firebase for delivery, response:");
            System.out.println(response);
        } else {
            System.out.println("Unable to send message to Firebase:");
            String response = inputstreamToString(connection.getErrorStream());
            System.out.println(response);
        }
    }

    /**
     * Read contents of InputStream into String.
     *
     * @param inputStream InputStream to read.
     * @return String containing contents of InputStream.
     * @throws IOException
     */
    private static String inputstreamToString(InputStream inputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        Scanner scanner = new Scanner(inputStream);
        while (scanner.hasNext()) {
            stringBuilder.append(scanner.nextLine());
        }
        return stringBuilder.toString();
    }

    /**
     * Pretty print a JsonObject.
     *
     * @param jsonObject JsonObject to pretty print.
     */
    private static void prettyPrint(JsonObject jsonObject) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(gson.toJson(jsonObject) + "\n");
    }

    /**
     * Send notification message to FCM for delivery to registered devices.
     *
     * @throws IOException
     */
    public static void sendCommonMessage() throws IOException {
        JsonObject notificationMessage = buildNotificationMessage();
        System.out.println("FCM request body for message using common notification object:");
        prettyPrint(notificationMessage);
        //sendMessage(notificationMessage);
    }

    /**
     * Construct the body of a notification message request.
     *
     * @return JSON of notification message.
     */
    private static JsonObject buildNotificationMessage() {
        JsonObject jNotification = new JsonObject();
        jNotification.addProperty("title", TITLE);
        jNotification.addProperty("body", BODY);

        JsonObject jMessage = new JsonObject();
        jMessage.add("notification", jNotification);
        jMessage.addProperty("topic", "news");

        JsonObject jFcm = new JsonObject();
        jFcm.add(MESSAGE_KEY, jMessage);

        return jFcm;
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

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
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