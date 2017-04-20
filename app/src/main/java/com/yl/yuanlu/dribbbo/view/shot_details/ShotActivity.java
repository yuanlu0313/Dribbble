package com.yl.yuanlu.dribbbo.view.shot_details;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.yl.yuanlu.dribbbo.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by LUYUAN on 4/10/2017.
 */

public class ShotActivity extends AppCompatActivity {

    public static final String KEY_SHOT_TITLE = "shot_title";

    @BindView(R.id.toolbar) Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shot_details);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        //Activity function, change the title showed on Actionbar/Toolbar
        //call getIntent() to return the intent that start this activity, which contains the shot and title
        setTitle(getIntent().getStringExtra(KEY_SHOT_TITLE));

        //set to show back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //start the fragment
        //ShotFragment need to get the intent information in order to retrieve shot object from intent
        //call setArguments(Bundle args) to pass the intent information to ShotFragment
        //it is not recommended by Android to change the default fragment constructor
        ShotFragment shotFragment = new ShotFragment();
        shotFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container_shot_details, shotFragment)
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
