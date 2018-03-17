package me.echeung.moemoekyun.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import me.echeung.listenmoeapi.models.Song;
import me.echeung.moemoekyun.R;

public class SongDetailAdapter extends ArrayAdapter<Song> {

    public SongDetailAdapter(@NonNull Context context, List<Song> songs) {
        super(context, 0, songs);
    }

    @NonNull
    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.song_detail_item, parent, false);
        }

        final Song song = getItem(position);

        ((TextView) view.findViewById(R.id.title)).setText(song.getTitle());
        ((TextView) view.findViewById(R.id.artist)).setText(song.getArtistString());

        return view;
    }

}
