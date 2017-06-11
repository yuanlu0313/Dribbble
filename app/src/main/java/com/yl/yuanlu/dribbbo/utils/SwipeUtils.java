package com.yl.yuanlu.dribbbo.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.yl.yuanlu.dribbbo.R;

/**
 * Created by LUYUAN on 5/16/2017.
 */

public abstract class SwipeUtils extends ItemTouchHelper.SimpleCallback {

    private Context context;
    private Drawable background;
    private Drawable deleteIcon;
    private int xMarkMargin;
    private boolean initiated;
    private int leftcolorCode;
    private String leftSwipeLable;


    /**
     * Creates a Callback for the given drag and swipe allowance. These values serve as
     * defaults
     * and if you want to customize behavior per ViewHolder, you can override
     * {@link #getSwipeDirs(RecyclerView, ViewHolder)}
     * and / or {@link #getDragDirs(RecyclerView, ViewHolder)}.
     *
     * @param dragDirs  Binary OR of direction flags in which the Views can be dragged. Must be
     *                  composed of {@link #LEFT}, {@link #RIGHT}, {@link #START}, {@link
     *                  #END},
     *                  {@link #UP} and {@link #DOWN}.
     * @param swipeDirs Binary OR of direction flags in which the Views can be swiped. Must be
     *                  composed of {@link #LEFT}, {@link #RIGHT}, {@link #START}, {@link
     *                  #END},
     *                  {@link #UP} and {@link #DOWN}.
     */
    public SwipeUtils(int dragDirs, int swipeDirs, Context context) {
        super(dragDirs, swipeDirs);
        this.context = context;
    }

    public void init() {
        background = new ColorDrawable();   //A specialized Drawable that fills the Canvas with a specified color.
        xMarkMargin = (int) context.getResources().getDimension(R.dimen.ic_clear_margin);
        deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete_white_24dp);
        deleteIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        initiated = true;
    }

    //used only by drag and move operation
    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public abstract void onSwiped(RecyclerView.ViewHolder viewHolder, int direction);

    @Override
    public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        return super.getSwipeDirs(recyclerView, viewHolder);
    }

    //called by ItemTouchHelper during recycler view onDraw(), which is itemDecoration function
    //onDraw(): Draw any appropriate decorations into the Canvas supplied to the RecyclerView. Any content drawn by this method will be drawn before the item views are drawn, and will thus appear underneath the views.
    //override to customize the view response to user interaction
    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

        //the view of item being swiped/dragged
        //can use this to get the coordinate of the item on screen
        View itemView = viewHolder.itemView;

        if (!initiated) {
            init();
        }

        int itemHeight = itemView.getBottom() - itemView.getTop();

        //Setting Swipe Background
        ((ColorDrawable) background).setColor(getLeftcolorCode());
        //dX and dY gives the which coordinate the user swipe or drag the item to
        //dX will be negative when swipe from right to left
        //set the boundary of background based on the information
        background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
        //call draw() to draw in its boundary on the canvas
        background.draw(c);

        //returns the exact size of the drawable which you have put in the resource folder without any modification.
        int intrinsicWidth = deleteIcon.getIntrinsicWidth();
        int intrinsicHeight = deleteIcon.getIntrinsicWidth();

        //calculate which coordinate region on screen the icon should sit
        int xMarkLeft = itemView.getRight() - xMarkMargin - intrinsicWidth;
        int xMarkRight = itemView.getRight() - xMarkMargin;
        int xMarkTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
        int xMarkBottom = xMarkTop + intrinsicHeight;


        //Setting up Swipe Icon
        deleteIcon.setBounds(xMarkLeft, xMarkTop + 23, xMarkRight, xMarkBottom + 23);
        deleteIcon.draw(c);

        //Setting up Swipe Text "Delete"
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(48);
        paint.setTextAlign(Paint.Align.CENTER);
        c.drawText(getLeftSwipeLable(), xMarkLeft + 30, xMarkTop + 13, paint);

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    //set and return what test need to be painted when swipe (together with the icon)
    public String getLeftSwipeLable() {
        return leftSwipeLable;
    }

    public void setLeftSwipeLable(String leftSwipeLable) {
        this.leftSwipeLable = leftSwipeLable;
    }

    //set and return the background color when swipe
    public int getLeftcolorCode() {
        return leftcolorCode;
    }

    public void setLeftcolorCode(int leftcolorCode) {
        this.leftcolorCode = leftcolorCode;
    }

}
