package com.yl.yuanlu.dribbbo.view.shot_list;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.yl.yuanlu.dribbbo.model.Shot;
import com.yl.yuanlu.dribbbo.model.SpaceItemDecoration;
import com.yl.yuanlu.dribbbo.model.User;
import com.yl.yuanlu.dribbbo.utils.ModelUtils;
import com.yl.yuanlu.dribbbo.view.shot_details.ShotFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by LUYUAN on 4/5/2017.
 */

public class ShotListFragment extends Fragment {

    public static final int REQ_CODE = 200;
    public static final int HOME_LIST_TYPE = 1;
    public static final int LIKE_LIST_TYPE = 2;
    public static final int BUCKET_LIST_TYPE = 3;

    public static final String KEY_LIST_TYPE = "ListType";
    public static final String KEY_BUCKET_ID = "bucket_id";

    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @BindView(R.id.pull_refresh_container) SwipeRefreshLayout swipeRefreshLayout;

    private ShotListAdaptor adaptor;
    private int listType;

    //not recommended to overwrite default Fragment constructor
    //standard way is as follow (use Bundle args)
    public static ShotListFragment newInstance(int list_type) {
        Bundle args = new Bundle();
        args.putInt(KEY_LIST_TYPE, list_type);
        ShotListFragment shotListFragment = new ShotListFragment();
        shotListFragment.setArguments(args);
        return shotListFragment;
    }

    public static ShotListFragment newBucketShotListInstance(@NonNull String bucketID) {
        Bundle args = new Bundle();
        args.putInt(KEY_LIST_TYPE, BUCKET_LIST_TYPE);
        args.putString(KEY_BUCKET_ID, bucketID);
        ShotListFragment shotListFragment = new ShotListFragment();
        shotListFragment.setArguments(args);
        return shotListFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recyclerview, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        listType = getArguments().getInt(KEY_LIST_TYPE);

        //Implement the itemdecoration to add space around each item properly
        recyclerView.addItemDecoration(new SpaceItemDecoration(getResources().getDimensionPixelSize(R.dimen.spacing_medium)));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        //disable pull to refresh when data is not yet loaded
        swipeRefreshLayout.setEnabled(false);

        //setup pull to refresh functionality
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                AsyncTaskCompat.executeParallel(new LoadShots(true));
            }
        });

        //load more data from API
        //the initial page load is also here, as in ShotListAdaptor constructor, we will set showLoading = true by default
        //then System call getItemCount and will get "1", so System will call onBindViewHolder() which would call onLoadMore()
        adaptor = new ShotListAdaptor(new ArrayList<Shot>(), this, Glide.with(this), new ShotListAdaptor.LoadMoreListener() {
            @Override
            public void onLoadMore() {
                AsyncTaskCompat.executeParallel(new LoadShots(false));
            }
        });
        recyclerView.setAdapter(adaptor);
    }

    //when jump back from ShotFragment (back button), need to update that specific shot's like/bucket count to make sure data synced between two Fragments
    //if it is LIKES tab, remove the shot from list if it is unliked in ShotFragment
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==ShotListFragment.REQ_CODE && resultCode== Activity.RESULT_OK) {
            Shot updatedShot = ModelUtils.toObject(data.getStringExtra(ShotFragment.KEY_SHOT), new TypeToken<Shot>(){});
            Log.i("Yuan DBG : ", String.valueOf(listType));
            if(listType==LIKE_LIST_TYPE && !updatedShot.liked) {
                adaptor.removeData(updatedShot.id);
            }
            //Have to refresh list because cannot directly use updatedShot.bucketed to determine whether this shot is still in this bucket
            else if(listType==BUCKET_LIST_TYPE) {
                AsyncTaskCompat.executeParallel(new LoadShots(true));
            }
            else {
                for (Shot shot : adaptor.getData()) {
                    if (shot.id.equals(updatedShot.id)) {
                        shot.likes_count = updatedShot.likes_count;
                        shot.buckets_count = updatedShot.buckets_count;
                        adaptor.notifyDataSetChanged();
                        break;
                    }
                }
            }
        }
    }

    //the asynctask to load shots list from API
    private class LoadShots extends AsyncTask<Void, Void, List<Shot>> {

        private Boolean refresh;  //distinguish whether this is a refresh operation or normal load operation

        public LoadShots(Boolean refresh) {
            this.refresh = refresh;
        }

        @Override
        protected List<Shot> doInBackground(Void... params) {
            int page = refresh ? 1 : adaptor.getDataCount() / Dribbble.COUNT_PER_LOAD + 1;
            try {
                switch (listType) {
                    case HOME_LIST_TYPE :
                        return Dribbble.getShotList(page);
                    case LIKE_LIST_TYPE :
                        return Dribbble.getLikedShotList(page);
                    case BUCKET_LIST_TYPE :
                        String bucketID = getArguments().getString(KEY_BUCKET_ID);
                        return Dribbble.getBucketShotList(bucketID, page);
                    default:
                        return Dribbble.getShotList(page);
                }
            } catch (DribbbleException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Shot> shots) {
            if(refresh) {
                adaptor.setData(shots);
                swipeRefreshLayout.setRefreshing(false); //make the refresh icon disappear after data loaded
            }
            else {
                adaptor.append(shots);
                swipeRefreshLayout.setEnabled(true);  //enable pull to refresh after data loaded
            }
            adaptor.setshowLoading(shots.size()>=Dribbble.COUNT_PER_LOAD);
        }
    }

}
