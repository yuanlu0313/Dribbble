package com.yl.yuanlu.dribbbo.view.shot_list;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yl.yuanlu.dribbbo.R;
import com.yl.yuanlu.dribbbo.model.Shot;
import com.yl.yuanlu.dribbbo.model.SpaceItemDecoration;
import com.yl.yuanlu.dribbbo.model.User;

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

    private static final int COUNT_PER_PAGE = 20;

    @BindView(R.id.recycler_view) RecyclerView recyclerView;

    private ShotListAdaptor adaptor;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recyclerview, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        //Implement the itemdecoration to add space around each item properly
        recyclerView.addItemDecoration(new SpaceItemDecoration(getResources().getDimensionPixelSize(R.dimen.spacing_medium)));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        //onLoadMore is called from UI thread, but LoadData is usually time consuming so need to open a new Thread
        //However, append data to adaptor have to be done on UI Thread, so use handler
        final android.os.Handler handler = new android.os.Handler();
        adaptor = new ShotListAdaptor(fakeData(0), new ShotListAdaptor.LoadMoreListener() {
            @Override
            public void onLoadMore() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    List<Shot> moreData = fakeData(adaptor.getDataCount()/COUNT_PER_PAGE);
                                    adaptor.append(moreData);
                                    adaptor.setshowLoading(moreData.size() == COUNT_PER_PAGE);
                                }
                            });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
        recyclerView.setAdapter(adaptor);
    }

    private List<Shot> fakeData(int page) {
        List<Shot> shotList = new ArrayList<>();
        Random random = new Random();
        int count = (page<2) ? COUNT_PER_PAGE : 10;
        for (int i = 0; i < count; ++i) {
            Shot shot = new Shot();
            shot.user = new User();
            shot.user.name = "Author " + i;
            shot.title = "Shot" + i;
            shot.views_count = random.nextInt(10000);
            shot.likes_count = random.nextInt(200);
            shot.buckets_count = random.nextInt(50);
            shot.description = "It's a good shot.";

            shotList.add(shot);
        }
        return shotList;
    }

}
