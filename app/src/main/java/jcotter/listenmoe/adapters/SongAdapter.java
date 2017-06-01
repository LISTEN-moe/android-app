package jcotter.listenmoe.adapters;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import jcotter.listenmoe.R;
import jcotter.listenmoe.holders.SongHolder;
import jcotter.listenmoe.interfaces.OnSongItemClickListener;
import jcotter.listenmoe.model.Song;

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
        return new SongHolder(v, songs, listener);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Song song = songs.get(position);

        ((SongHolder) holder).mTitle.setText(song.getTitle());
        ((SongHolder) holder).mSubtitle.setText(song.getArtistAndAnime());
        ((SongHolder) holder).mFavorited.setVisibility(song.isFavorite() ? View.VISIBLE : View.GONE);

        if (!song.isEnabled()) {
            ((SongHolder) holder).mTitle.setTypeface(null, Typeface.ITALIC);
            ((SongHolder) holder).mSubtitle.setTypeface(null, Typeface.ITALIC);
        }
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }
}
