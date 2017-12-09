package me.echeung.moemoekyun.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import me.echeung.listenmoeapi.models.Song;
import me.echeung.moemoekyun.BR;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.databinding.SongItemBinding;
import me.echeung.moemoekyun.utils.SongActionsUtil;
import me.echeung.moemoekyun.utils.SongSortUtil;

public class SongAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private String listId;
    private WeakReference<OnSongItemClickListener> listener;

    private List<Song> allSongs;
    private List<Song> visibleSongs;

    private String filterQuery;

    public SongAdapter(Context context, String listId, OnSongItemClickListener listener) {
        this.context = context;
        this.listId = listId;
        this.listener = new WeakReference<>(listener);

        setHasStableIds(true);
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
        SongSortUtil.setListSortType(context, listId, sortType);
        updateSongs();
    }

    public void sortDescending(boolean descending) {
        SongSortUtil.setListSortDescending(context, listId, descending);
        updateSongs();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        final SongItemBinding binding = DataBindingUtil.inflate(layoutInflater, R.layout.song_item, parent, false);
        return new SongHolder(binding, this);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Song song = visibleSongs.get(position);
        final SongHolder songHolder = (SongHolder) holder;
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

    private void updateSongs() {
        if (allSongs == null || allSongs.isEmpty()) return;

        visibleSongs = allSongs;

        if (!TextUtils.isEmpty(filterQuery)) {
            visibleSongs = new ArrayList<>();
            for (final Song song : allSongs) {
                if (song.getTitle().toLowerCase().contains(filterQuery) ||
                        song.getArtistAndAnime().toLowerCase().contains(filterQuery)) {
                    visibleSongs.add(song);
                }
            }
        }

        SongSortUtil.sort(context, listId, visibleSongs);

        notifyDataSetChanged();
    }

    /**
     * Gets a random song from the filtered list, which isn't on cool down.
     */
    public Song getRandomRequestSong() {
        List<Song> songs = getEnabledVisibleSongs();
        return songs.isEmpty() ? null : songs.get(new Random().nextInt(songs.size()));
    }

    private List<Song> getSongs() {
        return visibleSongs;
    }

    private List<Song> getEnabledVisibleSongs() {
        List<Song> songs = new ArrayList<>();
        if (visibleSongs != null) {
            for (Song song : visibleSongs) {
                if (song != null && song.isEnabled()) {
                    songs.add(song);
                }
            }
        }
        return songs;
    }

    protected OnSongItemClickListener getListener() {
        return listener.get();
    }

    public interface OnSongItemClickListener {
        void onSongItemClick(final Song song);
    }

    private static class SongHolder extends RecyclerView.ViewHolder {

        private SongItemBinding binding;

        SongHolder(final SongItemBinding binding, final SongAdapter adapter) {
            super(binding.getRoot());

            this.binding = binding;

            binding.getRoot().setOnClickListener(v -> {
                if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                    final OnSongItemClickListener listener = adapter.getListener();
                    if (listener != null) {
                        final Song song = adapter.getSongs().get(getLayoutPosition());
                        listener.onSongItemClick(song);
                    }
                }
            });

            binding.getRoot().setOnLongClickListener(v -> {
                final Song song = adapter.getSongs().get(getLayoutPosition());
                SongActionsUtil.copyToClipboard(adapter.context, song);
                return true;
            });
        }

        void bind(final Song song) {
            binding.setVariable(BR.title, song.getTitle());
            binding.setVariable(BR.subtitle, song.getArtistAndAnime());
            binding.setVariable(BR.favorited, song.isFavorite());
            binding.setVariable(BR.enabled, song.isEnabled());

            binding.executePendingBindings();
        }
    }
}
