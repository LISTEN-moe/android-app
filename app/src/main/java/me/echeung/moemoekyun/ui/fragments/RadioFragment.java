package me.echeung.moemoekyun.ui.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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
import android.widget.Toast;

import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.databinding.FragmentRadioBinding;
import me.echeung.moemoekyun.service.RadioService;
import me.echeung.moemoekyun.ui.App;
import me.echeung.moemoekyun.ui.activities.MainActivity;
import me.echeung.moemoekyun.ui.fragments.base.TabFragment;
import me.echeung.moemoekyun.utils.AuthUtil;
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

        binding.radioSongs.songList1.setOnLongClickListener(v -> copyToClipboard(viewModel.getCurrentSong().toString()));
        binding.radioSongs.songList2.setOnLongClickListener(v -> copyToClipboard(viewModel.getLastSong()));
        binding.radioSongs.songList3.setOnLongClickListener(v -> copyToClipboard(viewModel.getSecondLastSong()));

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

    private boolean copyToClipboard(String songInfo) {
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("song", songInfo);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(getActivity().getApplicationContext(), getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show();
        return true;
    }
}
