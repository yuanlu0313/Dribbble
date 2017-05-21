package com.yl.yuanlu.dribbbo.view.bucket_list;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.yl.yuanlu.dribbbo.R;
import com.yl.yuanlu.dribbbo.view.shot_list.ShotListFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by LUYUAN on 5/7/2017.
 */

public class BucketShotListActivity extends AppCompatActivity {

    @BindView(R.id.toolbar) Toolbar toolbar;

    public static final String KEY_BUCKET_TITLE = "bucket_title";
    public static final String KEY_BUCKET_ID = "bucket_id";

    private String bucket_ID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bucket_shotlist);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        setTitle(getIntent().getStringExtra(KEY_BUCKET_TITLE));
        bucket_ID = getIntent().getStringExtra(KEY_BUCKET_ID);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ShotListFragment fragment = ShotListFragment.newBucketShotListInstance(bucket_ID);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container_bucket_shotlist, fragment)
                .commit();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
