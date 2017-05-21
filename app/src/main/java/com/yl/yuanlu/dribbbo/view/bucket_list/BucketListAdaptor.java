package com.yl.yuanlu.dribbbo.view.bucket_list;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yl.yuanlu.dribbbo.R;
import com.yl.yuanlu.dribbbo.dribbble.Dribbble;
import com.yl.yuanlu.dribbbo.dribbble.DribbbleException;
import com.yl.yuanlu.dribbbo.model.Bucket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by LUYUAN on 5/1/2017.
 */

public class BucketListAdaptor extends RecyclerView.Adapter {

    List<Bucket> data;
    private List<String> itemsPendingRemoval;
    private static final int PENDING_REMOVAL_TIMEOUT = 3000; // 3sec
    private Handler handler = new Handler(); // hanlder for running delayed runnables
    private HashMap<String, Runnable> pendingRunnables = new HashMap<>();

    LoadMoreListener loadMoreListener;
    private boolean showLoading;
    private boolean showCheckBox;
    Context context;

    public BucketListAdaptor(@NonNull List<Bucket> data, Context context, Boolean showCheckBox, LoadMoreListener loadMoreListener) {
        this.data = data;
        this.loadMoreListener = loadMoreListener;
        this.showCheckBox = showCheckBox;
        this.showLoading = true;
        this.context = context;
        itemsPendingRemoval = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_bucket, parent, false);
        return new BucketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        //Log.i("Yuan DBG : ", String.valueOf(data.size()));
        if(position < data.size()) {
            final Bucket bucket = data.get(position);
            BucketViewHolder bucketViewHolder = (BucketViewHolder) holder;

            bucketViewHolder.bucketName.setText(bucket.name);
            bucketViewHolder.bucketShotCount.setText(Integer.toString(bucket.shots_count) + "shots");
            if(showCheckBox) {
                bucketViewHolder.swipeBucketLayout.setVisibility(View.GONE);
                bucketViewHolder.regularBucketLayout.setVisibility(View.VISIBLE);
                bucketViewHolder.bucketCheckBox.setVisibility(View.VISIBLE);
                bucketViewHolder.bucketCheckBox.setImageResource((bucket.isChosen) ?
                    R.drawable.ic_check_box_black_24dp :
                    R.drawable.ic_check_box_outline_blank_black_24dp);
                bucketViewHolder.bucketClickableCover.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bucket.isChosen = !bucket.isChosen;
                        notifyItemChanged(position);
                    }
                });
            }
            else if(itemsPendingRemoval.contains(bucket.id)) {
                bucketViewHolder.swipeBucketLayout.setVisibility(View.VISIBLE);
                bucketViewHolder.regularBucketLayout.setVisibility(View.GONE);
                bucketViewHolder.undo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        undoDel(bucket.id, position);
                    }
                });
            }
            else {
                bucketViewHolder.swipeBucketLayout.setVisibility(View.GONE);
                bucketViewHolder.regularBucketLayout.setVisibility(View.VISIBLE);
                bucketViewHolder.bucketCheckBox.setVisibility(View.GONE);
                bucketViewHolder.bucketClickableCover.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, BucketShotListActivity.class);
                        intent.putExtra(BucketShotListActivity.KEY_BUCKET_TITLE, bucket.name);
                        intent.putExtra(BucketShotListActivity.KEY_BUCKET_ID, bucket.id);
                        context.startActivity(intent);
                    }
                });
            }
        }
        else {
            loadMoreListener.onLoadMore();
        }
    }

    @Override
    public int getItemCount() {
        return showLoading ? (data.size() + 1) : data.size();
    }

    public int getDataCount() {
        return data.size();
    }

    public void setData(List<Bucket> newData) {
        data.clear();
        data.addAll(newData);
        notifyDataSetChanged();
    }

    public List<Bucket> getData () {
        return data;
    }

    public void append(List<Bucket> newData) {
        data.addAll(newData);
        notifyDataSetChanged();
    }

    public void prepend(Bucket newData) {
        data.add(0, newData);
        notifyDataSetChanged();
    }

    public void clearData() {
        data.clear();
        notifyDataSetChanged();
    }

    public void setshowLoading(boolean showLoading) {
        this.showLoading = showLoading;
    }

    public interface LoadMoreListener {
        void onLoadMore();
    }


    /******* Swipe to delete related functions ******/

    //whether the item in position is swiped and pending for delete
    public boolean isPendingRemoval(int position) {
        return itemsPendingRemoval.contains(data.get(position).id);
    }

    //undo pending removal process when undo is clicked
    private void undoDel(String bucketID, int position) {
        Runnable pendingRemovalRunnable = pendingRunnables.get(bucketID);
        pendingRunnables.remove(bucketID);
        if(pendingRemovalRunnable!=null) {
            //call to remove any pending posts of Runnable r that are in the message queue.
            handler.removeCallbacks(pendingRemovalRunnable);
        }
        itemsPendingRemoval.remove(bucketID);
        notifyItemChanged(position);
    }

    //add the item in position to pending removal status
    public void pendingRemoval(final int position) {
        final String bucketID = data.get(position).id;
        final Bucket bucket = data.get(position);
        if(!itemsPendingRemoval.contains(bucketID)) {
            //add bucketID to itemsPendingRemoval and notify system to redraw that item
            //list_item_swipe will be drawn then
            itemsPendingRemoval.add(bucketID);
            notifyItemChanged(position);
            //wait 3 second then delete, by open a new thread
            Runnable pendingRemovalRunnable = new Runnable() {
                @Override
                public void run() {
                    int position_to_remove = -1;
                    for(int i=0; i<data.size(); i++) {
                        if(bucket.id.equals(data.get(i).id)) {
                            position_to_remove = i;
                            break;
                        }
                    }
                    if(position_to_remove!=-1) {
                        remove(position_to_remove);
                    }
                }
            };
            handler.postDelayed(pendingRemovalRunnable, PENDING_REMOVAL_TIMEOUT);   //delay the thread by 3 seconds
            pendingRunnables.put(bucketID, pendingRemovalRunnable);
        }
    }

    //request to API to remove a bucket, also remove it from itemsPendingRemoval, and remove it from adaptor data
    //use synchronized to make sure remove() can only be called by one thread at a time, this can prevent risk condition
    private synchronized void remove (int position) {
        String bucketID = data.get(position).id;
        if(itemsPendingRemoval.contains(bucketID)) {
            itemsPendingRemoval.remove(bucketID);
        }
        data.remove(position);
        notifyItemRemoved(position);
        AsyncTaskCompat.executeParallel(new deleteBucketTask(bucketID));
    }

    private class deleteBucketTask extends AsyncTask<Void, Void, Void> {

        private String buckstID;

        public deleteBucketTask(@NonNull String bucketID) {
            this.buckstID = bucketID;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Dribbble.deleteBucket(buckstID);
            } catch (DribbbleException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}
