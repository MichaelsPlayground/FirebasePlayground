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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FcmNotificationsSenderOrg {

    /**
     * status: works
     * Original source: https://github.com/Raj-m01/Android-Chat-App
     * This code needs this library to work:
     * implementation 'com.android.volley:volley:1.2.1'
     * It is using the legacy Cloud Messaging API
     */

    private static final String TAG = "FcmNotificationSender";
    private final String postUrl = "https://fcm.googleapis.com/fcm/send";
    private final String fcmServerKey = "AAAA7WlMaYI:APA91bEvt_DoL1XRSAJIThgSLFjD-sSIueSRqGGfXpgfYJ-gTDLp_38BFKnbssdTEMh728q8Pe0lkBiueHhoWmSRLft-kJzOUuK12ug_AJq1gD_bRdFQkGiFKgrCB3RhKpSoD2Fe2Kf0";
    String userFcmToken;
    String title;
    String body;
    Context mContext;
    Activity mActivity;
    private RequestQueue requestQueue;

    public FcmNotificationsSenderOrg(String userFcmToken, String title, String body, Context mContext, Activity mActivity) {
        this.userFcmToken = userFcmToken;
        this.title = title;
        this.body = body;
        this.mContext = mContext;
        this.mActivity = mActivity;
    }

    public void SendNotifications() {

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
