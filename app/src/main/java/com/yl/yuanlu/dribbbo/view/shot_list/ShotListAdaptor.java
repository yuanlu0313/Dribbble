package com.yl.yuanlu.dribbbo.view.shot_list;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
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
    private RequestManager glide;
    private boolean showLoading;
    private final ShotListFragment shotListFragment;

    public ShotListAdaptor(@NonNull List<Shot> data, ShotListFragment shotListFragment, RequestManager glide, LoadMoreListener loadMoreListener) {
        this.data = data;
        this.loadMoreListener = loadMoreListener;
        this.glide = glide;
        this.showLoading = true;
        this.shotListFragment = shotListFragment;
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
            glide.load(shot.getImageURL()).into(shotViewHolder.image);

            //Start ShotActivity to show shot details when a shot is clicked
            //pass shot to ShotFragment using intent and Gson
            //pass shot title to ShotActivity to show the shot title
            //***We have to do startActivityForResult() because when like/bucket operation done in ShotActivity,
            //the like/bucket count in ShotList also need to be updated (user use back button to come back, will not trigger reload data from API)
            shotViewHolder.clickablecover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(shotListFragment.getContext(), ShotActivity.class);
                    intent.putExtra(ShotFragment.KEY_SHOT, ModelUtils.toString(shot, new TypeToken<Shot>(){}));
                    intent.putExtra(ShotActivity.KEY_SHOT_TITLE, shot.title);
                    //startActivityForResult() usually called in Activity/Fragment who handles data
                    //so we pass in a shotListFragment object (this do the same thing as callback)
                    shotListFragment.startActivityForResult(intent, ShotListFragment.REQ_CODE);
                }
            });
        }
        else {
            //onBindViewHolder is called when the corresponding view just show up on UI
            //so set listener here to load more data when LOAD view appears
            //at very beginning when no data loaded, position>=0 valid, so onLoadMore() will also be called to load initial page
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

    public void setData(List<Shot> newData) {
        data.clear();
        data.addAll(newData);
        notifyDataSetChanged();
    }

    public void removeData(String ShotID) {
        for(int i=0; i<data.size(); i++) {
            if(data.get(i).id.equals(ShotID)) {
                data.remove(i);
                break;
            }
        }
        notifyDataSetChanged();
    }

    public List<Shot> getData() {
        return data;
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
