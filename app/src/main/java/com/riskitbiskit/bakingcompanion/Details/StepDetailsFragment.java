package com.riskitbiskit.bakingcompanion.Details;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.riskitbiskit.bakingcompanion.Main.MainActivity;
import com.riskitbiskit.bakingcompanion.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StepDetailsFragment extends Fragment {

    //Constants
    public static final String VOLLEY_TAG = "volley_tag";
    public static final String LIST_INDEX = "index";
    public static final String INSTRUCTIONS_LIST = "instructions";
    public static final String PLAYER_POSITION = "playerPosition";

    //Fields
    private SimpleExoPlayer mExoPlayer;
    private RequestQueue mRequestQueue;
    private int recipeNumber;
    private int requestedStep;
    private boolean isTwoPanel;
    private long playerPosition;
    List<Instructions> mInstructions;

    //Views
    @BindView(R.id.exoplayer)
    SimpleExoPlayerView mSimpleExoPlayerView;
    @BindView(R.id.previous_button)
    Button mPreviousBt;
    @BindView(R.id.next_button)
    Button mNextBt;
    @BindView(R.id.step_instructions_frag_tv)
    TextView mStepTV;
    @BindView(R.id.exoplayer_replacement_thumbnail)
    ImageView mThumbnailIV;

    //constructor
    public StepDetailsFragment () {}

    //setter methods
    public void setRecipeNumber(int recipeNumber) {
        this.recipeNumber = recipeNumber;
    }

    public void setRequestedStep(int requestedStep) {
        this.requestedStep = requestedStep;
    }

    public void setIsTwoPanel(boolean isTwoPanel) {
        this.isTwoPanel = isTwoPanel;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //inflate layout
        View rootView = inflater.inflate(R.layout.step_details_frag, container, false);

        //bind views
        ButterKnife.bind(this, rootView);

        //initialize variables
        mInstructions = new ArrayList<>();

        //grab data from saved state and apply
        Intent intent = getActivity().getIntent();

        //check to see if device should display two panel or single panel
        if (isTwoPanel) {
            if (savedInstanceState != null) {
                reuseSavedData(savedInstanceState);
            } else {
                makeVolleyRequest();
            }
        } else {
            if (savedInstanceState != null) {
                reuseSavedData(savedInstanceState);
            } else {
                recipeNumber = intent.getIntExtra(MainActivity.RECIPE_INDEX_NUMBER, 0);
                requestedStep = intent.getIntExtra(RecipeDetails.INSTRUCTION_STEP, 0);
                makeVolleyRequest();
            }
        }

        mNextBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //clear exoplayer of current data
                releasePlayer();
                //increment to the next step
                requestedStep++;
                fullSetup();
            }
        });

        mPreviousBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //clear exoplayer of current data
                releasePlayer();
                //increment to the next step
                requestedStep--;
                fullSetup();
            }
        });

        return rootView;
    }

    private void reuseSavedData(Bundle savedInstanceState) {
        //retrieved saved data
        requestedStep = savedInstanceState.getInt(LIST_INDEX);
        mInstructions = savedInstanceState.getParcelableArrayList(INSTRUCTIONS_LIST);
        playerPosition = savedInstanceState.getLong(PLAYER_POSITION);

        //setup views
        fullSetup();
    }

    private void makeVolleyRequest() {
        mRequestQueue = Volley.newRequestQueue(getContext());

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, MainActivity.DATA_URL, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    parseJsonData(response);
                    fullSetup();

                } catch (JSONException JSONE) {
                    JSONE.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("TAG", error + "");
            }
        });

        jsonArrayRequest.setTag(VOLLEY_TAG);

        mRequestQueue.add(jsonArrayRequest);
    }

    private void parseJsonData(JSONArray response) throws JSONException {
        //get reference to root node
        JSONObject currentRecipe = response.getJSONObject(recipeNumber);

        //grab requested instruction
        JSONArray requestedInstructions = currentRecipe.getJSONArray("steps");

        //cycle through each step
        for (int i = 0; i < requestedInstructions.length(); i++) {
            //extract relevant data from each step
            JSONObject currentStep = requestedInstructions.getJSONObject(i);
            int id = currentStep.getInt("id");
            String shortDescription = currentStep.getString("shortDescription");
            String description = currentStep.getString("description");
            String videoUrl = currentStep.getString("videoURL");
            String thumbnail = currentStep.getString("thumbnailURL");

            //add to list of instructions
            mInstructions.add(new Instructions(id, shortDescription, description, videoUrl, thumbnail));
        }
    }

    private void initializePlayer(Uri mediaUri) {
        if (mExoPlayer == null) {
            // Create an instance of the ExoPlayer.
            TrackSelector trackSelector = new DefaultTrackSelector();
            LoadControl loadControl = new DefaultLoadControl();
            mExoPlayer = ExoPlayerFactory.newSimpleInstance(getContext(), trackSelector, loadControl);
            mSimpleExoPlayerView.setPlayer(mExoPlayer);

            // Set the ExoPlayer.EventListener to this activity.
            mExoPlayer.addListener(new ExoPlayerListener());

            // Prepare the MediaSource.
            String userAgent = Util.getUserAgent(getContext(), "StrangeBakerThings");
            MediaSource mediaSource = new ExtractorMediaSource(mediaUri, new DefaultDataSourceFactory(
                    getContext(), userAgent), new DefaultExtractorsFactory(), null, null);
            mExoPlayer.prepare(mediaSource);

            if (playerPosition != 0L) {
                mExoPlayer.seekTo(playerPosition);
            }

            mExoPlayer.setPlayWhenReady(true);
        }
    }

    private void fullSetup() {
        addOrRemoveButtons();
        setupExoPlayer();
        setupInstructions();
    }

    //method for setting up add or remove buttons depending on step
    private void addOrRemoveButtons() {

        //Add or remove previous button
        if (requestedStep == 0) {
            mPreviousBt.setVisibility(View.GONE);
        } else {
            mPreviousBt.setVisibility(View.VISIBLE);
        }

        //Add or remove next button
        if (mInstructions.size() -1 == requestedStep) {
            mNextBt.setVisibility(View.GONE);
        } else {
            mNextBt.setVisibility(View.VISIBLE);
        }
    }

    //method for setting up exoplayer
    private void setupExoPlayer() {
        //check to see if there is a videoUrl
        if (!mInstructions.get(requestedStep).getVideoUrl().equals("")) {

            //check to see that there is a reference to the image view
            if (mThumbnailIV != null) {
                //set to invisible, since there is a video available
                mThumbnailIV.setVisibility(View.INVISIBLE);
            }

            //make exoplayer view visible so that it can play video
            mSimpleExoPlayerView.setVisibility(View.VISIBLE);
            //start playing video
            initializePlayer(Uri.parse(mInstructions.get(requestedStep).getVideoUrl()));

        //check to see if there's a thumbnail instead
        } else if (!mInstructions.get(requestedStep).getThumbnailUrl().equals("")) {

            //check to see if that thumbnail is actually a video
            if (mInstructions.get(requestedStep).getThumbnailUrl().contains(".mp4")) {
                //if thumbnail is a video, we're going to convert it to an image so make imageview
                //visible
                if (mThumbnailIV != null) {
                    mThumbnailIV.setVisibility(View.INVISIBLE);
                }
                //make exoplay invisible
                mSimpleExoPlayerView.setVisibility(View.VISIBLE);
                //use showThumbnail method to show the thumbnail
                showThumbnail(mInstructions.get(requestedStep).getThumbnailUrl());

            } else {
                //make image view visible
                mThumbnailIV.setVisibility(View.VISIBLE);
                //make exoplayer view invisible
                mSimpleExoPlayerView.setVisibility(View.INVISIBLE);
                //set the thumbnail to
                imageViewThumbnail(mInstructions.get(requestedStep).getThumbnailUrl());
            }

        //if there is no thumbnail or video, show default image
        } else {
            mSimpleExoPlayerView.setVisibility(View.GONE);

            if (mThumbnailIV != null) {
                mThumbnailIV.setVisibility(View.VISIBLE);
            }

            Glide.with(getContext()).load(R.drawable.default_cooking).into(mThumbnailIV);
        }
    }

    private void showThumbnail(String url) {
        //Setup ExoPlayer and ExoPlayerView
        if (mExoPlayer == null) {
            // Create an instance of the ExoPlayer.
            TrackSelector trackSelector = new DefaultTrackSelector();
            LoadControl loadControl = new DefaultLoadControl();
            mExoPlayer = ExoPlayerFactory.newSimpleInstance(getContext(), trackSelector, loadControl);
            mSimpleExoPlayerView.setPlayer(mExoPlayer);
        }

        //Create a bitmap from an MP4
        Bitmap thumbnail = null;
        try {
            thumbnail = convertMP4(url);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        //Attach bitmap to ExoPlayerView
        mSimpleExoPlayerView.setDefaultArtwork(thumbnail);
    }

    //method for converting an mp4 to a bitmap
    //https://stackoverflow.com/questions/22954894/is-it-possible-to-generate-a-thumbnail-from-a-video-url-in-android
    private Bitmap convertMP4(String path) throws Throwable {
        Bitmap bitmap = null;
        MediaMetadataRetriever mediaMetadataRetriever = null;
        try
        {
            mediaMetadataRetriever = new MediaMetadataRetriever();
            if (Build.VERSION.SDK_INT >= 14)
                mediaMetadataRetriever.setDataSource(path, new HashMap<String, String>());
            else
                mediaMetadataRetriever.setDataSource(path);
            //   mediaMetadataRetriever.setDataSource(videoPath);
            bitmap = mediaMetadataRetriever.getFrameAtTime();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Throwable("Exception in retriveVideoFrameFromVideo(String videoPath)" + e.getMessage());

        } finally {
            if (mediaMetadataRetriever != null) {
                mediaMetadataRetriever.release();
            }
        }
        return bitmap;
    }

    //method for handling images
    private void imageViewThumbnail(String thumbnailUrl) {
        Glide.with(getContext()).load(thumbnailUrl).into(mThumbnailIV);
    }

    //method for setting up instructions
    private void setupInstructions() {
        //set up instructions
        mStepTV.setText(mInstructions.get(requestedStep).getInstruction());
    }

    //method for cleaning up ExoPlayer
    private void releasePlayer() {
        if (mExoPlayer != null) {
            mExoPlayer.stop();
            mExoPlayer.release();
            mExoPlayer = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mInstructions.size() != 0) {
            initializePlayer(Uri.parse(mInstructions.get(requestedStep).getVideoUrl()));
        }
    }

    //save current list and index during device rotation
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(INSTRUCTIONS_LIST, (ArrayList<Instructions>) mInstructions);
        outState.putInt(LIST_INDEX, requestedStep);

        if (mExoPlayer != null) {
            playerPosition = mExoPlayer.getCurrentPosition();
            outState.putLong(PLAYER_POSITION, playerPosition);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(VOLLEY_TAG);
        }
        releasePlayer();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //clean up playerPosition
        playerPosition = 0L;
    }
}