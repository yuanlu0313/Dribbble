package com.yl.yuanlu.dribbbo.view.shot_details;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.yl.yuanlu.dribbbo.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by LUYUAN on 4/10/2017.
 */

public class ShotImageViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.shot_details_image) ImageView imageView;

    public ShotImageViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
