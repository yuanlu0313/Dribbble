package com.yl.yuanlu.dribbbo.dribbble;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.yl.yuanlu.dribbbo.model.Bucket;
import com.yl.yuanlu.dribbbo.model.Like;
import com.yl.yuanlu.dribbbo.model.Shot;
import com.yl.yuanlu.dribbbo.model.User;
import com.yl.yuanlu.dribbbo.utils.ModelUtils;

import org.json.JSONException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by LUYUAN on 4/18/2017.
 */

public class Dribbble {

    private static final String API_URL = "https://api.dribbble.com/v1/";

    public static final int COUNT_PER_LOAD = 12;

    private static final String USER_END_POINT = API_URL + "user";
    private static final String USERS_END_POINT = API_URL + "users";
    private static final String SHOTS_END_POINT = API_URL + "shots";
    private static final String BUCKETS_END_POINT = API_URL + "buckets";

    private static final String SP_AUTH = "auth";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_USER = "user";
    private static final String KEY_NAME = "name";
    private static final String KEY_DESCRIPTION = "description";

    private static OkHttpClient client = new OkHttpClient();
    private static String accessToken;
    private static User user;



    public static User getCurrentUser() {
        return user;
    }

    //build the Auth header into a request
    private static Request.Builder buildAuthRequest(String url) {
        return new Request.Builder()
                .addHeader("Authorization", "Bearer " + accessToken)
                .url(url);
    }

    //send the prepared request to API and catch the response from API
    private static Response sendRequest(Request request) {
        try {
            return client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //to build a Get request
    public static Response makeGetRequest (String url) throws DribbbleException {
        Request request = buildAuthRequest(url).build();
        return sendRequest(request);
    }

    //to build a POST request, need to pass in RequestBody also
    public static Response makePostRequest (String url, RequestBody requestBody) throws DribbbleException {
        Request request = buildAuthRequest(url).post(requestBody).build();
        return sendRequest(request);
    }

    //to build a PUT request
    public static Response makePutRequest (String url, RequestBody requestBody) throws DribbbleException {
        Request request = buildAuthRequest(url).put(requestBody).build();
        return sendRequest(request);
    }

    //to build a DELETE request, this one used by unLike, so no RequestBody passed in
    public static Response makeDeleteRequest(String url, RequestBody requestBody) throws DribbbleException {
        Request request = buildAuthRequest(url).delete(requestBody).build();
        return sendRequest(request);
    }

    //handle the received response from API (in form of JSon String)
    //translate it to corresponding object
    private static <T> T parseResponse(Response response, TypeToken<T> typeToken) throws DribbbleException {
        String responseString;
        try {
            responseString = response.body().string();
        } catch (IOException e) {
            throw new DribbbleException(e.getMessage());
        }
        try {
            return ModelUtils.toObject(responseString, typeToken);
        }
        catch (JsonSyntaxException e) {
            throw new DribbbleException(e.getMessage());
        }
    }

    //Load real token from sp if present
    public static void init(Context context) {
        accessToken = ModelUtils.load_from_sp(SP_AUTH, context, KEY_ACCESS_TOKEN);
        if(accessToken != null) {
            Log.i("Yuan DBG : Load token :", accessToken);
            String user_jsonString = ModelUtils.load_from_sp(SP_AUTH, context, KEY_USER);
            user = ModelUtils.toObject(user_jsonString, new TypeToken<User>(){});
        }
    }

    //save real token to sp, get User info with real token and also save to sp
    //save user info for convenience because user info always need to be showed in app and not changing frequently
    //called after initially get real token from temp token
    public static void login(@NonNull String accessToken, Context context) throws DribbbleException {
        Dribbble.accessToken = accessToken;  //Note cannot directly "accessToken = accessToken", refer to static function knowledge
        ModelUtils.save_to_sp(SP_AUTH, context, accessToken, KEY_ACCESS_TOKEN);

        Dribbble.user = getUser();
        ModelUtils.save_to_sp(SP_AUTH, context, ModelUtils.toString(user, new TypeToken<User>(){}), KEY_USER);
    }

    public static boolean isLoggedIn() {
        return accessToken != null;
    }

    //Log out, delete saved token
    public static void logout(Context context) {
        ModelUtils.delete_from_sp(SP_AUTH, context, KEY_ACCESS_TOKEN);
        ModelUtils.delete_from_sp(SP_AUTH, context, KEY_USER);
        accessToken = null;
        user = null;
    }

    //get the current logged in User information from API
    private static User getUser() throws DribbbleException {
        Response response = makeGetRequest(USER_END_POINT);
        if(response.code()!=HttpURLConnection.HTTP_OK) throw new DribbbleException(response.message());
        return parseResponse(response, new TypeToken<User>(){});
    }

    //get the shot list from API (with page parameter)
    public static List<Shot> getShotList(int page) throws DribbbleException {
        String url = SHOTS_END_POINT + "?page=" + page;
        Response response = makeGetRequest(url);
        if(response.code()!=HttpURLConnection.HTTP_OK) throw new DribbbleException(response.message());
        return parseResponse(response, new TypeToken<List<Shot>>(){});
    }

    //check whether current user liked a shot (referred by shot id)
    public static Boolean isLiked(@NonNull String id) throws DribbbleException {
        String url = SHOTS_END_POINT + "/" + id + "/like";
        Response response = makeGetRequest(url);
        switch(response.code()) {
            case HttpURLConnection.HTTP_OK :
                response.body().close();
                return true;
            case HttpURLConnection.HTTP_NOT_FOUND :
                response.body().close();
                return false;
            default:
                response.body().close();
                throw new DribbbleException(response.message());
        }
    }

    //like a shot based on shotID
    public static void likeShot(@NonNull String id) throws DribbbleException {
        String url = SHOTS_END_POINT + "/" + id + "/like";
        RequestBody postBody = new FormBody.Builder().build();
        Response response = makePostRequest(url, postBody);
        if(response.code()!=HttpURLConnection.HTTP_CREATED) {
            throw new DribbbleException(response.message());
        }
        response.body().close();
    }

    //unlike a shot based on shotID
    public static void unlikeShot(@NonNull String id) throws DribbbleException {
        String url = SHOTS_END_POINT + "/" + id + "/like";
        RequestBody deleteBody = new FormBody.Builder().build();
        Response response = makeDeleteRequest(url, deleteBody);
        if(response.code()!=HttpURLConnection.HTTP_NO_CONTENT) {
            throw new DribbbleException(response.message());
        }
        response.body().close();
    }

    //get the liked shot list from API (with page parameter)
    public static List<Shot> getLikedShotList(@NonNull int page) throws DribbbleException {
        String url = USER_END_POINT + "/likes" + "?page=" + page;
        Response response = makeGetRequest(url);
        if(response.code()!=HttpURLConnection.HTTP_OK) throw new DribbbleException(response.message());
        List<Like> likes = parseResponse(response, new TypeToken<List<Like>>(){});
        List<Shot> shots = new ArrayList<Shot>();
        for(Like like : likes) {
            shots.add(like.shot);
        }
        return shots;
    }

    //get the Bucket list from API
    //load all user buckets, used to check which buckets a shot is added to
    public static List<Bucket> getBucketList() throws DribbbleException {
        String url = USER_END_POINT + "/" + "buckets?per_page=" + Integer.MAX_VALUE;
        Response response = makeGetRequest(url);
        if(response.code()!=HttpURLConnection.HTTP_OK) throw new DribbbleException(response.message());
        List<Bucket> buckets = parseResponse(response, new TypeToken<List<Bucket>>(){});
        return buckets;
    }
    //load by page, used in infinite loading bucket list
    public static List<Bucket> getBucketList(@NonNull int page) throws DribbbleException {
        String url = USER_END_POINT + "/buckets" + "?page=" + page;
        Response response = makeGetRequest(url);
        if(response.code()!=HttpURLConnection.HTTP_OK) throw new DribbbleException(response.message());
        List<Bucket> buckets = parseResponse(response, new TypeToken<List<Bucket>>(){});
        return buckets;
    }

    //load all buckets that contains a shot, not only current user
    public static List<Bucket> getShotBucketList(@NonNull String shotID) throws DribbbleException {
        String url = SHOTS_END_POINT + "/" + shotID + "/buckets?per_page=" + Integer.MAX_VALUE;
        Response response = makeGetRequest(url);
        if(response.code()!=HttpURLConnection.HTTP_OK) throw new DribbbleException(response.message());
        List<Bucket> buckets = parseResponse(response, new TypeToken<List<Bucket>>(){});
        return buckets;
    }


    //create a new bucket, note that the response will contain the bucket created
    public static Bucket createBucket(@NonNull String name, @NonNull String description) throws DribbbleException {
        String url = BUCKETS_END_POINT;
        RequestBody postBody = new FormBody.Builder()
                .add(KEY_NAME, name)
                .add(KEY_DESCRIPTION, description)
                .build();
        Response response = makePostRequest(url, postBody);
        if(response.code()!=HttpURLConnection.HTTP_CREATED) throw new DribbbleException(response.message());
        Bucket bucket = parseResponse(response, new TypeToken<Bucket>(){});
        return bucket;
    }

    //delete a bucket
    public static void deleteBucket(@NonNull String bucketID) throws DribbbleException {
        String url = BUCKETS_END_POINT + "/" + bucketID;
        RequestBody deleteBody = new FormBody.Builder().build();
        Response response = makeDeleteRequest(url, deleteBody);
        if(response.code()!=HttpURLConnection.HTTP_NO_CONTENT) throw new DribbbleException(response.message());
        response.body().close();
    }

    //return the ShotList contained in current bucket
    public static List<Shot> getBucketShotList(@NonNull String bucketID, @NonNull int page) throws DribbbleException {
        String url = BUCKETS_END_POINT + "/" + bucketID + "/shots?page=" + page;
        Response response = makeGetRequest(url);
        if(response.code()!=HttpURLConnection.HTTP_OK) throw new DribbbleException(response.message());
        return parseResponse(response, new TypeToken<List<Shot>>(){});
    }

    //add a shot to a user bucket
    public static void addShotToBucket(@NonNull String bucketID, @NonNull String shotID) throws DribbbleException{
        String url = BUCKETS_END_POINT + "/" + bucketID + "/shots?shot_id=" + shotID;
        RequestBody putBody = new FormBody.Builder().build();
        Response response = makePutRequest(url, putBody);
        if(response.code()!=HttpURLConnection.HTTP_NO_CONTENT) throw new DribbbleException(response.message());
        response.body().close();
    }

    //remove a shot from a user bucket
    public static void removeShotFromBucket(@NonNull String bucketID, @NonNull String shotID) throws DribbbleException {
        String url = BUCKETS_END_POINT + "/" + bucketID + "/shots?shot_id=" + shotID;
        RequestBody deleteBody = new FormBody.Builder().build();
        Response response = makeDeleteRequest(url, deleteBody);
        if(response.code()!=HttpURLConnection.HTTP_NO_CONTENT) throw new DribbbleException(response.message());
        response.body().close();
    }
}
