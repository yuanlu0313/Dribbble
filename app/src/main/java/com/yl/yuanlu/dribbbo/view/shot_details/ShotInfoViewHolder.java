package com.yl.yuanlu.dribbbo.view.shot_details;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.yl.yuanlu.dribbbo.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by LUYUAN on 4/10/2017.
 */

public class ShotInfoViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.shot_details_view_count) TextView viewCount;
    @BindView(R.id.shot_action_like) ImageButton actionLike;
    @BindView(R.id.shot_like_count) TextView likeCount;
    @BindView(R.id.shot_action_bucket) ImageButton actionBucket;
    @BindView(R.id.shot_bucket_count) TextView bucketCount;
    @BindView(R.id.shot_action_share) TextView actionShare;
    @BindView(R.id.shot_title) TextView title;
    @BindView(R.id.shot_author_picture) ImageView authorPicture;
    @BindView(R.id.shot_author_name) TextView authorName;
    @BindView(R.id.shot_description) TextView description;


    public ShotInfoViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
