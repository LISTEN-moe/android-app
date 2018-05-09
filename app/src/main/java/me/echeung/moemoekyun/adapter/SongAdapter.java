package me.echeung.moemoekyun.adapter;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.client.model.Song;
import me.echeung.moemoekyun.databinding.SongItemBinding;
import me.echeung.moemoekyun.util.SongActionsUtil;
import me.echeung.moemoekyun.util.SongSortUtil;

public class SongAdapter extends ListAdapter<Song, RecyclerView.ViewHolder> {

    private static final DiffUtil.ItemCallback<Song> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Song>() {
                @Override
                public boolean areItemsTheSame(@NonNull Song oldSong, @NonNull Song newSong) {
                    return oldSong.getId() == newSong.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Song oldSong, @NonNull Song newSong) {
                    return oldSong.equals(newSong);
                }
            };

    private WeakReference<Activity> activity;
    private String listId;

    private List<Song> allSongs;
    private List<Song> visibleSongs;

    private String filterQuery;

    public SongAdapter(Activity activity, String listId) {
        super(DIFF_CALLBACK);

        this.activity = new WeakReference<>(activity);
        this.listId = listId;

        setHasStableIds(true);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        SongItemBinding binding = DataBindingUtil.inflate(layoutInflater, R.layout.song_item, parent, false);
        return new SongViewHolder(binding, this);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Song song = visibleSongs.get(position);
        SongViewHolder songHolder = (SongViewHolder) holder;
        songHolder.bind(song);
    }

    @Override
    public long getItemId(int position) {
        return visibleSongs.get(position).getId();
    }

    @Override
    public int getItemCount() {
        return visibleSongs != null ? visibleSongs.size() : 0;
    }

    public void setSongs(List<Song> songs) {
        this.allSongs = songs;
        updateSongs();
    }

    public void filter(String query) {
        this.filterQuery = query;
        updateSongs();
    }

    public void sortType(String sortType) {
        Activity activityRef = activity.get();
        if (activityRef == null) return;

        SongSortUtil.setListSortType(activityRef, listId, sortType);
        updateSongs();
    }

    public void sortDescending(boolean descending) {
        Activity activityRef = activity.get();
        if (activityRef == null) return;

        SongSortUtil.setListSortDescending(activityRef, listId, descending);
        updateSongs();
    }

    private void updateSongs() {
        Activity activityRef = activity.get();
        if (activityRef == null) return;

        if (allSongs == null || allSongs.isEmpty()) return;

        visibleSongs = allSongs;

        if (!TextUtils.isEmpty(filterQuery)) {
            visibleSongs = new ArrayList<>();
            for (Song song : allSongs) {
                if (song.search(filterQuery)) {
                    visibleSongs.add(song);
                }
            }
        }

        SongSortUtil.sort(activityRef, listId, visibleSongs);

        notifyDataSetChanged();
    }

    /**
     * Gets a random song from the filtered list.
     */
    public Song getRandomRequestSong() {
        List<Song> songs = getSongs();
        return songs.isEmpty() ? null : songs.get(new Random().nextInt(songs.size()));
    }

    private List<Song> getSongs() {
        return visibleSongs;
    }

    private Activity getActivity() {
        return activity.get();
    }

    private static class SongViewHolder extends RecyclerView.ViewHolder {

        private SongItemBinding binding;

        SongViewHolder(SongItemBinding binding, SongAdapter adapter) {
            super(binding.getRoot());

            this.binding = binding;

            binding.getRoot().setOnClickListener(v -> {
                if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                    Song song = adapter.getSongs().get(getLayoutPosition());
                    SongActionsUtil.showSongsDialog(adapter.getActivity(), null, song);
                }
            });

            binding.getRoot().setOnLongClickListener(v -> {
                Song song = adapter.getSongs().get(getLayoutPosition());
                SongActionsUtil.copyToClipboard(adapter.getActivity(), song);
                return true;
            });
        }

        void bind(Song song) {
            binding.setSong(song);

            binding.executePendingBindings();
        }
    }

}
