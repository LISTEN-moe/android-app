package me.echeung.moemoekyun.ui.fragments;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableBoolean;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.databinding.FragmentRadioBinding;
import me.echeung.moemoekyun.service.StreamService;
import me.echeung.moemoekyun.state.AppState;
import me.echeung.moemoekyun.ui.activities.MainActivity;
import me.echeung.moemoekyun.ui.fragments.base.TabFragment;
import me.echeung.moemoekyun.util.AuthUtil;

public class RadioFragment extends TabFragment {

    public static Fragment newInstance(int sectionNumber) {
        return TabFragment.newInstance(sectionNumber, new RadioFragment());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final FragmentRadioBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_radio, container, false);
        final AppState state = AppState.getInstance();

        binding.radioSongs.setSong(state.currentSong);
        binding.radioSongs.setLastSong(state.lastSong);
        binding.radioSongs.setSecondLastSong(state.secondLastSong);
        binding.radioSongs.setShowHistory(state.showHistory);

        binding.radioControls.setPlaying(state.playing);
        binding.radioControls.setFavorited(state.currentFavorited);
        binding.radioControls.setListeners(state.listeners);
        binding.radioControls.setRequester(state.requester);

        final TextView vRequestBy = binding.radioControls.requestedBy;
        vRequestBy.setMovementMethod(LinkMovementMethod.getInstance());

        final FloatingActionButton vPlayPauseBtn = binding.radioControls.playPauseBtn;
        vPlayPauseBtn.setOnClickListener(v -> togglePlayPause());

        final ImageButton vHistoryBtn = binding.radioControls.historyBtn;
        vHistoryBtn.setOnClickListener(v -> showHistory());

        final ImageButton vFavoriteBtn = binding.radioControls.favoriteBtn;
        vFavoriteBtn.setOnClickListener(v -> favorite());

        return binding.getRoot();
    }

    private void togglePlayPause() {
        final Intent playPauseIntent = new Intent(StreamService.PLAY_PAUSE);
        getActivity().sendBroadcast(playPauseIntent);
    }

    private void favorite() {
        if (!AuthUtil.isAuthenticated(getActivity())) {
            ((MainActivity) getActivity()).showLoginDialog(this::favorite);
            return;
        }

        final Intent favIntent = new Intent(StreamService.TOGGLE_FAVORITE);
        getActivity().sendBroadcast(favIntent);
    }

    private void showHistory() {
        final ObservableBoolean showHistory = AppState.getInstance().showHistory;

        showHistory.set(!showHistory.get());
    }
}
