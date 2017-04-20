package com.yl.yuanlu.dribbbo.model;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by LUYUAN on 4/9/2017.
 */

public class SpaceItemDecoration extends RecyclerView.ItemDecoration {

    private int space;

    public SpaceItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.bottom = space;
        outRect.left = space;
        outRect.right = space;

        //only the first item need a top space, we don't want double the space between 2 items
        if(parent.getChildAdapterPosition(view) == 0) {
            outRect.top = space;
        }
    }
}
