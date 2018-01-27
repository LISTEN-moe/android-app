package me.echeung.moemoekyun.utils;

import android.app.Activity;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import me.echeung.listenmoeapi.models.Song;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.adapters.SongAdapter;
import me.echeung.moemoekyun.databinding.SearchBarBinding;
import me.echeung.moemoekyun.viewmodels.SearchBarViewModel;

public class SearchBarUtil {

    private WeakReference<Activity> activity;
    private SearchBarBinding binding;
    private SongAdapter adapter;
    private String listId;

    public SearchBarUtil(Activity activity, SearchBarBinding binding, SongAdapter adapter, String listId) {
        this.activity = new WeakReference<>(activity);
        this.binding = binding;
        this.adapter = adapter;
        this.listId = listId;
    }

    public void init() {
        binding.setVm(new SearchBarViewModel());

        // Set up favorites filtering
        final EditText vFilterQuery = binding.query;
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
                adapter.filter(query);
            }
        });

        // Set up favorites sorting
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
}
