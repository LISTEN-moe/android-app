package me.echeung.moemoekyun.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.adapters.SongAdapter;
import me.echeung.moemoekyun.databinding.FragmentUserBinding;
import me.echeung.moemoekyun.interfaces.UserFavoritesListener;
import me.echeung.moemoekyun.interfaces.UserInfoListener;
import me.echeung.moemoekyun.model.Song;
import me.echeung.moemoekyun.model.UserFavorites;
import me.echeung.moemoekyun.model.UserInfo;
import me.echeung.moemoekyun.state.UserState;
import me.echeung.moemoekyun.ui.activities.MainActivity;
import me.echeung.moemoekyun.ui.fragments.base.TabFragment;
import me.echeung.moemoekyun.util.APIUtil;
import me.echeung.moemoekyun.util.AuthUtil;
import me.echeung.moemoekyun.util.SongActionsUtil;

public class UserFragment extends TabFragment implements SongAdapter.OnSongItemClickListener {

    public static final String FAVORITE_EVENT = "fav_event";

    private LinearLayout vLoginMsg;
    private LinearLayout vUserContent;
    private ImageView vUserAvatar;

    // Favorites
    private List<Song> favorites;
    private SongAdapter adapter;

    // Receiver
    private IntentFilter intentFilter;
    private BroadcastReceiver intentReceiver;
    private boolean receiverRegistered = false;

    public static Fragment newInstance(int sectionNumber) {
        return TabFragment.newInstance(sectionNumber, new UserFragment());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final FragmentUserBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_user, container, false);
        final UserState state = UserState.getInstance();

        binding.userCard.setUserName(state.userName);
        binding.userCard.setUserRequests(state.userRequests);
        binding.userCard.setQueueSize(state.queueSize);
        binding.userCard.setQueuePosition(state.queuePosition);

        vLoginMsg = binding.loginMsg.container;
        vUserContent = binding.userContent;
        vUserAvatar = binding.userCard.userAvatar;

        // Login view
        final Button vBtnLogin = binding.loginMsg.btnLogin;
        vBtnLogin.setOnClickListener(v -> ((MainActivity) getActivity()).showLoginDialog());

        // Favorites list adapter
        adapter = new SongAdapter(this);
        final RecyclerView vUserFavorites = binding.favorites.favoritesList;
        vUserFavorites.setLayoutManager(new LinearLayoutManager(getContext()));
        vUserFavorites.setAdapter(adapter);

        // Set up favorites filtering
        final EditText vFilterQuery = binding.favorites.filterQuery;
        vFilterQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                final String query = editable.toString().trim().toLowerCase();

                if (TextUtils.isEmpty(query)) {
                    adapter.setSongs(favorites);
                } else {
                    final List<Song> filteredFavorites = new ArrayList<>();
                    for (final Song song : favorites) {
                        if (song.getTitle().toLowerCase().contains(query) ||
                                song.getArtistAndAnime().toLowerCase().contains(query)) {
                            filteredFavorites.add(song);
                        }
                    }
                    adapter.setSongs(filteredFavorites);
                }
            }
        });

        // Broadcast receiver
        initBroadcastReceiver();

        initUserContent();

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

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
        boolean authenticated = AuthUtil.isAuthenticated(getContext());

        vLoginMsg.setVisibility(authenticated ? View.GONE : View.VISIBLE);
        vUserContent.setVisibility(authenticated ? View.VISIBLE : View.GONE);

        if (!authenticated) {
            return;
        }

        APIUtil.getUserInfo(getContext(), new UserInfoListener() {
            @Override
            public void onFailure(final String result) {
            }

            @Override
            public void onSuccess(final UserInfo userInfo) {
                runOnUiThread(() -> {
                    final String userName = userInfo.getUsername();

                    UserState.getInstance().userName.set(userName);

                    // TODO: user avatars/banners are coming in v4
                });
            }
        });

        APIUtil.getUserFavorites(getContext(), new UserFavoritesListener() {
            @Override
            public void onFailure(final String result) {
            }

            @Override
            public void onSuccess(final UserFavorites userFavorites) {
                runOnUiThread(() -> {
                    favorites = userFavorites.getSongs();
                    adapter.setSongs(favorites);
                    UserState.getInstance().userRequests.set(userFavorites.getExtra().getRequests());
                });
            }
        });
    }

    @Override
    public void onSongItemClick(final Song song) {
        // Create button "Favorite"/"Unfavorite"
        final String favoriteAction = song.isFavorite() ?
                getString(R.string.action_unfavorite) :
                getString(R.string.action_favorite);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setTitle(song.getTitle())
                .setMessage(song.getArtistAndAnime())
                .setPositiveButton(android.R.string.cancel, null)
                .setNegativeButton(favoriteAction, (dialogInterface, in) -> SongActionsUtil.favorite(getActivity(), adapter, song));

        if (song.isEnabled()) {
            // Create button "Request"
            builder.setNeutralButton(getString(R.string.action_request), (dialogInterface, im) -> SongActionsUtil.request(getActivity(), adapter, song));
        }

        builder.create().show();
    }
}
