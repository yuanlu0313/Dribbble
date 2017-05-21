package com.yl.yuanlu.dribbbo.view.shot_details;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.google.gson.reflect.TypeToken;
import com.yl.yuanlu.dribbbo.R;
import com.yl.yuanlu.dribbbo.dribbble.Dribbble;
import com.yl.yuanlu.dribbbo.dribbble.DribbbleException;
import com.yl.yuanlu.dribbbo.model.Bucket;
import com.yl.yuanlu.dribbbo.model.Shot;
import com.yl.yuanlu.dribbbo.model.User;
import com.yl.yuanlu.dribbbo.utils.ModelUtils;
import com.yl.yuanlu.dribbbo.view.bucket_list.BucketListActivity;
import com.yl.yuanlu.dribbbo.view.bucket_list.BucketListFragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by LUYUAN on 4/10/2017.
 */

public class ShotFragment extends Fragment {

    //The key used to pass a single shot info with intent
    public final static String KEY_SHOT = "single_shot";
    public final static int REQ_CODE_BUCKET_LIST = 500;

    @BindView(R.id.recycler_view_shot) RecyclerView recyclerView;

    private ShotAdaptor adaptor;
    private Shot shot;
    private Boolean opFailed;  //use to tell whether the operation submitted to API is executed successfully, if not, nothing should be changed
    private ArrayList<String> checkedBucketIDs;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shot_recyclerview, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        opFailed=false;
        //retrieve the shot from intent
        //can use getArguments because we call setArguments in ShotActivity
        shot = ModelUtils.toObject(getArguments().getString(KEY_SHOT), new TypeToken<Shot>(){});
        shot.liked = false;  //**Temp
        shot.bucketed = false;
        adaptor = new ShotAdaptor(shot, Glide.with(this), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adaptor);

        //check whether the current shot is liked/bucketed by current loggedin user
        AsyncTaskCompat.executeParallel(new CheckLiked());
        AsyncTaskCompat.executeParallel(new checkBucketed());
    }

    //update shot UI after comming back from BucketList
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQ_CODE_BUCKET_LIST && resultCode==Activity.RESULT_OK) {
            ArrayList<String> selectedBucketIDs = data.getStringArrayListExtra(BucketListFragment.KEY_SELECTED_BUCKET_ID);
            shot.bucketed = (selectedBucketIDs.size()>0);
            shot.buckets_count += (selectedBucketIDs.size() - checkedBucketIDs.size());
            recyclerView.getAdapter().notifyDataSetChanged();
            ArrayList<String> toAdd = new ArrayList<>();
            ArrayList<String> toRemove = new ArrayList<>();
            for(String checked : checkedBucketIDs) {
                if(!selectedBucketIDs.contains(checked)) toRemove.add(checked);
            }
            for(String selected : selectedBucketIDs) {
                if(!checkedBucketIDs.contains(selected)) toAdd.add(selected);
            }
            AsyncTaskCompat.executeParallel(new updateBucketTask(toAdd, toRemove));
        }
    }

    //update the modified shot back to ShotListFragment to make sure data is synced when go back
    //put it in intent here, the jump back is by ShotActivity finish()
    private void updateShotResult() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(KEY_SHOT, ModelUtils.toString(shot, new TypeToken<Shot>(){}));
        getActivity().setResult(Activity.RESULT_OK, resultIntent);
    }

    //check whether the current shot is liked by current loggedin user
    private class CheckLiked extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                return Dribbble.isLiked(shot.id);
            } catch (DribbbleException e) {
                opFailed = true;
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if(!opFailed) {
                shot.liked = aBoolean;
                recyclerView.getAdapter().notifyDataSetChanged();   //***notify adaptor for the change
            }
            else opFailed = false;
        }
    }

    //following two functions implement like/unlike this shot function
    public void like() {
        AsyncTaskCompat.executeParallel(new likeTask());
    }

    private class likeTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            if(shot.liked) {
                try {
                    Dribbble.unlikeShot(shot.id);
                } catch (DribbbleException e) {
                    opFailed = true;
                    e.printStackTrace();
                }
            }
            else {
                try {
                    Dribbble.likeShot(shot.id);
                } catch (DribbbleException e) {
                    opFailed = true;
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(!opFailed) {
                shot.liked = !shot.liked;
                shot.likes_count += (shot.liked) ? 1 : -1;
                recyclerView.getAdapter().notifyDataSetChanged();   //***notify adaptor for the change
                //update this shot to ShotListFragment to sync up data there
                updateShotResult();
            }
            else opFailed = false;
        }

    }

    //check whether this shot is in any of user's buckets
    //HOW: load all buckets contains this shot, load all current user buckets, find the intersection
    //return the bucket list for further bucket/unbucket operation
    private class checkBucketed extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected List<String> doInBackground(Void... params) {
            List<Bucket> userBuckets;
            List<Bucket> shotBuckets;
            try {
                userBuckets = Dribbble.getBucketList();
                shotBuckets = Dribbble.getShotBucketList(shot.id);
            } catch (DribbbleException e) {
                e.printStackTrace();
                return null;
            }

            Set<String> userBucketIDs = new HashSet<>();
            for(Bucket bucket : userBuckets) {
                userBucketIDs.add(bucket.id);
            }

            List<String> checkedBucketIDs = new ArrayList<>();
            for(Bucket bucket : shotBuckets) {
                if(userBucketIDs.contains(bucket.id)) {
                    checkedBucketIDs.add(bucket.id);
                }
            }

            return checkedBucketIDs;
        }

        @Override
        protected void onPostExecute(List<String> strings) {
            checkedBucketIDs = (ArrayList<String>) strings;
            if(!checkedBucketIDs.isEmpty()) {
                shot.bucketed = true;
            }
            recyclerView.getAdapter().notifyDataSetChanged();   //***notify adaptor for the change
        }
    }

    //bucket/unbucket operation, jump to BucketList activity
    public void bucket() {
        Intent intent = new Intent(getContext(), BucketListActivity.class);
        intent.putStringArrayListExtra(BucketListFragment.KEY_CHECKED_BUCKET_ID, checkedBucketIDs);
        startActivityForResult(intent, REQ_CODE_BUCKET_LIST);
    }

    //update the bucket to API based on user's selection result in BucketListActivity
    private class updateBucketTask extends AsyncTask<Void, Void, Void> {

        private ArrayList<String> toAdd;
        private ArrayList<String> toRemove;

        public updateBucketTask(ArrayList<String> toAdd, ArrayList<String> toRemove) {
            this.toAdd = toAdd;
            this.toRemove = toRemove;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                for(String addIDs : toAdd) {
                    Dribbble.addShotToBucket(addIDs, shot.id);
                }
                for(String removeIDs : toRemove) {
                    Dribbble.removeShotFromBucket(removeIDs, shot.id);
                }
            } catch (DribbbleException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            updateShotResult();
        }
    }

}
