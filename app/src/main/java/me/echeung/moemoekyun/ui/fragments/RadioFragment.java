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
import me.echeung.moemoekyun.databinding.RadioFragmentBinding;
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
        final RadioFragmentBinding binding = DataBindingUtil.inflate(inflater, R.layout.radio_fragment, container, false);
        final AppState state = AppState.getInstance();
        binding.setPlaying(state.playing);
        binding.setSong(state.currentSong);
        binding.setFavorited(state.currentFavorited);
        binding.setListeners(state.listeners);
        binding.setRequester(state.requester);
        binding.setShowHistory(state.showHistory);
        binding.setLastSong(state.lastSong);
        binding.setSecondLastSong(state.secondLastSong);

        final View view = binding.getRoot();

        final TextView vRequestBy = view.findViewById(R.id.requested_by);
        vRequestBy.setMovementMethod(LinkMovementMethod.getInstance());

        final FloatingActionButton vPlayPauseBtn = view.findViewById(R.id.play_pause_btn);
        vPlayPauseBtn.setOnClickListener(v -> togglePlayPause());

        final ImageButton vHistoryBtn = view.findViewById(R.id.history_btn);
        vHistoryBtn.setOnClickListener(v -> showHistory());

        final ImageButton vFavoriteBtn = view.findViewById(R.id.favorite_btn);
        vFavoriteBtn.setOnClickListener(v -> favorite());

        return view;
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
