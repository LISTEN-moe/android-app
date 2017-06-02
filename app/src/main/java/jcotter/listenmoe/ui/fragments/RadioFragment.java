package jcotter.listenmoe.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import jcotter.listenmoe.R;
import jcotter.listenmoe.constants.ResponseMessages;
import jcotter.listenmoe.interfaces.FavoriteSongCallback;
import jcotter.listenmoe.model.Song;
import jcotter.listenmoe.service.StreamService;
import jcotter.listenmoe.ui.activities.MainActivity;
import jcotter.listenmoe.ui.fragments.base.TabFragment;
import jcotter.listenmoe.util.APIUtil;
import jcotter.listenmoe.util.AuthUtil;
import jcotter.listenmoe.util.SDKUtil;

public class RadioFragment extends TabFragment {

    // UI views
    private TextView mTrackTitle;
    private TextView mTrackSubtitle;
    private TextView mRequestedByTxt;
    private SeekBar mVolumeBar;
    private ImageButton mPlayPauseBtn;
    private ImageButton mFavoriteBtn;
    private TextView mListenersTxt;

    // Radio things
    private int songID;
    private boolean favorite;
    private boolean playing;

    public static Fragment newInstance(int sectionNumber) {
        return TabFragment.newInstance(sectionNumber, new RadioFragment());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_radio, container, false);

        // Get UI views
        mTrackTitle = (TextView) rootView.findViewById(R.id.track_title);
        mTrackSubtitle = (TextView) rootView.findViewById(R.id.track_subtitle);
        mRequestedByTxt = (TextView) rootView.findViewById(R.id.requestedText);
        mVolumeBar = (SeekBar) rootView.findViewById(R.id.seekBar);
        mPlayPauseBtn = (ImageButton) rootView.findViewById(R.id.play_pause_btn);
        mFavoriteBtn = (ImageButton) rootView.findViewById(R.id.favorite_btn);
        mListenersTxt = (TextView) rootView.findViewById(R.id.currentText);

        // Set font to OpenSans
        final Typeface openSans = Typeface.createFromAsset(getActivity().getAssets(), "fonts/OpenSans-Regular.ttf");
        mTrackTitle.setTypeface(openSans);
        mTrackSubtitle.setTypeface(openSans);
        mRequestedByTxt.setTypeface(openSans);
        mListenersTxt.setTypeface(openSans);

        mRequestedByTxt.setVisibility(View.INVISIBLE);

        // Set up view listeners
        volumeSliderListener();
        mPlayPauseBtn.setOnClickListener(mOnClickListener);
        mFavoriteBtn.setOnClickListener(mOnClickListener);

        socketDisplay();

        return rootView;
    }


    // UI methods

    /**
     * Listener for volume slider progress changed.
     */
    private void volumeSliderListener() {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mVolumeBar.setProgress((int) (sharedPreferences.getFloat(StreamService.VOLUME, 0.5f) * 100));
        mVolumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (StreamService.isServiceRunning) {
                    Intent intent = new Intent(getActivity(), StreamService.class)
                            .putExtra(StreamService.VOLUME, seekBar.getProgress() / 100.0f);
                    getActivity().startService(intent);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharedPreferences.Editor editor = sharedPreferences.edit()
                        .putFloat(StreamService.VOLUME, seekBar.getProgress() / 100.0f);
                editor.apply();
            }
        });
    }

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (v == mPlayPauseBtn) {
                playPauseLogic();
            } else if (v == mFavoriteBtn) {
                favoriteLogic();
            }
        }
    };

    /**
     * Displays data received from websocket and checks if stream is playing.
     */
    private void socketDisplay() {
        songID = -1;
        favorite = false;

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(getActivity().getPackageName());
        intentFilter.addAction(StreamService.UPDATE_PLAYING);

        ((MainActivity) getActivity()).registerBroadcastReceiver(new BroadcastReceiver() {
            @SuppressWarnings("deprecation")
            @Override
            public void onReceive(Context context, final Intent intent) {
                switch (intent.getAction()) {
                    // Updating current song info from StreamService
                    case StreamService.UPDATE_PLAYING:
                        // TODO: clean this up a bit more
                        final String trackTitle;
                        final String trackSubtitle;
                        final StringBuilder requesterBuilder = new StringBuilder();

                        // Fetch data from intent
                        if (intent.hasExtra(StreamService.UPDATE_PLAYING_SONG)) {
                            final Song currentSong = intent.getParcelableExtra(StreamService.UPDATE_PLAYING_SONG);
                            final String requester = intent.getStringExtra(StreamService.UPDATE_PLAYING_REQUESTER);

                            songID = currentSong.getId();
                            favorite = currentSong.isFavorite();

                            // Current song info
                            trackTitle = currentSong.getTitle();
                            trackSubtitle = currentSong.getArtistAndAnime();

                            // Song requester
                            if (!requester.equals("")) {
                                requesterBuilder.append(String.format(getResources().getString(R.string.requested_by), requester));
                            }
                        } else {
                            trackTitle = getString(R.string.api_failure);
                            trackSubtitle = "";
                        }

                        // Current listeners
                        final String listeners = String.format(getResources().getString(R.string.current_listeners), intent.getIntExtra(StreamService.UPDATE_PLAYING_LISTENERS, 0));

                        final String requestedBy = requesterBuilder.toString();

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTrackTitle.setText(trackTitle);
                                mTrackSubtitle.setText(trackSubtitle);
                                mListenersTxt.setText(listeners);

                                if (requestedBy.equals("")) {
                                    mRequestedByTxt.setVisibility(View.INVISIBLE);
                                } else {
                                    mRequestedByTxt.setVisibility(View.VISIBLE);
                                    mRequestedByTxt.setMovementMethod(LinkMovementMethod.getInstance());
                                    mRequestedByTxt.setText(SDKUtil.fromHtml(requestedBy));
                                }
                                final int favDrawable = favorite ? R.drawable.ic_star_white_24dp : R.drawable.ic_star_border_white_24dp;
                                mFavoriteBtn.setImageDrawable(SDKUtil.getDrawable(getActivity(), favDrawable));
                            }
                        });
                        break;

                    default:
                        if (intent.hasExtra(StreamService.RUNNING)) {
                            playing = intent.getBooleanExtra(StreamService.RUNNING, false);
                            final int playDrawable = playing ? R.drawable.ic_pause_white_24dp : R.drawable.ic_play_arrow_white_24dp;
                            mPlayPauseBtn.setImageDrawable(SDKUtil.getDrawable(getActivity(), playDrawable));
                        }

                        if (intent.hasExtra(StreamService.FAVORITE)) {
                            favorite = intent.getBooleanExtra(StreamService.FAVORITE, false);
                            final int favDrawable = favorite ? R.drawable.ic_star_white_24dp : R.drawable.ic_star_border_white_24dp;
                            mFavoriteBtn.setImageDrawable(SDKUtil.getDrawable(getActivity(), favDrawable));
                        }
                        break;
                }
            }
        }, intentFilter);

        final Intent intent = new Intent(getActivity(), StreamService.class);
        if (StreamService.isServiceRunning) {
            intent.putExtra(StreamService.REQUEST, true); // Requests socket update
        } else {
            intent.putExtra(StreamService.RECEIVER, true); // Start service
        }
        intent.putExtra(StreamService.PROBE, true); // Checks if stream is playing
        getActivity().startService(intent);
    }

    private void favoriteLogic() {
        if (!AuthUtil.isAuthenticated(getActivity())) {
            ((MainActivity) getActivity()).showLoginDialog();
            return;
        }

        if (songID == -1) return;

        APIUtil.favoriteSong(getActivity(), songID, new FavoriteSongCallback() {
            @Override
            public void onFailure(final String result) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result.equals(ResponseMessages.AUTH_FAILURE)) {
                            Toast.makeText(getActivity(), getString(R.string.token_expired), Toast.LENGTH_SHORT).show();
                            ((MainActivity) getActivity()).showLoginDialog();
                        }
                    }
                });
            }

            @Override
            public void onSuccess(final boolean favorited) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        favorite = favorited;
                        if (favorited) {
                            mFavoriteBtn.setImageDrawable(SDKUtil.getDrawable(getActivity(), R.drawable.ic_star_white_24dp));
                        } else {
                            mFavoriteBtn.setImageDrawable(SDKUtil.getDrawable(getActivity(), R.drawable.ic_star_border_white_24dp));
                        }

                        if (StreamService.isServiceRunning) {
                            Intent favUpdate = new Intent(getActivity(), StreamService.class)
                                    .putExtra(StreamService.TOGGLE_FAVORITE, favorited);
                            getActivity().startService(favUpdate);
                        }
                    }
                });
            }
        });

        final Toast toast = Toast.makeText(getActivity(), getString(R.string.sending), Toast.LENGTH_SHORT);
        toast.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, 750);
    }

    private void playPauseLogic() {
        if (songID == -1) return;

        final Intent intent = new Intent(getActivity(), StreamService.class);
        intent.putExtra(StreamService.PLAY, !playing);
        intent.putExtra(StreamService.VOLUME, mVolumeBar.getProgress() / 100.0f);
        getActivity().startService(intent);
    }
}
