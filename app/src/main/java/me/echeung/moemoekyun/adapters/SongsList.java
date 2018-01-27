package me.echeung.moemoekyun.adapters;

import android.app.Activity;
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
import me.echeung.moemoekyun.viewmodels.SearchBarViewModel;

public class SongsList {

    private WeakReference<Activity> activity;
    private SongsListBinding binding;
    private SongAdapter adapter;
    private String listId;
    private SongListLoader loader;

    public SongsList(Activity activity, SongsListBinding binding, String listId, SongListLoader loader) {
        this.activity = new WeakReference<>(activity);
        this.binding = binding;
        this.listId = listId;
        this.loader = loader;

        this.adapter = new SongAdapter(activity, listId);
        final RecyclerView songsList = binding.list;
        songsList.setLayoutManager(new LinearLayoutManager(activity));
        songsList.setAdapter(adapter);
    }

    public void init() {
        binding.setVm(new SearchBarViewModel());

        binding.query.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // TODO: debounce this
                final String query = editable.toString().trim().toLowerCase();
                adapter.filter(query);

                loader.onFilter(query, adapter.getItemCount() != 0);
            }
        });

        binding.overflowBtn.setOnClickListener(v -> {
            final Activity activityRef = this.activity.get();
            if (activityRef == null) return;

            final PopupMenu popupMenu = new PopupMenu(activityRef, binding.overflowBtn);
            popupMenu.inflate(R.menu.menu_sort);

            SongSortUtil.initSortMenu(activityRef, listId, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(this::handleMenuItemClick);
            popupMenu.show();
        });

        loadSongs();
    }

    public void loadSongs() {
        loader.loadSongs(adapter);
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
        void onFilter(String query, boolean hasResults);
    }

}
