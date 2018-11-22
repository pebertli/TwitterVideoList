/*
 * *
 *  * Created by Pebertli Barata on 9/16/18 11:01 AM
 *  * Copyright (c) 2018 . All rights reserved.
 *  * Last modified 9/15/18 9:53 AM
 *
 */

package com.pebertli.videolist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Model for video holder and simplification of the rest api, despite the use of the Twitter Core
 * Made this Serializable for fragment communication
 */
public class VideoModel implements Serializable
{
    List<VideoVariant> variants = new ArrayList<>();
    String uriThumbnail;
    Long position;

    public VideoModel(List<VideoVariant> variants, String uriThumbnail)
    {
        this.variants = variants;
        this.uriThumbnail = uriThumbnail;
        this.position = 0L;
    }

    VideoVariant getMinQuality()
    {
        long lowest = 0;
        VideoVariant result = variants.get(0);
        for (int i = 1 ; i<variants.size();i++)
        {
            VideoVariant v =variants.get(i);
            if(v.bitrate < lowest)
            {
                lowest = v.bitrate;
                result = v;
            }
        }

        return result;
    }

    VideoVariant getMaxQuality()
    {
        long highest = 0;
        VideoVariant result = variants.get(0);
        for (int i = 1 ; i<variants.size();i++)
        {
            VideoVariant v =variants.get(i);
            if(v.bitrate > highest)
            {
                highest = v.bitrate;
                result = v;
            }
        }

        return result;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VideoModel that = (VideoModel) o;
        return Objects.equals(uriThumbnail, that.uriThumbnail);
    }

    @Override
    public int hashCode()
    {

        return Objects.hash(uriThumbnail);
    }
}

class VideoVariant implements Serializable
{
    String uriVideo;
    int contentType; //avoiding using enum //0 mp4, 1 HLS
    long bitrate;

    public VideoVariant(String uriVideo, int contenType, long bitrate)
    {
        this.uriVideo = uriVideo;
        this.contentType = contenType;
        this.bitrate = bitrate;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VideoVariant that = (VideoVariant) o;
        return contentType == that.contentType &&
                Objects.equals(uriVideo, that.uriVideo);
    }

    @Override
    public int hashCode()
    {

        return Objects.hash(uriVideo, contentType);
    }
}