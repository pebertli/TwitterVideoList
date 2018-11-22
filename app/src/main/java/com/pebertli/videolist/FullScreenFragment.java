/*
 * *
 *  * Created by Pebertli Barata on 9/16/18 10:30 AM
 *  * Copyright (c) 2018 . All rights reserved.
 *  * Last modified 9/15/18 7:53 PM
 *
 */

package com.pebertli.videolist;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.google.android.exoplayer2.ui.PlayerView;

/**
 * Fragment to show the full screen video
 * Maintain his own PlayerView
 */
public class FullScreenFragment extends Fragment
{
    VideoModel video;
    EasyPlayer player;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_full_screen, container, false);

        PlayerView playerView = view.findViewById(R.id.playerFull);

        Bundle bundle = getArguments();
        boolean reload;

        view.setVisibility(View.VISIBLE);

        //as Android does not recommend constructor for fragments, I could not do dependency injection
        if(bundle!=null)
        {
            video = (VideoModel) bundle.getSerializable("video");
            player = (EasyPlayer) bundle.getSerializable("mPlayer");
            reload = bundle.getBoolean("reload", false);

            //video is valid
            if (player != null && video != null)
            {
                //if the video is from a different row that was playing, in other word, should re-prepare
                if (reload)
                {
                    player.prepare(video);
                    player.playOn(playerView, video.position);
                } else
                    player.playOn(playerView);
            }
        }

        ImageButton closeButton = view.findViewById(R.id.closeVideoButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //delegate the close procedure to activity
                getActivity().onBackPressed();
            }
        });

        return view;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

}
