package jcotter.listenmoe.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import jcotter.listenmoe.R;
import jcotter.listenmoe.interfaces.OnSongItemClickListener;
import jcotter.listenmoe.model.Song;

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