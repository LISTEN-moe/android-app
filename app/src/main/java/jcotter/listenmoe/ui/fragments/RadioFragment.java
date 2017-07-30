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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jcotter.listenmoe.R;
import jcotter.listenmoe.constants.ResponseMessages;
import jcotter.listenmoe.interfaces.FavoriteSongListener;
import jcotter.listenmoe.model.Song;
import jcotter.listenmoe.service.StreamService;
import jcotter.listenmoe.ui.activities.MainActivity;
import jcotter.listenmoe.ui.fragments.base.TabFragment;
import jcotter.listenmoe.util.APIUtil;
import jcotter.listenmoe.util.AuthUtil;
import jcotter.listenmoe.util.SDKUtil;

public class RadioFragment extends TabFragment {

    @BindView(R.id.track_title)
    TextView mTrackTitle;
    @BindView(R.id.track_subtitle)
    TextView mTrackSubtitle;
    @BindView(R.id.requested_by)
    TextView mRequestedByTxt;
    @BindView(R.id.volume_seekbar)
    SeekBar mVolumeBar;
    @BindView(R.id.play_pause_btn)
    ImageButton mPlayPauseBtn;
    @BindView(R.id.favorite_btn)
    ImageButton mFavoriteBtn;
    @BindView(R.id.current_listeners)
    TextView mListenersTxt;

    private Unbinder unbinder;

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
        final View view = inflater.inflate(R.layout.fragment_radio, container, false);
        unbinder = ButterKnife.bind(this, view);

        // Volume bar
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

        connectToSocket();

        return view;
    }

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }

    /**
     * Displays data received from websocket and checks if stream is playing.
     */
    private void connectToSocket() {
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
                            if (!requester.isEmpty()) {
                                requesterBuilder.append(String.format(getResources().getString(R.string.requested_by), requester));
                            }
                        } else {
                            trackTitle = getString(R.string.api_failure);
                            trackSubtitle = "";
                        }

                        // Current listeners
                        final String listeners = String.format(getResources().getString(R.string.current_listeners), intent.getIntExtra(StreamService.UPDATE_PLAYING_LISTENERS, 0));
                        final String requestedBy = requesterBuilder.toString();

                        getActivity().runOnUiThread(() -> {
                            mTrackTitle.setText(trackTitle);
                            mTrackSubtitle.setText(trackSubtitle);
                            mListenersTxt.setText(listeners);

                            if (requestedBy.isEmpty()) {
                                mRequestedByTxt.setVisibility(View.INVISIBLE);
                            } else {
                                mRequestedByTxt.setVisibility(View.VISIBLE);
                                mRequestedByTxt.setMovementMethod(LinkMovementMethod.getInstance());
                                mRequestedByTxt.setText(SDKUtil.fromHtml(requestedBy));
                            }

                            final int favDrawable = favorite ? R.drawable.ic_star_white_24dp : R.drawable.ic_star_border_white_24dp;
                            mFavoriteBtn.setImageDrawable(SDKUtil.getDrawable(getActivity(), favDrawable));
                        });
                        break;

                    default:
                        // TODO: this doesn't work very reliably
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

    @OnClick(R.id.play_pause_btn)
    public void togglePlayPause() {
        if (songID == -1) return;

        final Intent intent = new Intent(getActivity(), StreamService.class);
        intent.putExtra(StreamService.PLAY, !playing);
        intent.putExtra(StreamService.VOLUME, mVolumeBar.getProgress() / 100.0f);
        getActivity().startService(intent);
    }

    @OnClick(R.id.favorite_btn)
    public void favorite() {
        if (!AuthUtil.isAuthenticated(getActivity())) {
            ((MainActivity) getActivity()).showLoginDialog(this::favorite);
            return;
        }

        if (songID == -1) return;

        APIUtil.favoriteSong(getActivity(), songID, new FavoriteSongListener() {
            @Override
            public void onFailure(final String result) {
                getActivity().runOnUiThread(() -> {
                    if (result.equals(ResponseMessages.AUTH_FAILURE)) {
                        Toast.makeText(getActivity(), getString(R.string.token_expired), Toast.LENGTH_SHORT).show();
                        ((MainActivity) getActivity()).showLoginDialog();
                    }
                });
            }

            @Override
            public void onSuccess(final boolean favorited) {
                getActivity().runOnUiThread(() -> {
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
                });
            }
        });

        final Toast toast = Toast.makeText(getActivity(), getString(R.string.sending), Toast.LENGTH_SHORT);
        toast.show();
        new Handler().postDelayed(toast::cancel, 750);
    }
}
