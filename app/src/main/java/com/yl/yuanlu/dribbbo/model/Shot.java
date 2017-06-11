package com.yl.yuanlu.dribbbo.model;

import java.util.Map;

/**
 * Created by LUYUAN on 4/6/2017.
 */

public class Shot {

    public static final String IMAGE_NORMAL = "normal";
    public static final String IMAGE_HIDPI = "hidpi";

    public String id;
    public String title;
    public String description;
    public String html_url;

    public Map<String, String> images;
    public boolean animated;

    public int views_count;
    public int likes_count;
    public int buckets_count;

    public User user;

    public Boolean liked;   //not directly from API, set by ourself
    public Boolean bucketed;

    public String getImageURL() {
        if(images==null) return null;
        else if(animated) return images.get(IMAGE_NORMAL);
        else if(images.containsKey(IMAGE_HIDPI)) return images.get(IMAGE_HIDPI);
        else return images.get(IMAGE_NORMAL);
    }

    public String getUserAvatarURL() {
        if(user==null) return null;
        else return user.avatar_url;
    }

}
