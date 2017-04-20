package com.yl.yuanlu.dribbbo.view.shot_list;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.reflect.TypeToken;
import com.yl.yuanlu.dribbbo.R;
import com.yl.yuanlu.dribbbo.model.Shot;
import com.yl.yuanlu.dribbbo.utils.ModelUtils;
import com.yl.yuanlu.dribbbo.view.shot_details.ShotActivity;
import com.yl.yuanlu.dribbbo.view.shot_details.ShotFragment;

import java.util.List;

/**
 * Created by LUYUAN on 4/5/2017.
 */

public class ShotListAdaptor extends RecyclerView.Adapter {

    private final static int VIEW_TYPE_SHOT = 1;
    private final static int VIEW_TYPE_LOAD = 2;

    List<Shot> data;
    LoadMoreListener loadMoreListener;
    private boolean showLoading;

    public ShotListAdaptor(@NonNull List<Shot> data, LoadMoreListener loadMoreListener) {
        this.data = data;
        this.loadMoreListener = loadMoreListener;
        this.showLoading = true;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_SHOT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_shot, parent, false);
            return new ShotViewHolder(view);
        }
        else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_loading, parent, false);
            //RecyclerView.ViewHolder() is abstract, so have to implement, that's why have {} after it
            //We don't need to show anything in loading animation, so just return default ViewHolder is enough
            return new RecyclerView.ViewHolder(view) {};
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        if(position < data.size()) {
            final Shot shot = data.get(position);
            ShotViewHolder shotViewHolder = (ShotViewHolder) holder;

            shotViewHolder.bucketCount.setText(String.valueOf(shot.buckets_count));
            shotViewHolder.likeCount.setText(String.valueOf(shot.likes_count));
            shotViewHolder.viewCount.setText(String.valueOf(shot.views_count));
            shotViewHolder.image.setImageResource(R.drawable.shot_image_holder);

            //Start ShotActivity to show shot details when a shot is clicked
            //pass shot to ShotFragment using intent and Gson
            //pass shot title to ShotActivity to show the shot title
            shotViewHolder.clickablecover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = holder.itemView.getContext();
                    Intent intent = new Intent(context, ShotActivity.class);
                    intent.putExtra(ShotFragment.KEY_SHOT, ModelUtils.toString(shot, new TypeToken<Shot>(){}));
                    intent.putExtra(ShotActivity.KEY_SHOT_TITLE, shot.title);
                    context.startActivity(intent);
                }
            });
        }
        else {
            //onBindViewHolder is called when the corresponding view just show up on UI
            //so set listener here to load more data when LOAD view appears
            loadMoreListener.onLoadMore();
        }
    }

    @Override
    public int getItemCount() {
        //+1 for loading animation
        return showLoading ? (data.size() + 1) : data.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(position < data.size()) return VIEW_TYPE_SHOT;
        else return VIEW_TYPE_LOAD;
    }

    public int getDataCount() {
        return data.size();
    }

    public void append(List<Shot> moreData) {
        data.addAll(moreData);
        notifyDataSetChanged();  //Keep UI and data synced up
    }

    public void setshowLoading(boolean showLoading) {
        this.showLoading = showLoading;
    }

    //implement an interface to support callback
    //**Why callback: Adaptor is responsible to show data on UI, while Fragment is to load data
    //so we utilize callback to let Fragment load data
    public interface LoadMoreListener {
        void onLoadMore();
    }


}
