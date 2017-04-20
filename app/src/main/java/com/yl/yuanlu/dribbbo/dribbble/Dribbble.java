package com.yl.yuanlu.dribbbo.dribbble;

import android.content.Context;

import com.google.gson.reflect.TypeToken;
import com.yl.yuanlu.dribbbo.model.User;
import com.yl.yuanlu.dribbbo.utils.ModelUtils;

/**
 * Created by LUYUAN on 4/18/2017.
 */

public class Dribbble {

    private static String accessToken;
    private static User user;

    private static final String SP_AUTH = "auth";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_USER = "user";


    public static void init(Context context) {
        accessToken = ModelUtils.load_from_sp(SP_AUTH, context, KEY_ACCESS_TOKEN);
        if(accessToken != null) {
            String user_jsonString = ModelUtils.load_from_sp(SP_AUTH, context, KEY_USER);
            user = ModelUtils.toObject(user_jsonString, new TypeToken<User>(){});
        }
    }

    public static boolean isLoggedIn() {
        return accessToken != null;
    }

}
