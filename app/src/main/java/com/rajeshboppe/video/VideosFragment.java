package com.rajeshboppe.video;

import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;

import java.util.ArrayList;
import java.util.List;

public class VideosFragment extends Fragment implements ExoPlayer.EventListener {

    private VideoAdapter mVideoAdapter;

    private SimpleExoPlayerView mSimpleExoPlayerView;
    private SimpleExoPlayer mSimpleExoPlayer;
    private boolean isFullScreen = false;
    private ImageView mFullScreenButton;

    private int mCurrent = 0;

    private List<Video> mVideos = new ArrayList<>();

    private final String[] VIDEO_TITLES = {"Clash Of Clans - Goodbye builder", "Clash Of Clans - Comeback builder",
            "How Do We Get There", "Inside The Clans Castle"};
    private final String[] VIDEO_URLS = {"https://coinage.in/ClashOfClans/clashofclans_byebyebuilder.mp4",
            "https://coinage.in/ClashOfClans/clashofclans_combackbuilder.mp4",
            "https://coinage.in/ClashOfClans/clashofclans_howdo_wegetoverthere.mp4",
            "https://coinage.in/ClashOfClans/insidetheclancastle.mp4"};

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeData();
    }

    private void initializeData() {
        for (int i = 0; i < VIDEO_TITLES.length; i++) {
            Video video = new Video();
            video.setTitle(VIDEO_TITLES[i]);
            video.setUrl(VIDEO_URLS[i]);
            mVideos.add(video);
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_videos, container, false);

        mSimpleExoPlayerView = root.findViewById(R.id.exo_player_view);
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelector trackSelector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(bandwidthMeter));
        mSimpleExoPlayer = ExoPlayerFactory.newSimpleInstance(getActivity(), trackSelector);
        mSimpleExoPlayer.addListener(this);

        playNext();

        mFullScreenButton = mSimpleExoPlayerView.findViewById(R.id.exo_fullscreen_icon);
        mFullScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                assert getContext() != null;
                assert getActivity() != null;
                assert getActivity().getWindow() != null;

                ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

                if (isFullScreen) {
                    mFullScreenButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_fullscreen_24));
                    getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    if (actionBar != null) {
                        actionBar.show();
                    }
                    ((MainActivity) getActivity()).showBottomBar();
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mSimpleExoPlayerView.getLayoutParams();
                    params.width = params.MATCH_PARENT;
                    params.height = (int) (230 * getActivity().getResources().getDisplayMetrics().density);
                    mSimpleExoPlayerView.setLayoutParams(params);
                    isFullScreen = false;
                } else {
                    mFullScreenButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_fullscreen_exit_24));
                    getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                    if (actionBar != null) {
                        actionBar.hide();
                    }
                    ((MainActivity) getActivity()).hideBottomBar();
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mSimpleExoPlayerView.getLayoutParams();
                    params.width = params.MATCH_PARENT;
                    params.height = params.MATCH_PARENT;
                    mSimpleExoPlayerView.setLayoutParams(params);
                    isFullScreen = true;
                }
            }
        });

        RecyclerView recyclerView = root.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        mVideoAdapter = new VideoAdapter();
        mVideoAdapter.setData(mVideos);
        recyclerView.setAdapter(mVideoAdapter);

        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(itemDecoration);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mSimpleExoPlayer != null) {
            mSimpleExoPlayer.setPlayWhenReady(true);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mSimpleExoPlayer != null) {
            mSimpleExoPlayer.setPlayWhenReady(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSimpleExoPlayer != null) {
            mSimpleExoPlayer.setPlayWhenReady(false);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (mSimpleExoPlayer != null) {
            mSimpleExoPlayer.setPlayWhenReady(true);
        }
    }

    private void playNext() {
        try {
            DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory("exoplayer_video");
            ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
            MediaSource mediaSource1 = new ExtractorMediaSource(Uri.parse(VIDEO_URLS[0]),
                    dataSourceFactory, extractorsFactory, null, null);
            MediaSource mediaSource2 = new ExtractorMediaSource(Uri.parse(VIDEO_URLS[1]),
                    dataSourceFactory, extractorsFactory, null, null);
            MediaSource mediaSource3 = new ExtractorMediaSource(Uri.parse(VIDEO_URLS[2]),
                    dataSourceFactory, extractorsFactory, null, null);
            MediaSource mediaSource4 = new ExtractorMediaSource(Uri.parse(VIDEO_URLS[3]),
                    dataSourceFactory, extractorsFactory, null, null);

            ConcatenatingMediaSource concatenatedSource =
                    new ConcatenatingMediaSource(mediaSource1, mediaSource2, mediaSource3, mediaSource4);

            mSimpleExoPlayerView.setPlayer(mSimpleExoPlayer);
            mSimpleExoPlayer.prepare(concatenatedSource);
            mSimpleExoPlayer.setPlayWhenReady(true);
            mCurrent++;
        } catch (Exception e) {
            Log.e("MainAcvtivity", " exoplayer error " + e.toString());
        }
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

        if (playWhenReady && playbackState == Player.STATE_READY) {
            Log.d("TAG", "onPlayerStateChanged: " + mSimpleExoPlayer.getCurrentPeriodIndex());
            for (int i = 0; i < mVideos.size(); i++)
                mVideos.get(i).setCurrent(mSimpleExoPlayer.getCurrentPeriodIndex() == i);
            mVideoAdapter.setData(mVideos);
        } else if (playbackState == Player.STATE_ENDED) {
            Log.d("TAG", "Video ended");
            playNext();
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {

    }

}