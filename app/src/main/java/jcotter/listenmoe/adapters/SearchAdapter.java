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

public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public interface OnItemClickListener {
        void onItemClick(final Song item);
    }

    private List<Song> songs;
    private OnItemClickListener listener;

    public SearchAdapter(OnItemClickListener listener) {
        this.songs = new ArrayList<>();
        this.listener = listener;
    }

    public void setResults(List<Song> songs) {
        this.songs = songs;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_item, parent, false);
        return new SearchItemHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Song song = songs.get(position);

        ((SearchItemHolder) holder).mTitle.setText(song.getTitle());
        ((SearchItemHolder) holder).mSubtitle.setText(song.getArtistAndAnime());
        ((SearchItemHolder) holder).mFavorited.setVisibility(song.isFavorite() ? View.VISIBLE : View.GONE);

        if (!song.isEnabled()) {
            ((SearchItemHolder) holder).mTitle.setTypeface(null, Typeface.ITALIC);
            ((SearchItemHolder) holder).mSubtitle.setTypeface(null, Typeface.ITALIC);
        }
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    private class SearchItemHolder extends RecyclerView.ViewHolder {
        TextView mTitle;
        TextView mSubtitle;
        LinearLayout mFavorited;

        SearchItemHolder(View itemView) {
            super(itemView);

            mTitle = (TextView) itemView.findViewById(R.id.title);
            mSubtitle = (TextView) itemView.findViewById(R.id.subtitle);
            mFavorited = (LinearLayout) itemView.findViewById(R.id.favorited);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                        final Song song = songs.get(getLayoutPosition());
                        listener.onItemClick(song);
                    }
                }
            });
        }
    }
}
