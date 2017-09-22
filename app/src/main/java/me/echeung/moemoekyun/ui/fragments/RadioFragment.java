package me.echeung.moemoekyun.ui.fragments;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import me.echeung.moemoekyun.App;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.databinding.FragmentRadioBinding;
import me.echeung.moemoekyun.service.RadioService;
import me.echeung.moemoekyun.ui.activities.MainActivity;
import me.echeung.moemoekyun.ui.fragments.base.TabFragment;
import me.echeung.moemoekyun.utils.AuthUtil;
import me.echeung.moemoekyun.utils.SongActionsUtil;
import me.echeung.moemoekyun.viewmodels.RadioViewModel;

public class RadioFragment extends TabFragment {

    private FragmentRadioBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_radio, container, false);

        final RadioViewModel viewModel = App.getRadioViewModel();

        binding.radioSongs.setVm(viewModel);
        binding.radioControls.setVm(viewModel);

        final TextView vRequestBy = binding.radioControls.requestedBy;
        vRequestBy.setMovementMethod(LinkMovementMethod.getInstance());

        final FloatingActionButton vPlayPauseBtn = binding.radioControls.playPauseBtn;
        vPlayPauseBtn.setOnClickListener(v -> togglePlayPause());

        final ImageButton vHistoryBtn = binding.radioControls.historyBtn;
        vHistoryBtn.setOnClickListener(v -> showHistory());

        final ImageButton vFavoriteBtn = binding.radioControls.favoriteBtn;
        vFavoriteBtn.setOnClickListener(v -> favorite());

        binding.radioSongs.songList1.setOnLongClickListener(v -> {
            SongActionsUtil.copyToClipboard(getActivity(), viewModel.getCurrentSong());
            return true;
        });
        binding.radioSongs.songList2.setOnLongClickListener(v -> {
            SongActionsUtil.copyToClipboard(getActivity(), viewModel.getLastSong());
            return true;
        });
        binding.radioSongs.songList3.setOnLongClickListener(v -> {
            SongActionsUtil.copyToClipboard(getActivity(), viewModel.getSecondLastSong());
            return true;
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        if (binding != null) {
            binding.unbind();
        }

        super.onDestroy();
    }

    private void togglePlayPause() {
        final Intent playPauseIntent = new Intent(RadioService.PLAY_PAUSE);
        getActivity().sendBroadcast(playPauseIntent);
    }

    private void favorite() {
        if (!AuthUtil.isAuthenticated(getActivity())) {
            ((MainActivity) getActivity()).showLoginDialog(this::favorite);
            return;
        }

        final Intent favIntent = new Intent(RadioService.TOGGLE_FAVORITE);
        getActivity().sendBroadcast(favIntent);
    }

    private void showHistory() {
        App.getRadioViewModel().toggleShowHistory();
    }
}
