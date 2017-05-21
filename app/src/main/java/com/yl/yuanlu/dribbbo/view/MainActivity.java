package com.yl.yuanlu.dribbbo.view;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yl.yuanlu.dribbbo.R;
import com.yl.yuanlu.dribbbo.dribbble.Dribbble;
import com.yl.yuanlu.dribbbo.dribbble.DribbbleException;
import com.yl.yuanlu.dribbbo.view.bucket_list.BucketListFragment;
import com.yl.yuanlu.dribbbo.view.shot_list.ShotListFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawerLayout;
    @BindView(R.id.nv_view) NavigationView navigationView;

    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //AppCompactActivity function, to replace Actionbar with Toolbar, which gives up more control and flexibility
        setSupportActionBar(toolbar);
        //setup navigation drawer
        setupDrawer();

        setupShotListFragment();

    }

    //below 4 functions are necessary for showing and implementing hamburger icon (just standard way)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    private void setupDrawer() {
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);

        //showing User name and picture in nav header
        View nav_header_view = navigationView.inflateHeaderView(R.layout.nav_header);

        ((TextView) nav_header_view.findViewById(R.id.nav_header_user_name)).setText(Dribbble.getCurrentUser().name);
        Glide.with(this).load(Dribbble.getCurrentUser().avatar_url).into((CircleImageView) nav_header_view.findViewById(R.id.nav_header_user_picture));

        //Log out operation, delete saved user and access_token
        nav_header_view.findViewById(R.id.nav_header_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dribbble.logout(MainActivity.this);
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //set up navigation drawer item select
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                //if the item clicked is already selected, close drawer directly
                if(item.isChecked()) {
                    drawerLayout.closeDrawers();
                    return true;
                }

                //listener on which item is clicked
                Fragment fragment = null;
                switch(item.getItemId()) {
                    case R.id.drawer_menu_item_home :
                        fragment = ShotListFragment.newInstance(ShotListFragment.HOME_LIST_TYPE);
                        setTitle("Dribbbo");
                        break;
                    case R.id.drawer_menu_item_likes :
                        fragment = ShotListFragment.newInstance(ShotListFragment.LIKE_LIST_TYPE);
                        setTitle("Likes");
                        break;
                    case R.id.drawer_menu_item_buckets :
                        fragment = BucketListFragment.newInstance(false, null);
                        setTitle("Buckets");
                        break;
                    default :
                }

                drawerLayout.closeDrawers();

                if(fragment != null) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container_shot_list, fragment)
                            .commit();
                    return true;
                }

                return false;
            }
        });

    }

    //add ShotList Fragment to Main Activity
    private void setupShotListFragment() {
        ShotListFragment shotListFragment = ShotListFragment.newInstance(ShotListFragment.HOME_LIST_TYPE);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container_shot_list, shotListFragment)
                .commit();
    }

}
