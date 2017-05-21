package com.yl.yuanlu.dribbbo.view.shot_details;

import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.RequestManager;
import com.yl.yuanlu.dribbbo.R;
import com.yl.yuanlu.dribbbo.model.Shot;

import java.util.zip.Inflater;

/**
 * Created by LUYUAN on 4/10/2017.
 */

public class ShotAdaptor extends RecyclerView.Adapter {

    private final static int IMAGE_VIEW_TYPE = 1;
    private final static int INFO_VIEW_TYPE = 2;
    private final ShotFragment shotFragment;

    Shot shot;
    private RequestManager glide;

    public ShotAdaptor(Shot shot, RequestManager glide, ShotFragment shotFragment) {
        this.glide = glide;
        this.shot = shot;
        this.shotFragment = shotFragment;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType==IMAGE_VIEW_TYPE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.shot_details_image, parent, false);
            return new ShotImageViewHolder(view);
        }
        else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.shot_details_info, parent, false);
            return new ShotInfoViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(position==0) {
            //Load picture here
            ShotImageViewHolder shotImageViewHolder = (ShotImageViewHolder) holder;
            glide.load(shot.getImageURL()).into(shotImageViewHolder.imageView);
        }
        else {
            ShotInfoViewHolder shotInfoViewHolder = (ShotInfoViewHolder) holder;
            shotInfoViewHolder.viewCount.setText(String.valueOf(shot.views_count));
            shotInfoViewHolder.likeCount.setText(String.valueOf(shot.likes_count));
            shotInfoViewHolder.bucketCount.setText(String.valueOf(shot.buckets_count));
            shotInfoViewHolder.title.setText(shot.title);
            shotInfoViewHolder.authorName.setText(shot.user.name);
            glide.load(shot.getUserAvatarURL()).into(shotInfoViewHolder.authorPicture);
            shotInfoViewHolder.description.setText(Html.fromHtml(shot.description==null ? "" : shot.description));  //correctly show description syntax
            shotInfoViewHolder.description.setMovementMethod(LinkMovementMethod.getInstance());  //make all links in description work

            //the like button
            shotInfoViewHolder.actionLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shotFragment.like();
                }
            });

            //bucket button
            shotInfoViewHolder.actionBucket.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shotFragment.bucket();
                }
            });

            //toggle the like button color based on whether liked
            if(!shot.liked) shotInfoViewHolder.actionLike.setImageResource(R.drawable.ic_favorite_border_black_18dp);
            else shotInfoViewHolder.actionLike.setImageResource(R.drawable.ic_favorite_pink_400_18dp);

            if(!shot.bucketed) shotInfoViewHolder.actionBucket.setImageResource(R.drawable.ic_inbox_black_18dp);
            else shotInfoViewHolder.actionBucket.setImageResource(R.drawable.ic_inbox_pink_400_18dp);

        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return (position==0) ? IMAGE_VIEW_TYPE : INFO_VIEW_TYPE;
    }
}
