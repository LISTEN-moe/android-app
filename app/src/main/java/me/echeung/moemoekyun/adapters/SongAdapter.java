package me.echeung.moemoekyun.adapters;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.model.Song;

public class SongAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Song> songs;
    private OnSongItemClickListener listener;

    public SongAdapter(OnSongItemClickListener listener) {
        this.songs = new ArrayList<>();
        this.listener = listener;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_item, parent, false);
        return new SongHolder(v, this);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Song song = songs.get(position);

        final SongHolder songHolder = (SongHolder) holder;

        songHolder.mTitle.setText(song.getTitle());
        songHolder.mSubtitle.setText(song.getArtistAndAnime());
        songHolder.mFavorited.setVisibility(song.isFavorite() ? View.VISIBLE : View.GONE);

        final int typeface = song.isEnabled() ? Typeface.NORMAL : Typeface.ITALIC;
        songHolder.mTitle.setTypeface(null, typeface);
        songHolder.mSubtitle.setTypeface(null, typeface);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    protected List<Song> getSongs() {
        return songs;
    }

    protected OnSongItemClickListener getListener() {
        return listener;
    }

    public interface OnSongItemClickListener {
        void onSongItemClick(final Song song);
    }

    static class SongHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.title)
        TextView mTitle;
        @BindView(R.id.subtitle)
        TextView mSubtitle;
        @BindView(R.id.favorited)
        LinearLayout mFavorited;

        SongHolder(final View view, final SongAdapter adapter) {
            super(view);
            ButterKnife.bind(this, view);

            view.setOnClickListener(v -> {
                if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                    final Song song = adapter.getSongs().get(getLayoutPosition());
                    adapter.getListener().onSongItemClick(song);
                }
            });
        }
    }
}
