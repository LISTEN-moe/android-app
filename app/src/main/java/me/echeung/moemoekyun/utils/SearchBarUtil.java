package me.echeung.moemoekyun.utils;

import android.app.Activity;
import android.support.v7.widget.PopupMenu;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.TextView;
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

    private TextWatcher textWatcher;
    private TextView.OnEditorActionListener onEditorActionListener;

    public SearchBarUtil(Activity activity, SearchBarBinding binding, SongAdapter adapter, String listId) {
        this.activity = new WeakReference<>(activity);
        this.binding = binding;
        this.adapter = adapter;
        this.listId = listId;
    }

    public SearchBarUtil withTextWatcher(TextWatcher textWatcher) {
        this.textWatcher = textWatcher;
        return this;
    }

    public SearchBarUtil withOnEditorActionListener(TextView.OnEditorActionListener onEditorActionListener) {
        this.onEditorActionListener = onEditorActionListener;
        return this;
    }

    public void init() {
        binding.setVm(new SearchBarViewModel());

        if (textWatcher != null) {
            binding.query.addTextChangedListener(textWatcher);
        }

        if (onEditorActionListener != null) {
            binding.query.setOnEditorActionListener(onEditorActionListener);
        }

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
