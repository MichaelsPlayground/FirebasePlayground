package de.androidcrypto.firebaseplayground;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class FcmNotificationsSenderGoogle {

    /**
     * status: works
     * Original source: https://github.com/Raj-m01/Android-Chat-App
     * This code needs this library to work:
     * implementation 'com.android.volley:volley:1.2.1'
     * It is using the legacy Cloud Messaging API
     */

    // https://github.com/firebase/quickstart-java/blob/f75816cd181cdaf49401db3b3b52e4f20f629470/messaging/src/main/java/com/google/firebase/quickstart/Messaging.java

    private static final String TAG = "FcmNotific.SenderGoogle";
    private final String postUrl = "https://fcm.googleapis.com/fcm/send";
    private final String fcmServerKey = "AAAA7WlMaYI:APA91bEvt_DoL1XRSAJIThgSLFjD-sSIueSRqGGfXpgfYJ-gTDLp_38BFKnbssdTEMh728q8Pe0lkBiueHhoWmSRLft-kJzOUuK12ug_AJq1gD_bRdFQkGiFKgrCB3RhKpSoD2Fe2Kf0";

    // new API:
    private static final String PROJECT_ID = "fir-playground-1856e";
    private static final String BASE_URL = "https://fcm.googleapis.com";
    private static final String FCM_SEND_ENDPOINT = "/v1/projects/" + PROJECT_ID + "/messages:send";

    private static final String MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
    private static final String[] SCOPES = { MESSAGING_SCOPE };
    private static final String TITLE = "FCM Notification";
    private static final String BODY = "Notification from FCM";
    public static final String MESSAGE_KEY = "message";

    String userFcmToken;
    String title;
    String body;
    Context mContext;
    Activity mActivity;
    private RequestQueue requestQueue;

    public FcmNotificationsSenderGoogle(String userFcmToken, String title, String body, Context mContext, Activity mActivity) {
        this.userFcmToken = userFcmToken;
        this.title = title;
        this.body = body;
        this.mContext = mContext;
        this.mActivity = mActivity;
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

    public static HttpURLConnection getConnectionOrg() throws IOException {
        URL url = new URL(BASE_URL + FCM_SEND_ENDPOINT);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestProperty("Authorization", "Bearer " + getAccessToken());
        httpURLConnection.setRequestProperty("Content-Type", "application/json; UTF-8");
        return httpURLConnection;
    }

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

    public static void sendMessageOrg(JsonObject fcmMessage) throws IOException {
        HttpURLConnection connection = getConnectionOrg();
        connection.setDoOutput(true);
        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
        writer.write(fcmMessage.toString());
        writer.flush();
        writer.close();

        int responseCode = connection.getResponseCode();
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
     * Send notification message to FCM for delivery to registered devices.
     *
     * @throws IOException
     */
    public static void sendCommonMessage() throws IOException {
        JsonObject notificationMessage = buildNotificationMessage();
        System.out.println("FCM request body for message using common notification object:");
        prettyPrint(notificationMessage);
        sendMessageOrg(notificationMessage);
    }

    // String userFcmToken, String title, String body
    /**
     * Construct the body of a notification message request.
     *
     * @return JSON of notification message.
     */
    public static JsonObject buildNotificationMessage(String title, String body) {
        JsonObject jNotification = new JsonObject();
        jNotification.addProperty("title", title);
        jNotification.addProperty("body", body);

        JsonObject jMessage = new JsonObject();
        jMessage.add("notification", jNotification);
        jMessage.addProperty("topic", "news");

        JsonObject jFcm = new JsonObject();
        jFcm.add("android_channel_id", jMessage);

        return jFcm;
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

    public void SendNotifications() {

        // this is using the original code and libraries:
        requestQueue = Volley.newRequestQueue(mActivity);
        JSONObject mainObj = new JSONObject();
        try {
            mainObj.put("to", userFcmToken);
            JSONObject notiObject = new JSONObject();
            notiObject.put("title", title);
            notiObject.put("body", body);
            notiObject.put("icon", "icon_for_splash");
            notiObject.put("sound", "littlebell14606.wav");
            notiObject.put("android_channel_id","1");
            mainObj.put("notification", notiObject);

            Log.i(TAG, "mainObj: " + mainObj.toString(2));
            Log.i(TAG, "notiObject: " + notiObject.toString(2));

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, postUrl, mainObj, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                    // code run is got response
                    Log.i(TAG, "JsonObjectRequest onResponse");
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // code run is got error
                    Log.i(TAG, "JsonObjectRequest onErrorResponse");
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {

                    Map<String, String> header = new HashMap<>();
                    header.put("content-type", "application/json");
                    header.put("authorization", "key=" + fcmServerKey);
                    Log.i(TAG, "Map: " + header.toString());
                    return header;
                }
            };
            requestQueue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
