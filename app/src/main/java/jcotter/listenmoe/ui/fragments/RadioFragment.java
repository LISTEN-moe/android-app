package jcotter.listenmoe.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jcotter.listenmoe.R;
import jcotter.listenmoe.constants.ResponseMessages;
import jcotter.listenmoe.databinding.RadioFragmentBinding;
import jcotter.listenmoe.interfaces.FavoriteSongListener;
import jcotter.listenmoe.model.Song;
import jcotter.listenmoe.service.StreamService;
import jcotter.listenmoe.ui.App;
import jcotter.listenmoe.ui.activities.MainActivity;
import jcotter.listenmoe.ui.fragments.base.TabFragment;
import jcotter.listenmoe.util.APIUtil;
import jcotter.listenmoe.util.AuthUtil;
import jcotter.listenmoe.util.SDKUtil;

public class RadioFragment extends TabFragment {

    @BindView(R.id.requested_by)
    TextView mRequestedByTxt;
    @BindView(R.id.play_pause_btn)
    ImageButton mPlayPauseBtn;
    @BindView(R.id.favorite_btn)
    ImageButton mFavoriteBtn;

    private Unbinder unbinder;

    public static Fragment newInstance(int sectionNumber) {
        return TabFragment.newInstance(sectionNumber, new RadioFragment());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final RadioFragmentBinding binding = DataBindingUtil.inflate(inflater, R.layout.radio_fragment, container, false);
        binding.setSong(App.STATE.currentSong);
        binding.setListeners(App.STATE.listeners);
        binding.setPlaying(App.STATE.playing);

        final View view = binding.getRoot();
        unbinder = ButterKnife.bind(this, view);

        mRequestedByTxt.setMovementMethod(LinkMovementMethod.getInstance());

        setupIntentFilter();

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
    private void setupIntentFilter() {
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
                        updateSongInfo();
                        break;
                }
            }
        }, intentFilter);
    }

    private void updateSongInfo() {
        final StringBuilder requesterBuilder = new StringBuilder();

        final Song currentSong = App.STATE.currentSong.get();

        getActivity().runOnUiThread(() -> {
            final int favDrawable = currentSong.isFavorite() ? R.drawable.ic_star_white_24dp : R.drawable.ic_star_border_white_24dp;
            mFavoriteBtn.setImageDrawable(SDKUtil.getDrawable(getActivity(), favDrawable));
        });
    }

    @OnClick(R.id.play_pause_btn)
    public void togglePlayPause() {
        if (App.STATE.currentSong.get() == null) return;

        App.getService().togglePlayPause();
    }

    @OnClick(R.id.favorite_btn)
    public void favorite() {
        if (!AuthUtil.isAuthenticated(getActivity())) {
            ((MainActivity) getActivity()).showLoginDialog(this::favorite);
            return;
        }

        final Song currentSong = App.STATE.currentSong.get();
        if (currentSong == null) return;

        final int songId = currentSong.getId();
        if (songId == -1) return;

        APIUtil.favoriteSong(getActivity(), songId, new FavoriteSongListener() {
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
                App.STATE.currentSong.get().setFavorite(favorited);
            }
        });

        final Toast toast = Toast.makeText(getActivity(), getString(R.string.sending), Toast.LENGTH_SHORT);
        toast.show();
        new Handler().postDelayed(toast::cancel, 750);
    }
}
