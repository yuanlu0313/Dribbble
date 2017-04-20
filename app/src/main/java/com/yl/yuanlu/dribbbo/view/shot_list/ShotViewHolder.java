package com.yl.yuanlu.dribbbo.view.shot_list;

import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.yl.yuanlu.dribbbo.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by LUYUAN on 4/5/2017.
 */

public class ShotViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.list_item_shot_bucket_count) TextView bucketCount;
    @BindView(R.id.list_item_shot_like_count) TextView likeCount;
    @BindView(R.id.list_item_shot_view_count) TextView viewCount;
    @BindView(R.id.list_item_shot_image) ImageView image;
    @BindView(R.id.shot_clickable_cover) View clickablecover;

    public ShotViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
