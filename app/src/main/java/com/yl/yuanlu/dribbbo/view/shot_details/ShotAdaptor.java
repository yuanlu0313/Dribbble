package com.yl.yuanlu.dribbbo.view.shot_details;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yl.yuanlu.dribbbo.R;
import com.yl.yuanlu.dribbbo.model.Shot;

import java.util.zip.Inflater;

/**
 * Created by LUYUAN on 4/10/2017.
 */

public class ShotAdaptor extends RecyclerView.Adapter {

    private final static int IMAGE_VIEW_TYPE = 1;
    private final static int INFO_VIEW_TYPE = 2;

    Shot shot;

    public ShotAdaptor(Shot shot) {
        this.shot = shot;
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
            //Load picture here, temporarily do nothing
        }
        else {
            ShotInfoViewHolder shotInfoViewHolder = (ShotInfoViewHolder) holder;
            shotInfoViewHolder.viewCount.setText(String.valueOf(shot.views_count));
            shotInfoViewHolder.likeCount.setText(String.valueOf(shot.likes_count));
            shotInfoViewHolder.bucketCount.setText(String.valueOf(shot.buckets_count));
            shotInfoViewHolder.title.setText(shot.title);
            shotInfoViewHolder.authorName.setText(shot.user.name);
            shotInfoViewHolder.description.setText(shot.description);
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
