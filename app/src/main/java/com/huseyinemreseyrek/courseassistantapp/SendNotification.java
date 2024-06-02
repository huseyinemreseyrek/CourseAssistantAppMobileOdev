package com.huseyinemreseyrek.courseassistantapp;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.android.volley.toolbox.JsonObjectRequest;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SendNotification {
    private final String userFcmToken;
    private final String title;
    private final String body;
    private final Context context;

    private final String postUrl = "https://fcm.googleapis.com/v1/projects/mobilodev-69e86/messages:send";

    public SendNotification(String userFcmToken, String title, String body, Context context){
        this.userFcmToken = userFcmToken;
        this.title = title;
        this.body = body;
        this.context = context;
    }

    public void SendNotifications(){
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JSONObject mainObj = new JSONObject();
        try{
            JSONObject messageObject = new JSONObject();
            JSONObject notificationObject = new JSONObject();
            notificationObject.put("title",title);
            notificationObject.put("body",body);
            messageObject.put("notification",notificationObject);
            messageObject.put("token",userFcmToken);
            mainObj.put("message",messageObject);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,postUrl,mainObj,response -> {
                System.out.println("Response: " + response);
            },volleyError -> {
                System.out.println("Error: " + volleyError);
            }){
                @NonNull
                @Override
                public Map<String, String> getHeaders() {
                    Accesstoken accesstoken = new Accesstoken();
                    String accesKey = accesstoken.getAccessToken();
                    Map<String, String> header = new HashMap<>();
                    header.put("authorization", "Bearer " + accesKey);
                    header.put("content-type", "application/json");
                    return header;

                }

            };
            requestQueue.add(request);
        }catch (JSONException e){
            Toast.makeText(context,e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }

}
