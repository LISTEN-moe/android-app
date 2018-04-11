package me.echeung.moemoekyun.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import me.echeung.listenmoeapi.models.Song;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.databinding.SongItemBinding;
import me.echeung.moemoekyun.utils.SongActionsUtil;

public class SongDetailAdapter extends ArrayAdapter<Song> {

    public SongDetailAdapter(@NonNull Context context, List<Song> songs) {
        super(context, 0, songs);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(getContext());
        final SongItemBinding binding = DataBindingUtil.inflate(inflater, R.layout.song_item, parent, false);

        final Song song = getItem(position);
        binding.setSong(song);

        binding.getRoot().setOnClickListener(view -> {
            // TODO: song actions
        });

        binding.getRoot().setOnLongClickListener(view -> {
            SongActionsUtil.copyToClipboard(view.getContext(), song);
            return true;
        });

        return binding.getRoot();
    }

}
