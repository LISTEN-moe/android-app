package me.echeung.listenmoe.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import me.echeung.listenmoe.R;
import me.echeung.listenmoe.adapters.SongAdapter;
import me.echeung.listenmoe.databinding.UserFragmentBinding;
import me.echeung.listenmoe.interfaces.UserFavoritesListener;
import me.echeung.listenmoe.interfaces.UserForumInfoListener;
import me.echeung.listenmoe.interfaces.UserInfoListener;
import me.echeung.listenmoe.model.Song;
import me.echeung.listenmoe.model.SongsList;
import me.echeung.listenmoe.model.UserInfo;
import me.echeung.listenmoe.ui.App;
import me.echeung.listenmoe.ui.activities.MainActivity;
import me.echeung.listenmoe.ui.fragments.base.TabFragment;
import me.echeung.listenmoe.util.APIUtil;
import me.echeung.listenmoe.util.AuthUtil;
import me.echeung.listenmoe.util.DownloadImageTask;
import me.echeung.listenmoe.util.SongActionsUtil;

public class UserFragment extends TabFragment implements SongAdapter.OnSongItemClickListener {

    public static final String FAV_EVENT = "fav_event";

    private ImageView userAvatar;

    // Favorites
    private List<Song> favorites;
    private SongAdapter adapter;

    // Receiver
    private IntentFilter intentFilter;
    private BroadcastReceiver receiver;

    public static Fragment newInstance(int sectionNumber) {
        return TabFragment.newInstance(sectionNumber, new UserFragment());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final UserFragmentBinding binding = DataBindingUtil.inflate(inflater, R.layout.user_fragment, container, false);
        binding.setUserName(App.STATE.userName);
        binding.setUserRequests(App.STATE.userRequests);

        final View view = binding.getRoot();
        userAvatar = binding.userAvatar;
        binding.btnLogin.setOnClickListener(v -> ((MainActivity) getActivity()).showLoginDialog());

        // TODO: favorites list should update

        // Favorites list adapter
        adapter = new SongAdapter(this);
        binding.userFavorites.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.userFavorites.setAdapter(adapter);

        // Set up favorites filtering
        binding.filterQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                final String query = editable.toString().trim().toLowerCase();

                if (query.isEmpty()) {
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
        intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.AUTH_EVENT);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!intent.getAction().equals(MainActivity.AUTH_EVENT)) {
                    return;
                }

                showViews();
            }
        };

        showViews();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        getContext().registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();

        getContext().unregisterReceiver(receiver);
    }

    private void showViews() {
        final boolean loggedIn = AuthUtil.isAuthenticated(getContext());

        if (loggedIn) {
            initData();
        }
    }

    private void initData() {
        if (!AuthUtil.isAuthenticated(getContext())) {
            return;
        }

        APIUtil.getUserInfo(getContext(), new UserInfoListener() {
            @Override
            public void onFailure(final String result) {
            }

            @Override
            public void onSuccess(final UserInfo userInfo) {
                getActivity().runOnUiThread(() -> {
                    final String userName = userInfo.getUsername();

                    App.STATE.userName.set(userName);

                    // TODO: Proper user profile/avatars are coming in v4
//                    APIUtil.getUserAvatar(getContext(), userName, new UserForumInfoListener() {
//                        @Override
//                        public void onFailure(final String result) {
//                        }
//
//                        @Override
//                        public void onSuccess(final String avatarUrl) {
//                            if (avatarUrl != null) {
//                                getActivity().runOnUiThread(() -> new DownloadImageTask(userAvatar).execute(avatarUrl));
//                            }
//                        }
//                    });
                });
            }
        });

        APIUtil.getUserFavorites(getContext(), new UserFavoritesListener() {
            @Override
            public void onFailure(final String result) {
            }

            @Override
            public void onSuccess(final SongsList songsList) {
                getActivity().runOnUiThread(() -> {
                    favorites = songsList.getSongs();
                    adapter.setSongs(favorites);
                    App.STATE.userRequests.set(songsList.getExtra().getRequests());
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
                .setMessage(R.string.req_dialog_message)
                .setPositiveButton(android.R.string.cancel, null)
                .setNegativeButton(favoriteAction, (dialogInterface, in) -> SongActionsUtil.favorite(getActivity(), adapter, song));

        if (song.isEnabled()) {
            // Create button "Request"
            builder.setNeutralButton(getString(R.string.action_request), (dialogInterface, im) -> SongActionsUtil.request(getActivity(), adapter, song));
        }

        builder.create().show();
    }
}
