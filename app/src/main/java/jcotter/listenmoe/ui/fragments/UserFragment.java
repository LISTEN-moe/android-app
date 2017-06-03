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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import jcotter.listenmoe.R;
import jcotter.listenmoe.adapters.SongAdapter;
import jcotter.listenmoe.interfaces.OnSongItemClickListener;
import jcotter.listenmoe.interfaces.UserFavoritesCallback;
import jcotter.listenmoe.interfaces.UserForumInfoCallback;
import jcotter.listenmoe.interfaces.UserInfoCallback;
import jcotter.listenmoe.model.Song;
import jcotter.listenmoe.model.SongsList;
import jcotter.listenmoe.model.UserInfo;
import jcotter.listenmoe.ui.activities.MainActivity;
import jcotter.listenmoe.ui.fragments.base.TabFragment;
import jcotter.listenmoe.util.APIUtil;
import jcotter.listenmoe.util.AuthUtil;
import jcotter.listenmoe.util.DownloadImageTask;
import jcotter.listenmoe.util.SongActionsUtil;

public class UserFragment extends TabFragment implements OnSongItemClickListener {

    // UI views
    private LinearLayout mLoginMsg;
    private LinearLayout mContent;
    private ImageView mUserAvatar;
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
        mLoginMsg = (LinearLayout) rootView.findViewById(R.id.login_msg);
        mContent = (LinearLayout) rootView.findViewById(R.id.content);
        mUserAvatar = (ImageView) rootView.findViewById(R.id.user_avatar);
        mUserName = (TextView) rootView.findViewById(R.id.user_name);
        mUserRequests = (TextView) rootView.findViewById(R.id.user_requests);
        mUserFavorites = (RecyclerView) rootView.findViewById(R.id.user_favorites);

        rootView.findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).showLoginDialog(new MainActivity.OnLoginListener() {
                    @Override
                    public void onLogin() {
                        initData();
                    }
                });
            }
        });

        // Set up favorites list
        adapter = new SongAdapter(this);
        mUserFavorites.setLayoutManager(new LinearLayoutManager(getContext()));
        mUserFavorites.setAdapter(adapter);

        // Show info
        initData();

        return rootView;
    }

    private void initData() {
        final boolean loggedIn = AuthUtil.isAuthenticated(getContext());
        mLoginMsg.setVisibility(loggedIn ? View.GONE : View.VISIBLE);
        mContent.setVisibility(loggedIn ? View.VISIBLE : View.GONE);

        if (!loggedIn) return;

        APIUtil.getUserInfo(getContext(), new UserInfoCallback() {
            @Override
            public void onFailure(final String result) {
            }

            @Override
            public void onSuccess(final UserInfo userInfo) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final String userName = userInfo.getUsername();

                        mUserName.setText(userName);

                        APIUtil.getUserAvatar(getContext(), userName, new UserForumInfoCallback() {
                            @Override
                            public void onFailure(final String result) {
                            }

                            @Override
                            public void onSuccess(final String avatarUrl) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        new DownloadImageTask(mUserAvatar).execute(avatarUrl);
                                    }
                                });
                            }
                        });
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
                        mUserRequests.setText(String.format(getString(R.string.user_requests), songsList.getExtra().getRequests()));
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
