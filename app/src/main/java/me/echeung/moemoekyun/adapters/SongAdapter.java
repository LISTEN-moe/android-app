package me.echeung.moemoekyun.adapters;

import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import me.echeung.moemoekyun.BR;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.api.v3.model.Song;
import me.echeung.moemoekyun.databinding.SongItemBinding;

public class SongAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Song> songs;
    private WeakReference<OnSongItemClickListener> listener;

    public SongAdapter(OnSongItemClickListener listener) {
        this.songs = new ArrayList<>();
        this.listener = new WeakReference<>(listener);

        setHasStableIds(true);
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        final SongItemBinding binding = DataBindingUtil.inflate(layoutInflater, R.layout.song_item, parent, false);
        return new SongHolder(binding, this);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Song song = songs.get(position);

        final SongHolder songHolder = (SongHolder) holder;

        songHolder.bind(song);
    }

    @Override
    public long getItemId(int position) {
        return songs.get(position).getId();
    }

    @Override
    public int getItemCount() {
        return songs != null ? songs.size() : 0;
    }

    protected List<Song> getSongs() {
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
