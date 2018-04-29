package me.echeung.moemoekyun.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import me.echeung.moemoekyun.App;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.adapter.songslist.SongAdapter;
import me.echeung.moemoekyun.adapter.songslist.SongList;
import me.echeung.moemoekyun.client.api.callback.UserFavoritesCallback;
import me.echeung.moemoekyun.client.api.callback.UserInfoCallback;
import me.echeung.moemoekyun.client.api.library.Library;
import me.echeung.moemoekyun.client.model.Song;
import me.echeung.moemoekyun.client.model.User;
import me.echeung.moemoekyun.databinding.FragmentUserBinding;
import me.echeung.moemoekyun.ui.activity.MainActivity;
import me.echeung.moemoekyun.ui.base.BaseFragment;
import me.echeung.moemoekyun.util.SongActionsUtil;
import me.echeung.moemoekyun.viewmodel.UserViewModel;

public class UserFragment extends BaseFragment<FragmentUserBinding> implements SongList.SongListLoader {

    private static final String LIST_ID = "USER_FAVORITES_LIST";

    private UserViewModel viewModel;

    private SongList songList;

    @Override
    public int getLayout() {
        return R.layout.fragment_user;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        viewModel = App.getUserViewModel();

        binding.setRadioVm(App.getRadioViewModel());
        binding.setUserVm(viewModel);

        songList = new SongList(getActivity(), binding.favorites.favoritesList, LIST_ID, this);

        initUserContent();

        return view;
    }

    @Override
    public BroadcastReceiver getBroadcastReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action != null) {
                    switch (action) {
                        case MainActivity.AUTH_EVENT:
                        case SongActionsUtil.FAVORITE_EVENT:
                            initUserContent();
                            break;
                    }
                }
            }
        };
    }

    @Override
    public IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.AUTH_EVENT);
        intentFilter.addAction(SongActionsUtil.FAVORITE_EVENT);

        return intentFilter;
    }

    @Override
    public void loadSongs(SongAdapter adapter) {
        App.getApiClient().getUserFavorites(new UserFavoritesCallback() {
            @Override
            public void onSuccess(List<Song> favorites) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        songList.showLoading(false);
                        adapter.setSongs(favorites);
                    });
                }

                viewModel.setHasFavorites(!favorites.isEmpty());
            }

            @Override
            public void onFailure(String message) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        songList.showLoading(false);
                    });
                }
            }
        });
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
            public void onSuccess(User user) {
                viewModel.setUser(user);

                if (user.getAvatarImage() != null) {
                    viewModel.setAvatarUrl(Library.CDN_AVATAR_URL + user.getAvatarImage());
                }

                if (user.getBannerImage() != null) {
                    viewModel.setBannerUrl(Library.CDN_BANNER_URL + user.getBannerImage());
                }
            }

            @Override
            public void onFailure(String message) {
            }
        });
    }

}
