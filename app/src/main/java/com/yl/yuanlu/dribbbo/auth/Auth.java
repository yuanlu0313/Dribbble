package com.yl.yuanlu.dribbbo.auth;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by LUYUAN on 4/12/2017.
 */

public class Auth {

    public static final int REQ_CODE = 100;

    private static final String URI_AUTHORIZE = "https://dribbble.com/oauth/authorize";
    private static final String URI_ACCESS_TOKEN = "https://dribbble.com/oauth/token";
    private static final String KEY_CLIENT_ID = "client_id";
    private static final String CLIENT_ID = "9c3946a6d0d19c03008a378d7773572daf4350913553a6e757bad7d4dd4e2aa2";
    private static final String KEY_CLIENT_SECRET = "client_secret";
    private static final String CLIENT_SECRET = "949616b7b01d32deca867a774ce533614dd72534abe3907a8e0c73b31eb975d6";
    private static final String KEY_REDIRECT_URI = "redirect_uri";
    public static final String REDIRECT_URI = "http://www.dribbbo.com/";
    private static final String KEY_SCOPE = "scope";
    private static final String SCOPE = "public+write";
    private static final String KEY_CODE = "code";
    private static final String KEY_ACCESS_TOKEN = "access_token";



    //start the AuthActivity, the LoginActivity is passed in
    //expect to receive auth code when AuthActivity ends
    public static void openAuthActivity(@NonNull Activity activity) {
        Intent intent = new Intent(activity, AuthActivity.class);
        activity.startActivityForResult(intent, REQ_CODE);
    }

    public static String getAuthURL() {
        String url = URI_AUTHORIZE;
        url += "?" + KEY_CLIENT_ID + "=" + CLIENT_ID;
        url += "&" + KEY_REDIRECT_URI + "=" + REDIRECT_URI;
        url += "&" + KEY_SCOPE + "=" + SCOPE;
        return url;
    }

    //build a POST request to fetch for real token with temporary token
    public static String fetchAccessToken(String tmp_token) throws IOException{
        OkHttpClient client = new OkHttpClient();
        RequestBody postBody = new FormBody.Builder()
                .add(KEY_CLIENT_ID, CLIENT_ID)
                .add(KEY_CLIENT_SECRET, CLIENT_SECRET)
                .add(KEY_CODE, tmp_token)
                .add(KEY_REDIRECT_URI, REDIRECT_URI)
                .build();
        Request request = new Request.Builder()
                .url(URI_ACCESS_TOKEN)
                .post(postBody)
                .build();
        Response response = client.newCall(request).execute();

        String responseString = response.body().string();
        if(responseString!=null) Log.i("Yuan BDG : New token : ", responseString);
        try {
            JSONObject obj = new JSONObject(responseString);
            return obj.getString(KEY_ACCESS_TOKEN);
        }
        catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }



}
