/*
 * *
 *  * Created by Pebertli Barata on 9/16/18 12:32 PM
 *  * Copyright (c) 2018 . All rights reserved.
 *  * Last modified 9/16/18 12:32 PM
 *
 */

package com.pebertli.videolist;


import android.content.Context;

import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSink;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;

import java.io.File;

/**
 * Factory for a cache of media sources
 * Still can't do a properly multiple files cache
 */
class CacheDataSourceFactory  implements DataSource.Factory
{

    private int mCacheIndex = 0;
    private final Context mContext;
    private final DefaultDataSourceFactory mDfaultDatasourceFactory;
    private final long mMaxFileSize;
    private final long mMaxCacheSize;

    public CacheDataSourceFactory(Context context, long maxCacheSize, long maxFileSize)
    {
        super();
        mContext = context;
        mMaxCacheSize = maxCacheSize;
        mMaxFileSize = maxFileSize;
        String userAgent = Util.getUserAgent(context, context.getString(R.string.app_name));
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        mDfaultDatasourceFactory = new DefaultDataSourceFactory(context, bandwidthMeter, new DefaultHttpDataSourceFactory(userAgent, bandwidthMeter));

    }



    @Override
    public DataSource createDataSource() {

        LeastRecentlyUsedCacheEvictor evictor = new LeastRecentlyUsedCacheEvictor(mMaxCacheSize);
        SimpleCache simpleCache = new SimpleCache(new File(mContext.getCacheDir(), "media"+ mCacheIndex++), evictor);

        return new CacheDataSource(simpleCache, mDfaultDatasourceFactory.createDataSource(),
                new FileDataSource(),
                new CacheDataSink(simpleCache, mMaxFileSize),CacheDataSource.FLAG_BLOCK_ON_CACHE | CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR, null);
    }

}
