package me.echeung.moemoekyun.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import me.echeung.listenmoeapi.callbacks.UserFavoritesCallback;
import me.echeung.listenmoeapi.callbacks.UserInfoCallback;
import me.echeung.listenmoeapi.endpoints.Library;
import me.echeung.listenmoeapi.models.Song;
import me.echeung.listenmoeapi.models.User;
import me.echeung.moemoekyun.App;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.adapters.songslist.SongAdapter;
import me.echeung.moemoekyun.adapters.songslist.SongList;
import me.echeung.moemoekyun.databinding.FragmentUserBinding;
import me.echeung.moemoekyun.ui.activities.MainActivity;
import me.echeung.moemoekyun.utils.SongActionsUtil;
import me.echeung.moemoekyun.viewmodels.UserViewModel;

public class UserFragment extends Fragment implements SongList.SongListLoader {

    private static final String LIST_ID = "USER_FAVORITES_LIST";

    private FragmentUserBinding binding;

    private UserViewModel viewModel;

    private SongList songList;

    // Receiver
    private IntentFilter intentFilter;
    private BroadcastReceiver intentReceiver;
    private boolean receiverRegistered = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_user, container, false);

        viewModel = App.getUserViewModel();

        binding.setRadioVm(App.getRadioViewModel());
        binding.setUserVm(viewModel);

        songList = new SongList(getActivity(), binding.favorites.favoritesList, LIST_ID, this);

        initBroadcastReceiver();
        initUserContent();

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        // In case favorites were updated
        initUserContent();

        if (!receiverRegistered) {
            getActivity().registerReceiver(intentReceiver, intentFilter);
            receiverRegistered = true;
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (receiverRegistered) {
            getActivity().unregisterReceiver(intentReceiver);
            receiverRegistered = false;
        }
    }

    @Override
    public void onDestroy() {
        if (receiverRegistered) {
            getActivity().unregisterReceiver(intentReceiver);
            receiverRegistered = false;
        }

        if (binding != null) {
            binding.unbind();
        }

        super.onDestroy();
    }

    private void initBroadcastReceiver() {
        intentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (action != null) {
                    switch (action) {
                        case MainActivity.AUTH_EVENT:
                        case SongActionsUtil.FAVORITE_EVENT:
                        case SongActionsUtil.REQUEST_EVENT:
                            initUserContent();
                            break;
                    }
                }
            }
        };

        intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.AUTH_EVENT);
        intentFilter.addAction(SongActionsUtil.FAVORITE_EVENT);

        getActivity().registerReceiver(intentReceiver, intentFilter);
        receiverRegistered = true;
    }

    private void initUserContent() {
        songList.showLoading(true);

        if (App.getAuthUtil().isAuthenticated()) {
            getUserInfo();
            songList.loadSongs();
        }
    }

    private void getUserInfo() {
        App.getApiClient().getUserInfo(new UserInfoCallback() {
            @Override
            public void onSuccess(final User user) {
                viewModel.setUser(user);

                if (user.getAvatarImage() != null) {
                    viewModel.setAvatarUrl(Library.CDN_AVATAR_URL + user.getAvatarImage());
                }

                if (user.getBannerImage() != null) {
                    viewModel.setBannerUrl(Library.CDN_BANNER_URL + user.getBannerImage());
                }
            }

            @Override
            public void onFailure(final String message) {
            }
        });
    }

    @Override
    public void loadSongs(SongAdapter adapter) {
        App.getApiClient().getUserFavorites(new UserFavoritesCallback() {
            @Override
            public void onSuccess(final List<Song> favorites) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        songList.showLoading(false);
                        adapter.setSongs(favorites);
                    });
                }

                viewModel.setHasFavorites(!favorites.isEmpty());
            }

            @Override
            public void onFailure(final String message) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        songList.showLoading(false);
                    });
                }
            }
        });
    }

}
