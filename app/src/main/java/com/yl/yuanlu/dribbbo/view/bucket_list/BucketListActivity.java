package com.yl.yuanlu.dribbbo.view.bucket_list;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.yl.yuanlu.dribbbo.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by LUYUAN on 5/8/2017.
 */

public class BucketListActivity extends AppCompatActivity {

    @BindView(R.id.toolbar) Toolbar toobar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bucket_list);
        ButterKnife.bind(this);

        setSupportActionBar(toobar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.choose_bucket);

        ArrayList<String> checkedBucketIDs = getIntent().getStringArrayListExtra(BucketListFragment.KEY_CHECKED_BUCKET_ID);

        BucketListFragment fragment = BucketListFragment.newInstance(true, checkedBucketIDs);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container_bucket_list, fragment)
                .commit();

    }

    //Implement the back button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
