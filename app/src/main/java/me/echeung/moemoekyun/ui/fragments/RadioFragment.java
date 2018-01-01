package me.echeung.moemoekyun.ui.fragments;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import me.echeung.moemoekyun.App;
import me.echeung.moemoekyun.BR;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.databinding.FragmentRadioBinding;
import me.echeung.moemoekyun.service.RadioService;
import me.echeung.moemoekyun.ui.activities.MainActivity;
import me.echeung.moemoekyun.utils.SongActionsUtil;
import me.echeung.moemoekyun.viewmodels.RadioViewModel;

public class RadioFragment extends Fragment {

    private FragmentRadioBinding binding;

    private RadioViewModel viewModel;

    private Observable.OnPropertyChangedCallback playPauseCallback;
    private FloatingActionButton vPlayPauseBtn;
    private AnimatedVectorDrawable playToPause;
    private AnimatedVectorDrawable pauseToPlay;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_radio, container, false);

        viewModel = App.getRadioViewModel();

        binding.radioSongs.setVm(viewModel);
        binding.radioControls.setVm(viewModel);

        final TextView vRequestBy = binding.radioControls.requestedBy;
        vRequestBy.setMovementMethod(LinkMovementMethod.getInstance());

        initPlayPause();

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

        if (playPauseCallback != null) {
            viewModel.removeOnPropertyChangedCallback(playPauseCallback);
        }

        super.onDestroy();
    }

    private void initPlayPause() {
        vPlayPauseBtn = binding.radioControls.playPauseBtn;
        vPlayPauseBtn.setOnClickListener(v -> togglePlayPause());

        playToPause = (AnimatedVectorDrawable) getActivity().getDrawable(R.drawable.avd_play_to_pause);
        pauseToPlay = (AnimatedVectorDrawable) getActivity().getDrawable(R.drawable.avd_pause_to_play);

        setPlayPauseDrawable();

        playPauseCallback = new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (propertyId == BR.isPlaying) {
                    setPlayPauseDrawable();
                }
            }
        };

        viewModel.addOnPropertyChangedCallback(playPauseCallback);
    }

    private void setPlayPauseDrawable() {
        final AnimatedVectorDrawable drawable = viewModel.getIsPlaying() ? playToPause : pauseToPlay;
        vPlayPauseBtn.setImageDrawable(drawable);
        drawable.start();
    }

    private void togglePlayPause() {
        final Intent playPauseIntent = new Intent(RadioService.PLAY_PAUSE);
        getActivity().sendBroadcast(playPauseIntent);
    }

    private void favorite() {
        if (!App.getAuthUtil().isAuthenticated()) {
            ((MainActivity) getActivity()).showLoginActivity(MainActivity.LOGIN_FAVORITE_REQUEST);
            return;
        }

        final Intent favIntent = new Intent(RadioService.TOGGLE_FAVORITE);
        getActivity().sendBroadcast(favIntent);
    }

    private void showHistory() {
        App.getRadioViewModel().toggleShowHistory();
    }
}
