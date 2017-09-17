package me.echeung.moemoekyun.adapters;

import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.echeung.listenmoeapi.models.Song;
import me.echeung.moemoekyun.BR;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.databinding.SongItemBinding;

public class SongAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String SORT_TITLE = "song_sort_title";
    public static final String SORT_TITLE_DESC = SORT_TITLE + ".desc";
    public static final String SORT_ARTIST = "song_sort_artist";
    public static final String SORT_ARTIST_DESC = SORT_ARTIST + ".desc";

    private List<Song> allSongs;
    private List<Song> visibleSongs;

    private String filterQuery;
    private String sortType;

    private WeakReference<OnSongItemClickListener> listener;

    public SongAdapter(OnSongItemClickListener listener) {
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

    public void sort(String sortType) {
        this.sortType = sortType;
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

        if (sortType != null) {
            switch (sortType) {
                case SORT_ARTIST:
                    Collections.sort(visibleSongs, (song, t1) -> song.getArtist().compareToIgnoreCase(t1.getArtist()));
                    break;

                case SORT_ARTIST_DESC:
                    Collections.sort(visibleSongs, (song, t1) -> t1.getArtist().compareToIgnoreCase(song.getArtist()));
                    break;

                case SORT_TITLE_DESC:
                    Collections.sort(visibleSongs, (song, t1) -> t1.getTitle().compareToIgnoreCase(song.getTitle()));
                    break;

                case SORT_TITLE:
                default:
                    Collections.sort(visibleSongs, (song, t1) -> song.getTitle().compareToIgnoreCase(t1.getTitle()));
                    break;
            }
        }

        notifyDataSetChanged();
    }

    protected List<Song> getSongs() {
        return visibleSongs;
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
