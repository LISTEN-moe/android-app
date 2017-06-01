package jcotter.listenmoe.ui.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import jcotter.listenmoe.R;
import jcotter.listenmoe.adapters.SongAdapter;
import jcotter.listenmoe.interfaces.OnSongItemClickListener;
import jcotter.listenmoe.interfaces.UserFavoritesCallback;
import jcotter.listenmoe.interfaces.UserInfoCallback;
import jcotter.listenmoe.model.Song;
import jcotter.listenmoe.model.SongsList;
import jcotter.listenmoe.model.UserInfo;
import jcotter.listenmoe.ui.fragments.base.TabFragment;
import jcotter.listenmoe.util.APIUtil;
import jcotter.listenmoe.util.AuthUtil;
import jcotter.listenmoe.util.SongActionsUtil;

public class UserFragment extends TabFragment implements OnSongItemClickListener {

    // UI views
    private TextView mUserName;
    private TextView mUserRequests;
    private RecyclerView mUserFavorites;

    private SongAdapter adapter;

    public static Fragment newInstance(int sectionNumber) {
        return TabFragment.newInstance(sectionNumber, new UserFragment());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_user, container, false);

        // Get UI views
        mUserName = (TextView) rootView.findViewById(R.id.user_name);
        mUserRequests = (TextView) rootView.findViewById(R.id.user_requests);
        mUserFavorites = (RecyclerView) rootView.findViewById(R.id.user_favorites);

        // Set up favorites list
        adapter = new SongAdapter(this);
        mUserFavorites.setLayoutManager(new LinearLayoutManager(getContext()));
//        mList.addItemDecoration(new DividerItemDecoration(getContext()));
        mUserFavorites.setAdapter(adapter);

        // Show info
        initData();

        return rootView;
    }

    private void initData() {
        if (!AuthUtil.isAuthenticated(getContext())) {
            // TODO: show login required message
            return;
        }

        APIUtil.getUserInfo(getContext(), new UserInfoCallback() {
            @Override
            public void onFailure(final String result) {

            }

            @Override
            public void onSuccess(final UserInfo userInfo) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mUserName.setText(userInfo.getUsername());
                    }
                });
            }
        });

        APIUtil.getUserFavorites(getContext(), new UserFavoritesCallback() {
            @Override
            public void onFailure(final String result) {

            }

            @Override
            public void onSuccess(final SongsList songsList) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.setSongs(songsList.getSongs());
                        mUserRequests.setText(Integer.toString(songsList.getExtra().getRequests()));
                    }
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
                .setPositiveButton(R.string.cancel, null)
                .setNegativeButton(favoriteAction, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int in) {
                        SongActionsUtil.favorite(getActivity(), adapter, song);
                    }
                });

        if (song.isEnabled()) {
            // Create button "Request"
            builder.setNeutralButton(getString(R.string.action_request), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int im) {
                    SongActionsUtil.request(getActivity(), adapter, song);
                }
            });
        }

        builder.create().show();
    }
}
