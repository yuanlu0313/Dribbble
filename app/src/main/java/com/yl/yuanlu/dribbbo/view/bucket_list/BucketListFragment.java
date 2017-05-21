package com.yl.yuanlu.dribbbo.view.bucket_list;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.yl.yuanlu.dribbbo.R;
import com.yl.yuanlu.dribbbo.dribbble.Dribbble;
import com.yl.yuanlu.dribbbo.dribbble.DribbbleException;
import com.yl.yuanlu.dribbbo.model.Bucket;
import com.yl.yuanlu.dribbbo.model.SpaceItemDecoration;
import com.yl.yuanlu.dribbbo.utils.SwipeUtils;
import com.yl.yuanlu.dribbbo.view.shot_list.ShotListFragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by LUYUAN on 5/1/2017.
 */

public class BucketListFragment extends Fragment {

    @BindView(R.id.recycler_view_with_fab) RecyclerView recyclerView;
    @BindView(R.id.pull_refresh_container) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.fab_add) FloatingActionButton fab;

    public static final String KEY_SHOW_CHECK_BOX = "show_checkbox";
    public static final String KEY_CHECKED_BUCKET_ID = "checked_bucket_id";
    public static final String KEY_SELECTED_BUCKET_ID = "selected_bucket_id";
    public static final int DIALOG_REQ_CODE = 300;

    private BucketListAdaptor adaptor;
    private Boolean showCheckBox;
    private Set<String> checkedBucketIDSet;

    public static BucketListFragment newInstance(boolean showCheckBox, ArrayList<String> checkedBucketIDs) {
        Bundle args = new Bundle();
        args.putBoolean(KEY_SHOW_CHECK_BOX, showCheckBox);
        args.putStringArrayList(KEY_CHECKED_BUCKET_ID, checkedBucketIDs);
        BucketListFragment bucketListFragment = new BucketListFragment();
        bucketListFragment.setArguments(args);
        return bucketListFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //must add this if need to setup menu in fragment not in activity
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fab_recyclerview, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        showCheckBox = getArguments().getBoolean(KEY_SHOW_CHECK_BOX);
        if(showCheckBox) {
            List<String> checkedBucketID = getArguments().getStringArrayList(KEY_CHECKED_BUCKET_ID);
            if(checkedBucketID!=null) {
                checkedBucketIDSet = new HashSet<>(checkedBucketID);
            }
        }

        //Implement the itemdecoration to add space around each item properly
        recyclerView.addItemDecoration(new SpaceItemDecoration(getResources().getDimensionPixelSize(R.dimen.spacing_medium)));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        //disable pull to refresh when data is not yet loaded
        swipeRefreshLayout.setEnabled(false);

        //setup pull to refresh functionality
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                AsyncTaskCompat.executeParallel(new LoadBuckets(true));
            }
        });

        adaptor = new BucketListAdaptor(new ArrayList<Bucket>(), getContext(), showCheckBox, new BucketListAdaptor.LoadMoreListener() {
            @Override
            public void onLoadMore() {
                AsyncTaskCompat.executeParallel(new LoadBuckets(false){});
            }
        });

        recyclerView.setAdapter(adaptor);

        //click floating action button to create a new Bucket
        //start a DialogFragment, use setTargetFragment between two fragments
        //similar to startActivity on two Activities
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewBucketDialogFragment dialogFragment = new NewBucketDialogFragment();
                dialogFragment.setTargetFragment(BucketListFragment.this, DIALOG_REQ_CODE);
                dialogFragment.show(getFragmentManager(), NewBucketDialogFragment.TAG);
            }
        });

        //swipe to delete feature
        if(!showCheckBox) {
            setSwipeForRecyclerView();
        }

    }

    //to update shot number of each bucket in list
    //when we jump from BucketListFragment to ShotListActivity, BucketListFragment won't be destroyed, will be in onStop() stage
    //when we click back button to navigate back to BucketListFragment, it will move from onStop() to onStart()
    //View won't be recreated as resource for BucketListFragment still saved
    //refer to Fragment lifecycle
    @Override
    public void onStart() {
        super.onStart();
        if(!showCheckBox && !adaptor.data.isEmpty()) {
            adaptor.clearData();
            AsyncTaskCompat.executeParallel(new LoadBuckets(true));
        }
    }

    //overwrite the menu with the one with SAVE button in choosing mode
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if(showCheckBox) {
            inflater.inflate(R.menu.bucket_choosing_menu, menu);
        }
    }

    //when SAVE clicked, update the selected bucket list and jump back to ShotActivity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.save) {
            //Snackbar.make(getView(), "SAVE Clicked", Snackbar.LENGTH_LONG).show();
            ArrayList<String> selectedBucketIDs = new ArrayList<>();
            ArrayList<Bucket> buckets = (ArrayList<Bucket>) adaptor.getData();
            for(Bucket bucket : buckets) {
                if(bucket.isChosen) {
                    selectedBucketIDs.add(bucket.id);
                }
            }
            Intent resultIntent = new Intent();
            resultIntent.putStringArrayListExtra(KEY_SELECTED_BUCKET_ID, selectedBucketIDs);
            //It's OK to call setResult() and finish() from fragment, not activity, just need to getActivity()
            getActivity().setResult(Activity.RESULT_OK, resultIntent);
            getActivity().finish();
        }
        return super.onOptionsItemSelected(item);
    }

    //when jumped back from the NewBucketDialogFragment, it's called inside NewBucketDialogFragment
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == DIALOG_REQ_CODE && resultCode == Activity.RESULT_OK) {
            String bucketName = data.getStringExtra(NewBucketDialogFragment.KEY_BUCKET_NAME);
            String bucketDescription = data.getStringExtra(NewBucketDialogFragment.KEY_BUCKET_DESCRIPTION);
            if(!TextUtils.isEmpty(bucketName)) {
                //create a new bucket
                AsyncTaskCompat.executeParallel(new NewBucket(bucketName, bucketDescription));
            }
        }
    }

    //load bucket list from API
    private class LoadBuckets extends AsyncTask<Void, Void, List<Bucket>> {

        private boolean refresh;

        public LoadBuckets(Boolean refresh) {
            this.refresh = refresh;
        }

        @Override
        protected List<Bucket> doInBackground(Void... params) {
            int page = refresh ? 1 : adaptor.getDataCount() / Dribbble.COUNT_PER_LOAD + 1;
            try {
                return Dribbble.getBucketList(page);
            } catch (DribbbleException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Bucket> buckets) {
            if(checkedBucketIDSet != null) {
                for(Bucket bucket : buckets) {
                    bucket.isChosen = checkedBucketIDSet.contains(bucket.id);
                }
            }

            if(refresh) {
                adaptor.setData(buckets);
                swipeRefreshLayout.setRefreshing(false);
            }
            else {
                adaptor.append(buckets);
                swipeRefreshLayout.setEnabled(true);
            }

            adaptor.setshowLoading(buckets.size()>=Dribbble.COUNT_PER_LOAD);
        }
    }

    //create a new Bucket to API and insert it to front of Bucket List
    private class NewBucket extends AsyncTask<Void, Void, Bucket> {

        private String bucketName;
        private String bucketDescription;

        public NewBucket(String bucketName, String bucketDescription) {
            this.bucketName = bucketName;
            this.bucketDescription = bucketDescription;
        }

        @Override
        protected Bucket doInBackground(Void... params) {
            try {
                return Dribbble.createBucket(bucketName, bucketDescription);
            } catch (DribbbleException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bucket bucket) {
            if(showCheckBox) {
                bucket.isChosen = true;
            }
            adaptor.prepend(bucket);
        }
    }

    //set up the swipe to delete feature
    public void setSwipeForRecyclerView() {
        SwipeUtils swipeHelper = new SwipeUtils(0, ItemTouchHelper.LEFT, getActivity()) {

            //onSwiped() will be called after item being swiped all the way to one side and dropped
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                Log.i("Yuan DBG : ", "swipe called!!!");
                int swipedPosition = viewHolder.getAdapterPosition();   //get which item is swiped
                BucketListAdaptor adaptor = (BucketListAdaptor) recyclerView.getAdapter();
                adaptor.pendingRemoval(swipedPosition); //start the pending removal process
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getAdapterPosition();
                BucketListAdaptor adaptor = (BucketListAdaptor) recyclerView.getAdapter();
                if(adaptor.isPendingRemoval(position)) {
                    return 0;
                }
                return super.getSwipeDirs(recyclerView, viewHolder);
            }
        };

        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(swipeHelper);
        mItemTouchHelper.attachToRecyclerView(recyclerView);
        swipeHelper.setLeftSwipeLable("Delete");
        swipeHelper.setLeftcolorCode(ContextCompat.getColor(getActivity(), R.color.swipe_bg));
    }

}
