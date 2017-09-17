package me.echeung.moemoekyun.adapters;

import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import me.echeung.listenmoeapi.models.Song;
import me.echeung.moemoekyun.BR;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.databinding.SongItemBinding;

public class SongAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String SORT_NAME = "song_sort_name";
    public static final String SORT_NAME_DESC = SORT_NAME + ".desc";
    public static final String SORT_ARTIST = "song_sort_artist";
    public static final String SORT_ARTIST_DESC = SORT_ARTIST + ".desc";

    private List<Song> allSongs;
    private List<Song> filteredSongs;

    private String filterQuery;

    private WeakReference<OnSongItemClickListener> listener;

    public SongAdapter(OnSongItemClickListener listener) {
        this.listener = new WeakReference<>(listener);

        setHasStableIds(true);
    }

    public void setSongs(List<Song> songs) {
        this.allSongs = songs;
        filter(filterQuery);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        final SongItemBinding binding = DataBindingUtil.inflate(layoutInflater, R.layout.song_item, parent, false);
        return new SongHolder(binding, this);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Song song = filteredSongs.get(position);
        final SongHolder songHolder = (SongHolder) holder;
        songHolder.bind(song);
    }

    @Override
    public long getItemId(int position) {
        return filteredSongs.get(position).getId();
    }

    @Override
    public int getItemCount() {
        return filteredSongs != null ? filteredSongs.size() : 0;
    }

    public void filter(String query) {
        this.filterQuery = query;

        if (allSongs == null || allSongs.isEmpty()) return;

        if (TextUtils.isEmpty(filterQuery)) {
            filteredSongs = allSongs;
        } else {
            filteredSongs = new ArrayList<>();
            for (final Song song : allSongs) {
                if (song.getTitle().toLowerCase().contains(query) ||
                        song.getArtistAndAnime().toLowerCase().contains(query)) {
                    filteredSongs.add(song);
                }
            }
        }

        notifyDataSetChanged();
    }

    protected List<Song> getSongs() {
        return filteredSongs;
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
        }

        void bind(final Song song) {
            binding.setVariable(BR.title, song.getTitle());
            binding.setVariable(BR.subtitle, song.getArtistAndAnime());
            binding.setVariable(BR.favorited, song.isFavorite());

            final int typeface = song.isEnabled() ? Typeface.NORMAL : Typeface.ITALIC;
            binding.txtTitle.setTypeface(null, typeface);
            binding.txtSubtitle.setTypeface(null, typeface);

            binding.executePendingBindings();
        }
    }
}
