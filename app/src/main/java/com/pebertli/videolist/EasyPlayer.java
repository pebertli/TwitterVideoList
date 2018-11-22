/*
 * *
 *  * Created by Pebertli Barata on 9/15/18 8:08 PM
 *  * Copyright (c) 2018 . All rights reserved.
 *  * Last modified 9/15/18 8:06 PM
 *
 */

package com.pebertli.videolist;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.view.SurfaceView;
import android.view.TextureView;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.Serializable;

/**
 * Facade for the exoplayer
 * Made this serializable for Fragment communication
 */
public class EasyPlayer implements Serializable
{
    private final SimpleExoPlayer mExoPlayer;
    private final DataSource.Factory mDataSourceFactory;

    boolean bestQuality;

    public EasyPlayer(Context context)
    {
        //Default creation of mPlayer with repeat mode
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        mExoPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
        mExoPlayer.setRepeatMode(Player.REPEAT_MODE_ALL);

        //one data source factory
        mDataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, context.getString(R.string.app_name)));
        //mDataSourceFactory = new CacheDataSourceFactory(context, 100 * 1024 * 1024, 5 * 1024 * 1024);
    }

    /** just pause */
    public void pause()
    {
        mExoPlayer.setPlayWhenReady(false);
    }

    /** just play/resume, if ready */
    public void play()
    {
        mExoPlayer.setPlayWhenReady(true);
    }

    /** switch the playerview of the Player, setting a timeline position
     * */
    public void playOn(PlayerView playerView, Long position)
    {
        //change the playerview of the exoplayer
        mExoPlayer.setVideoTextureView((TextureView) playerView.getVideoSurfaceView());
        playerView.setPlayer(mExoPlayer);
        //and reposition the timeline
        mExoPlayer.seekTo(position);
        mExoPlayer.setPlayWhenReady(true);
    }

    /** switch the playerview of the Player
     * */
    public void playOn(PlayerView playerView)
    {
        //change the playerview of the exoplayer
        mExoPlayer.setVideoTextureView((TextureView) playerView.getVideoSurfaceView());
        playerView.setPlayer(mExoPlayer);
        mExoPlayer.setPlayWhenReady(true);
    }

    /** just get the timeline position */
    public Long getPosition()
    {
        return mExoPlayer.getContentPosition();
    }

    /** recreate the proper source with the proper Factory, and prepare the player*/
    public void prepare(VideoModel video)
    {
        //regenerate video content
        MediaSource videoSource;

        VideoVariant v;
        if(bestQuality)
            v = video.getMaxQuality();
        else
            v = video.getMinQuality();

        //chunckless preparation improves loading
        if (v.contentType == ConstantHolder.CONTENT_VIDEO_HLS)
            videoSource = new HlsMediaSource.Factory(mDataSourceFactory).setAllowChunklessPreparation(true).createMediaSource(Uri.parse(v.uriVideo));
        else
            videoSource = new ExtractorMediaSource.Factory(mDataSourceFactory).createMediaSource(Uri.parse(v.uriVideo));
        mExoPlayer.prepare(videoSource);
    }

    /** remove the player from the current playerview */
    public void clearPlayerView(PlayerView playerView)
    {
        mExoPlayer.clearVideoTextureView((TextureView) playerView.getVideoSurfaceView());
        playerView.setPlayer(null);
    }

}
