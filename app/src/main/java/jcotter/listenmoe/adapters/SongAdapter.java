package jcotter.listenmoe.adapters;

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
import jcotter.listenmoe.R;
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

        SongHolder(final View view, final List<Song> songs, final OnSongItemClickListener listener) {
            super(view);
            ButterKnife.bind(this, view);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                        final Song song = songs.get(getLayoutPosition());
                        listener.onSongItemClick(song);
                    }
                }
            });
        }
    }
}
