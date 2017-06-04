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

import jcotter.listenmoe.R;
import jcotter.listenmoe.model.Song;

public class SongAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public interface OnSongItemClickListener {
        void onSongItemClick(final Song song);
    }

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

        final int typeface = song.isEnabled() ? Typeface.NORMAL : Typeface.ITALIC;
        ((SongHolder) holder).mTitle.setTypeface(null, typeface);
        ((SongHolder) holder).mSubtitle.setTypeface(null, typeface);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public class SongHolder extends RecyclerView.ViewHolder {
        public TextView mTitle;
        public TextView mSubtitle;
        public LinearLayout mFavorited;

        public SongHolder(final View itemView, final List<Song> songs, final OnSongItemClickListener listener) {
            super(itemView);

            mTitle = (TextView) itemView.findViewById(R.id.title);
            mSubtitle = (TextView) itemView.findViewById(R.id.subtitle);
            mFavorited = (LinearLayout) itemView.findViewById(R.id.favorited);

            itemView.setOnClickListener(new View.OnClickListener() {
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
