/*
 * *
 *  * Created by Pebertli Barata on 9/16/18 10:39 AM
 *  * Copyright (c) 2018 . All rights reserved.
 *  * Last modified 9/16/18 9:22 AM
 *
 */

package com.pebertli.videolist;

import android.content.Context;

import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.models.Search;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.SearchService;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Facade for the Twitter Search Service
 * define a callback interface for the Rest API response
 */
public class TwitterSearch
{
    /** callback interface with the twitters in a simple model */
    public interface OnTwitterResponseListener
    {
        void onResponse(List<VideoModel> result , int status);
    }

    //
    private String mFilter = ConstantHolder.DEFAULT_TWITTER_FILTER;
    private String mQuery = ConstantHolder.DEFAULT_TWITTER_QUERY;
    private int mPageSize = ConstantHolder.TWITTER_PAGE_SIZE;
    private Long mMaxId = null; //initialize with because the first page doesn't have mMaxId

    private final SearchService mSearchService;

    private final OnTwitterResponseListener mListener;

    public TwitterSearch(Context context, OnTwitterResponseListener listener)
    {
        //Twitter Core handle the Bearer store and recreation in case of invalidation
        Twitter.initialize(context);
        TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
        mSearchService = twitterApiClient.getSearchService();
        mListener = listener;
    }

    /** get tweets with a query in the post and a video that is not a retweet*/
    public void getTweetsAsVideos(String query)
    {
        //if is empty uses the mQuery that is already saved
        if(!query.isEmpty())
            this.mQuery = query;
        final List<VideoModel> result = new ArrayList<>();
        //encode mQuery to reinterpret symbols as hashtag and comma
        String encodedQuery = this.mQuery;
        try
        {
            encodedQuery = URLEncoder.encode(this.mQuery, "utf-8");
        } catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }

        Call<Search> call = mSearchService.tweets(encodedQuery + mFilter, null, null,null , "recent", mPageSize, null, null, mMaxId, true);
        call.enqueue(new Callback<Search>() {
            @Override
            public void onResponse(Call<Search> call, Response<Search> response)
            {

                //means that the HTTP code is a valid response, such as 200
                if(response.isSuccessful())
                {
                    //iterate over tweets returned
                    for(int i = 0; i< response.body().tweets.size();i++)
                    {
                        //the first tweet of the pages (except the first page) should be discarded,
                        //since the search method is inclusive, so the mMaxId is the last tweet of the last request
                        //and is returned as the first tweet of the current request
                        //I decided to use mMaxId instead of next_query since Twitter API seems to be strange when request recent posts
                        if(i == 0 && mMaxId != null)
                            continue;

                        Tweet t = response.body().tweets.get(i);
                        //make sure that there is a video on the post
                        if(t.extendedEntities.media.isEmpty() || t.extendedEntities.media.get(0).videoInfo.variants.isEmpty())
                            continue;
                        //give preference to HLS videos for better performance
                        int variantAmount = t.extendedEntities.media.get(0).videoInfo.variants.size();
                        //String contentType = t.extendedEntities.media.get(0).videoInfo.variants.get(0).contentType;
                        String urlThumb = t.extendedEntities.media.get(0).mediaUrl;

                        List<VideoVariant> listVariant = new ArrayList<>();
                        for(int v = 0; v<variantAmount; v++)
                        {
                            long bitrate = t.extendedEntities.media.get(0).videoInfo.variants.get(v).bitrate;
                            String urlVideo = t.extendedEntities.media.get(0).videoInfo.variants.get(v).url;
                            String contentTypestr = t.extendedEntities.media.get(0).videoInfo.variants.get(v).contentType;
                            int contentType = contentTypestr.equals("application/x-mpegURL") ? 1 :0;
                            listVariant.add(new VideoVariant(urlVideo, contentType, bitrate));
                        }
                        //add to result
                        result.add(new VideoModel(listVariant, urlThumb));
                        //if is the last tweet of the request, save the id for a possible next request in mMaxId
                        if(i == response.body().tweets.size()-1)
                            mMaxId = response.body().tweets.get(i).id;
                    }

                    //sinalize the listener that a response was returned
                    mListener.onResponse(result, ConstantHolder.TWITTER_RESPONSE_OK);
                }
                //sinalize the listener that a response was returned
                mListener.onResponse(result, ConstantHolder.TWITTER_RESPONSE_FAIL);
            }

            @Override
            public void onFailure(Call<Search> call, Throwable t)
            {
                //sinalize the listener that a response was returned
                mListener.onResponse(result, ConstantHolder.TWITTER_RESPONSE_FAIL);
            }


        });
    }
}
