/*
 * *
 *  * Created by Pebertli Barata on 9/16/18 10:50 AM
 *  * Copyright (c) 2018 . All rights reserved.
 *  * Last modified 9/16/18 10:50 AM
 *
 */

package com.pebertli.videolist;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.exoplayer2.ui.PlayerView;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Holder pattern for the Video model
 * bind a thumbnail to the foreground of the playerview (cached with Picasso)
 * Delegates the on click callback
 */
class VideoViewHolder extends RecyclerView.ViewHolder
{
    final PlayerView playerView;
    final ImageView thumbnailView;
    Uri uriThumbnail;

    public VideoViewHolder(View v)
    {
        super(v);
        playerView = itemView.findViewById(R.id.player);
        thumbnailView = itemView.findViewById(R.id.exo_shutter);
    }

    /** bind a thumbnail to the foreground of the playerview (cached with Picasso) */
    public void bind(final VideoModel item, final VideoAdapter.OnItemClickListener listener)
    {
        uriThumbnail = Uri.parse(item.uriThumbnail);

        Picasso.with(itemView.getContext()).load(uriThumbnail).into(thumbnailView);
        itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                listener.onItemClick(item);
            }
        });
    }
}

/**
 * Holder pattern for the progress bar which doesn't need a model
 */
class LoadingViewHolder extends RecyclerView.ViewHolder
{
    final ProgressBar progressBar;

    public LoadingViewHolder(View v)
    {
        super(v);
        progressBar = v.findViewById(R.id.progressBar);
    }
}

/**
 * Adapter for the recyclerview
 * Handle the progressbar and modifications on items
 */
public class VideoAdapter extends RecyclerView.Adapter
{

    public interface OnItemClickListener
    {
        void onItemClick(VideoModel item);
    }

    private final List<VideoModel> mItems;
    private final OnItemClickListener mListener;


    public VideoAdapter(List<VideoModel> items, OnItemClickListener listener)
    {
        mItems = items;
        mListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType)
    {
        RecyclerView.ViewHolder holder;
        if (viewType == ConstantHolder.ROW_VIDEO_TYPE_BASIC)
        {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.video_row, viewGroup, false);
            holder = new VideoViewHolder(v);
        }
        else {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.loading_row, viewGroup, false);
            holder = new LoadingViewHolder(v);
        }


        return holder;
    }

    @Override
    public int getItemViewType(int position)
    {
        return mItems.get(position) != null ? ConstantHolder.ROW_VIDEO_TYPE_BASIC : ConstantHolder.ROW_VIDEO_TYPE_LOAD;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int i)
    {
        if (holder instanceof VideoViewHolder)
        {
            ((VideoViewHolder) holder).bind(mItems.get(i), mListener);
        }
    }

    @Override
    public int getItemCount()
    {
        return mItems == null ? 0: mItems.size();
    }

    /** add or remove the progress bar */
    public void setLoading(boolean loading)
    {
        //the last item is the progress bar item
        if(!mItems.isEmpty() && mItems.get(mItems.size()-1) == null && !loading)
        {
            mItems.remove(mItems.size()-1);
            notifyItemRemoved(mItems.size());
        }
        else if(loading)
        {
            //add the progress bar to the end
            mItems.add(null);
            notifyItemInserted(mItems.size() - 1);
        }
    }

    /** clear the list */
    public void clear()
    {
        mItems.clear();
        notifyDataSetChanged();
    }

    /** add new items to the end of the list */
    public void add(final List<VideoModel> newItems)
    {
        if(newItems.isEmpty())
            return;

        int position = mItems.size();
        mItems.addAll(newItems);
        if(position == 0)
            notifyDataSetChanged();
        else
            notifyItemRangeInserted(position, mItems.size());

    }


}
