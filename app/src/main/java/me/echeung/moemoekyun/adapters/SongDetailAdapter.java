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

public class SongDetailAdapter extends ArrayAdapter<Song> {

    public SongDetailAdapter(@NonNull Context context, List<Song> songs) {
        super(context, 0, songs);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        Song song = getItem(position);
        TextView tvName = convertView.findViewById(android.R.id.text1);
        tvName.setText(song.toString());

        return convertView;
    }

}
