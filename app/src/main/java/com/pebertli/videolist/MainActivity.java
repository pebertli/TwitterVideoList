/*
 * *
 *  * Created by Pebertli Barata on 9/16/18 10:33 AM
 *  * Copyright (c) 2018 . All rights reserved.
 *  * Last modified 9/16/18 10:33 AM
 *
 */

package com.pebertli.videolist;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

/**
 * Acitivity to show the recicler view, search field and the full screen video
 * implements the recyclerview item click as a delegate, since it will call a fragment from this very same activity
 */
public class MainActivity extends FragmentActivity implements VideoAdapter.OnItemClickListener
{
    private VideoRecyclerView mRecyclerView;
    private FullScreenFragment mVideoFragment;
    private EasyPlayer mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //prevent keyboard to appear on start
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        mPlayer = new EasyPlayer(this);

        //mPlayer is passed by dependency injection, so is necessary to manual add the recyclerview to layout
        mRecyclerView = new VideoRecyclerView(this, mPlayer);
        LinearLayout layout = findViewById(R.id.recyclerViewLayout);
        mRecyclerView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        mRecyclerView.setHasFixedSize(true);
        layout.addView(mRecyclerView);

        //click listener for button
        final ImageButton searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //hide keyboard after button click
                InputMethodManager imm = (InputMethodManager)view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                searchTweets();
            }
        });

        final EditText searchTerm = findViewById(R.id.searchTerm);
        searchTerm.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent)
            {
                if (i == EditorInfo.IME_ACTION_SEARCH) {
                    //hide keyboard after search keyboard click
                    InputMethodManager imm = (InputMethodManager)textView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
                    searchTweets();

                    return true;
                }

                return false;
            }
        });

        final Switch switchQuality = findViewById(R.id.switchQuality);
        switchQuality.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b)
            {
                mPlayer.bestQuality = b;
            }
        });

        //create one fragment and reuse it
        mVideoFragment = new FullScreenFragment();

        //Video mAdapter needs a listener for click on item
        VideoAdapter mAdapter = new VideoAdapter(mRecyclerView.items, this);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        //basic state management
        mPlayer.pause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        mPlayer.play();
    }

    /** search tweets based on the edittext field, cleaning the previous rows*/
    private void searchTweets()
    {
        String query = ((EditText)findViewById(R.id.searchTerm)).getText().toString();
        //remove the previous search and get another one, if is a valid mQuery
        if(!query.isEmpty())
        {
            mPlayer.pause();
            mRecyclerView.getVideoAdapter().clear();
            mRecyclerView.getTweetsAsVideos(query);
        }
    }

    /** handle the onbackpressed to know if the fullscreen video is on stack */
    @Override
    public void onBackPressed()
    {
        FragmentManager fm = getSupportFragmentManager();
        if(fm.getBackStackEntryCount() > 0)//there is video full screen
        {
            //save the video that was playing on fragment
            mVideoFragment.video.position = mPlayer.getPosition();
            VideoViewHolder holder = (VideoViewHolder) mRecyclerView.findViewHolderForAdapterPosition(mRecyclerView.getCurrentIndexRow());
            if(holder != null)
            {
                //if the video that was playing on fragment is different from the current on recyclerview
                if(!mRecyclerView.getCurrentItem().equals(mVideoFragment.video))
                {
                    mPlayer.prepare(mRecyclerView.getCurrentItem());
                    mPlayer.playOn(holder.playerView, mRecyclerView.getCurrentItem().position);
                }
                else
                    mPlayer.playOn(holder.playerView);

            }
            mRecyclerView.locked = false;
            mRecyclerView.updateRowPlaying();
            //remove from top
            fm.popBackStack();
        }
        else
        {
            //by default, android will finish activity
            super.onBackPressed();
        }

    }

    /** implementation of the interface of clicking in the reciclerview
     * calls the full screen fragment, whether is the current playing row or not
     * */
    @Override
    public void onItemClick(VideoModel item)
    {
        //a custom call to fragment
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_right);

        mRecyclerView.locked = true;
        //sending information to fragment
        Bundle b = new Bundle();
        b.putSerializable("video", item);
        b.putSerializable("mPlayer", mPlayer);
        //if the clicked item is diferent of that is playing, then save the current position
        if(mRecyclerView.getCurrentItem() != item)
        {
            mPlayer.pause();
            mRecyclerView.getCurrentItem().position = mPlayer.getPosition();
            //and make the fragment reload the source, since it is a different video from that it's playing
            b.putBoolean("reload", true);
        }
        mVideoFragment.setArguments(b);

        //put the fragment on top
        ft.replace(R.id.fragment_content, mVideoFragment);
        ft.addToBackStack(null);
        ft.commit();
    }
}
