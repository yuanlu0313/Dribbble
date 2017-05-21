package com.yl.yuanlu.dribbbo.view.bucket_list;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yl.yuanlu.dribbbo.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by LUYUAN on 5/1/2017.
 */

public class BucketViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.regular_bucket_layout) FrameLayout regularBucketLayout;
    @BindView(R.id.swipe_layout) LinearLayout swipeBucketLayout;
    @BindView(R.id.undo) TextView undo;
    @BindView(R.id.bucket_clickable_cover) View bucketClickableCover;
    @BindView(R.id.bucket_name) TextView bucketName;
    @BindView(R.id.bucket_shot_count) TextView bucketShotCount;
    @BindView(R.id.bucket_check_box) ImageView bucketCheckBox;

    public BucketViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
