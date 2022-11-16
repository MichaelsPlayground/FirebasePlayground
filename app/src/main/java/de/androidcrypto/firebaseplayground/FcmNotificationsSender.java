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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class FcmNotificationsSender {

    /**
     * status: working
     * Original source: https://github.com/Raj-m01/Android-Chat-App
     * This code needs this library to work:
     * implementation 'com.google.code.gson:gson:2.9.0'
     * It is using the legacy Cloud Messaging API
     */

    private static final String TAG = "FcmNotificationSenderV2";
    private static final String postUrl = "https://fcm.googleapis.com/fcm/send";
    private static final String FCM_SERVER_KEY = "AAAA7WlMaYI:APA91bEvt_DoL1XRSAJIThgSLFjD-sSIueSRqGGfXpgfYJ-gTDLp_38BFKnbssdTEMh728q8Pe0lkBiueHhoWmSRLft-kJzOUuK12ug_AJq1gD_bRdFQkGiFKgrCB3RhKpSoD2Fe2Kf0";

    String userFcmToken;
    String title;
    String body;

    public FcmNotificationsSender(String userFcmToken, String title, String body) {
        this.userFcmToken = userFcmToken;
        this.title = title;
        this.body = body;
    }

    public void sendNotifications() {
        // use Google's GSON
        JsonObject jsonObject = buildNotificationMessage(title, body, userFcmToken);
        try {
            sendMessage(jsonObject, FCM_SERVER_KEY);
            Log.i(TAG, "message send with success");
        } catch (IOException e) {
            Log.e(TAG, "Error while sending message: " + e.toString());
            e.printStackTrace();
        }
    }

    /**
     * Construct the body of a notification message request.
     *
     * @return JSON of notification message.
     */
    private static JsonObject buildNotificationMessage(String title, String body, String receiverToken) {
        JsonObject jNotification = new JsonObject();
        jNotification.addProperty("title", title);
        jNotification.addProperty("body", body);
        //jNotification.addProperty("icon", "icon_for_splash");
        jNotification.addProperty("icon", "ic_notification");
        jNotification.addProperty("android_channel_id", "1");
        jNotification.addProperty("sound", "littlebell14606.mp3");
        //jNotification.addProperty("sound", "default");
        JsonObject jMessage = new JsonObject();
        jMessage.addProperty("to", receiverToken);
        jMessage.add("notification", jNotification);
        return jMessage;
    }

    /**
     * Create HttpURLConnection that can be used for both retrieving and publishing.
     *
     * @return Base HttpURLConnection.
     * @throws IOException
     */
    public static HttpURLConnection getConnection(String fcmServerKey) throws IOException {
        Log.i(TAG, "start get connection with fcmServerKey: " + fcmServerKey);
        URL url = new URL(postUrl);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestProperty("Authorization", "key=" + fcmServerKey);
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
    public static void sendMessage(JsonObject fcmMessage, String fcmServerKey) throws IOException {
        Log.i(TAG, "start sendMessage");
        HttpURLConnection connection = getConnection(fcmServerKey);
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
            Log.i(TAG, "Message sent to Firebase for delivery, response:" + response);
        } else {
            String response = inputstreamToString(connection.getErrorStream());
            Log.i(TAG, "Unable to send message to Firebase response: " + responseCode + " error: " + response);
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
}
