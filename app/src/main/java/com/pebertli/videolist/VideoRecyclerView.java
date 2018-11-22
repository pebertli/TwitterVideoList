/*
 * *
 *  * Created by Pebertli Barata on 9/16/18 11:02 AM
 *  * Copyright (c) 2018 . All rights reserved.
 *  * Last modified 9/16/18 11:02 AM
 *
 */

package com.pebertli.videolist;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Recyclerview specialization that holds holds a player, passed by dependency injection
 * implements the OnTwitterResponseListener to receive the response of the Rest Api
 * overrides OnScrolled to calculate the row in the center and the row to start the endless scrolling
 */
public class VideoRecyclerView extends RecyclerView implements TwitterSearch.OnTwitterResponseListener
{
    final List<VideoModel> items = new ArrayList<>();
    final private int mEagerItemLoading = 5;
    final private TwitterSearch mTwitterSearch;
    final private EasyPlayer mPlayer;
    final private LinearLayoutManager mLayoutManager;
    private int mCurrentIndexRowPlaying = -1;//item that is on center playing a video
    private boolean mLoading;

    boolean locked = false;

    public VideoRecyclerView(@NonNull Context context, EasyPlayer player)
    {
        super(context);


        mLayoutManager = new LinearLayoutManager(context);
        setLayoutManager(mLayoutManager);

        //injection of mPlayer
        mPlayer = player;
        //twitter search get a listener for response, which this recyclerview implements
        mTwitterSearch = new TwitterSearch(context, this);
    }

    /** calculates how much the last visible row is near to the last item of the adapter
     *  calculates the row that is closer to the center of the recyclerview
     * */
    @Override
    public void onScrolled(int dx, int dy)
    {
        super.onScrolled(dx, dy);

        //calculate if the last item(plus a offset) was reached
        int totalItemCount = mLayoutManager.getItemCount();
        int lastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
        //is not loading already, is in the range to loading and vertical velocity is greater than zero
        if (!mLoading && totalItemCount <= (lastVisibleItem + mEagerItemLoading) && dy>0) {

            //get the next results using pagination
            getTweetsAsVideos("");
        }

        //prevent change row while screen is rotating
        if(!locked)
            updateRowPlaying();
    }

    public void updateRowPlaying()
    {
        //if is scrolling (add or remove items from recyclerview trigger this event) get the item in the center
        int newRow = getRowIndexInCenter();
        //and current row to play the designed video
        setRowPlaying(newRow);
    }

    VideoAdapter getVideoAdapter()
    {
        return (VideoAdapter) getAdapter();
    }

    /** get the next page of results */
    public void getTweetsAsVideos(String query)
    {
        //mark the recycler and mAdapter as loading state
        mLoading = true;
        getVideoAdapter().setLoading(mLoading);

        //get tweets
        mTwitterSearch.getTweetsAsVideos(query);
    }

    /** implementation of the callback of the twitt search */
    @Override
    public void onResponse(final List<VideoModel> result, final int status)
    {
        //mark the recycler and mAdapter as ready state
        mLoading = false;
        getVideoAdapter().setLoading(mLoading);

        //if the response is OK add new items to mAdapter
        //todo handle other responses
        if(status != ConstantHolder.TWITTER_RESPONSE_OK)
        {
            getVideoAdapter().add(result);
        }
    }


    /**
     *  get the row (mAdapter row) with a center nearest to the center of the reciclerview
     i decided to get the center related to list instead of the screen, since the recyclerview can be placed in any place of the screen
     */
    private int getRowIndexInCenter()
    {
        int childCount = getChildCount();
        int minDistance = getHeight();//initialize with the bottom of the recyclerview
        int centerOfGroup = minDistance/2;
        View rowToPaint = null;

        //iterate over the visible rows
        for(int i = 0; i<childCount; i++)
        {
            View child = getChildAt(i);

            //check if it's a loading view holder, which can not be counted
            RecyclerView.ViewHolder holder = getChildViewHolder(child);
            if(holder instanceof LoadingViewHolder)
                continue;
            //get global size and position of the position
            Rect offsetViewBounds = new Rect();
            child.getDrawingRect(offsetViewBounds);
            //get the relative size and position
            offsetDescendantRectToMyCoords(child, offsetViewBounds);

            //center of the wor related to parent(reciclerview)
            int centerOfRow = offsetViewBounds.top+(offsetViewBounds.height()/2);

            //if the child is closer to the center
            int currentDistance = Math.abs(centerOfGroup - centerOfRow);
            if( currentDistance < minDistance)
            {
                minDistance = currentDistance;
                rowToPaint = child;
            }

        }


        //return the non null row closer to the center
        if(rowToPaint != null)
            return getChildViewHolder(rowToPaint).getAdapterPosition();
        else return -1;
    }

    /** just get the index of the current row in center */
    public int getCurrentIndexRow()
    {
        return mCurrentIndexRowPlaying;
    }

    /** just get the current model itself */
    public VideoModel getCurrentItem()
    {
        return items.get(mCurrentIndexRowPlaying);
    }

    /** set a new row to play his video
     * make sure to remove the video from the last row and save his state
     *  */
    private void setRowPlaying(int newRow)
    {
        //new row is valid and really new
        if(newRow >= 0 && newRow != mCurrentIndexRowPlaying)
        {
            VideoViewHolder lastHolder = (VideoViewHolder) findViewHolderForAdapterPosition(mCurrentIndexRowPlaying);
            VideoViewHolder newHolder = (VideoViewHolder) findViewHolderForAdapterPosition(newRow);
            //remove the mPlayer fom last row
            if(lastHolder != null)
            {
                VideoModel model = items.get(mCurrentIndexRowPlaying);

                //save last mPlayer position
                model.position = mPlayer.getPosition();
                mPlayer.pause();
                mPlayer.clearPlayerView(lastHolder.playerView);
            }

            //add mPlayer to the new row
            if(newHolder != null)
            {
                //update the current row
                mCurrentIndexRowPlaying = newRow;
                //prepare and play the current row
                //I decided to re-prepare the video here, instead of in bind of the holder, due to a bug in exoplayer #Issue 677
                mPlayer.prepare(getCurrentItem());
                mPlayer.playOn(newHolder.playerView, getCurrentItem().position);
            }
        }
    }

}
