/*
 * *
 *  * Created by Pebertli Barata on 9/16/18 10:28 AM
 *  * Copyright (c) 2018 . All rights reserved.
 *  * Last modified 9/15/18 12:02 PM
 *
 */

package com.pebertli.videolist;

/**
 * static Constant set
 * Android doesn't recommend enumerators due to increase of the dex size
 */
public class ConstantHolder
{
    //ViewHolder Type
    public static final int ROW_VIDEO_TYPE_BASIC = 0;
    public static final int ROW_VIDEO_TYPE_LOAD = 1;

    //Content Type
    public static final int CONTENT_VIDEO_MP4 = 0;
    public static final int CONTENT_VIDEO_HLS = 1;

    //Twitter
    public static final int TWITTER_RESPONSE_OK = 0;
    public static final int TWITTER_RESPONSE_FAIL = 1;
    public static final String DEFAULT_TWITTER_FILTER = " filter:consumer_video -filter:nativeretweets"; //search for videos on tweet posts that aren't retweets
    public static final String DEFAULT_TWITTER_QUERY = " #dogsoftwitter";
    public static final int TWITTER_PAGE_SIZE = 15;

}
