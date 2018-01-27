package me.echeung.moemoekyun.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.List;

import me.echeung.listenmoeapi.APIClient;
import me.echeung.listenmoeapi.callbacks.UserFavoritesCallback;
import me.echeung.listenmoeapi.callbacks.UserInfoCallback;
import me.echeung.listenmoeapi.models.Song;
import me.echeung.listenmoeapi.models.User;
import me.echeung.moemoekyun.App;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.adapters.SongAdapter;
import me.echeung.moemoekyun.adapters.SongList;
import me.echeung.moemoekyun.databinding.FragmentUserBinding;
import me.echeung.moemoekyun.ui.activities.MainActivity;
import me.echeung.moemoekyun.viewmodels.UserViewModel;

public class UserFragment extends Fragment implements SongList.SongListLoader {

    private static final String LIST_ID = "USER_FAVORITES_LIST";

    public static final String REQUEST_EVENT = "req_event";
    public static final String FAVORITE_EVENT = "fav_event";

    private FragmentUserBinding binding;

    private LinearLayout vLoginMsg;
    private LinearLayout vUserContent;

    private UserViewModel viewModel;

    private SwipeRefreshLayout swipeRefreshLayout;
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

        vLoginMsg = binding.loginMsg.container;
        vUserContent = binding.userContent;

        // TODO: move this to main activity level to hide all tabs
        // Login view
        final Button vBtnLogin = binding.loginMsg.btnLogin;
        vBtnLogin.setOnClickListener(v -> ((MainActivity) getActivity()).showAuthActivity());

        // TODO: move this to SongList
        // Pull to refresh
        swipeRefreshLayout = binding.userFavoritesContainer;
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        swipeRefreshLayout.setOnRefreshListener(() -> songList.loadSongs());
        swipeRefreshLayout.setRefreshing(false);

        // Only allow pull to refresh when user is at the top of the list
        final RecyclerView favoritesList = binding.favorites.favoritesList.list;
        favoritesList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int topRowVerticalPosition = favoritesList.getChildCount() != 0 ?
                        favoritesList.getChildAt(0).getTop() :
                        0;
                swipeRefreshLayout.setEnabled(topRowVerticalPosition >= 0);
            }
        });

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
                        case UserFragment.FAVORITE_EVENT:
                        case UserFragment.REQUEST_EVENT:
                            initUserContent();
                            break;
                    }
                }
            }
        };

        intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.AUTH_EVENT);
        intentFilter.addAction(UserFragment.FAVORITE_EVENT);

        getActivity().registerReceiver(intentReceiver, intentFilter);
        receiverRegistered = true;
    }

    private void initUserContent() {
        swipeRefreshLayout.setRefreshing(true);

        boolean authenticated = App.getAuthUtil().isAuthenticated();

        vLoginMsg.setVisibility(authenticated ? View.GONE : View.VISIBLE);
        vUserContent.setVisibility(authenticated ? View.VISIBLE : View.GONE);

        if (!authenticated) {
            return;
        }

        getUserInfo();
    }

    private void getUserInfo() {
        App.getApiClient().getUserInfo(new UserInfoCallback() {
            @Override
            public void onSuccess(final User user) {
                viewModel.setUser(user);

                if (user.getAvatarImage() != null) {
                    viewModel.setAvatarUrl(APIClient.CDN_AVATAR_URL + user.getAvatarImage());
                }

                if (user.getBannerImage() != null) {
                    viewModel.setBannerUrl(APIClient.CDN_BANNER_URL + user.getBannerImage());
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
                    getActivity().runOnUiThread(() -> adapter.setSongs(favorites));
                }

                viewModel.setHasFavorites(!favorites.isEmpty());

                completeRefresh();
            }

            @Override
            public void onFailure(final String message) {
                completeRefresh();
            }
        });
    }

    private void completeRefresh() {
        if (getActivity() != null && swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
            getActivity().runOnUiThread(() -> swipeRefreshLayout.setRefreshing(false));
        }
    }

}
