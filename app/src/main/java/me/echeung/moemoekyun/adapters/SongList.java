package me.echeung.moemoekyun.adapters;

import android.app.Activity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import me.echeung.listenmoeapi.models.Song;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.databinding.SongsListBinding;
import me.echeung.moemoekyun.utils.SongActionsUtil;
import me.echeung.moemoekyun.utils.SongSortUtil;
import me.echeung.moemoekyun.viewmodels.SongListViewModel;

public class SongList {

    private WeakReference<Activity> activity;
    private SongAdapter adapter;
    private SongListLoader loader;
    private SwipeRefreshLayout swipeRefreshLayout;

    private SongListViewModel songListViewModel;

    public SongList(Activity activity, SongsListBinding binding, String listId, SongListLoader loader) {
        this.activity = new WeakReference<>(activity);
        this.loader = loader;

        // List adapter
        this.adapter = new SongAdapter(activity, listId);
        final RecyclerView songsList = binding.list;
        songsList.setLayoutManager(new LinearLayoutManager(activity));
        songsList.setAdapter(adapter);

        // Pull to refresh
        swipeRefreshLayout = binding.refreshLayout;
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        swipeRefreshLayout.setOnRefreshListener(this::loadSongs);
        swipeRefreshLayout.setRefreshing(false);

        // Only allow pull to refresh when user is at the top of the list
        songsList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int topRowVerticalPosition = songsList.getChildCount() != 0
                        ? songsList.getChildAt(0).getTop()
                        : 0;
                swipeRefreshLayout.setEnabled(topRowVerticalPosition >= 0);
            }
        });

        this.songListViewModel = new SongListViewModel();
        binding.setVm(songListViewModel);

        // Filter
        binding.query.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                final String query = editable.toString().trim().toLowerCase();
                adapter.filter(query);

                songListViewModel.setHasResults(adapter.getItemCount() != 0);
            }
        });

        // Menu
        binding.overflowBtn.setOnClickListener(v -> {
            final Activity activityRef = this.activity.get();
            if (activityRef == null) return;

            final PopupMenu popupMenu = new PopupMenu(activityRef, binding.overflowBtn);
            popupMenu.inflate(R.menu.menu_sort);

            SongSortUtil.initSortMenu(activityRef, listId, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(this::handleMenuItemClick);
            popupMenu.show();
        });
    }

    public void loadSongs() {
        loader.loadSongs(adapter);
    }

    public void showLoading(boolean loading) {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(loading);
        }
    }

    private boolean handleMenuItemClick(MenuItem item) {
        final Activity activityRef = this.activity.get();
        if (activityRef == null) return false;

        if (SongSortUtil.handleSortMenuItem(item, adapter)) {
            return true;
        }

        if (item.getItemId() == R.id.action_random_request) {
            final Song randomSong = adapter.getRandomRequestSong();
            if (randomSong != null) {
                SongActionsUtil.request(activityRef, adapter, randomSong);
            } else {
                Toast.makeText(activityRef, activityRef.getString(R.string.all_cooldown), Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        return false;
    }

    public interface SongListLoader {
        void loadSongs(SongAdapter adapter);
    }

}
