package me.echeung.moemoekyun.adapters;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.databinding.SongDetailsBinding;
import me.echeung.moemoekyun.models.Song;
import me.echeung.moemoekyun.utils.SongActionsUtil;

public class SongDetailAdapter extends ArrayAdapter<Song> {

    private Activity activity;

    public SongDetailAdapter(@NonNull Activity activity, List<Song> songs) {
        super(activity, 0, songs);

        this.activity = activity;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(getContext());
        final SongDetailsBinding binding = DataBindingUtil.inflate(inflater, R.layout.song_details, parent, false);

        final Song song = getItem(position);
        binding.setSong(song);
        binding.setIsFavorite(song.isFavorite());

        binding.requestBtn.setOnClickListener(view -> {
            SongActionsUtil.request(activity, song);
        });

        binding.favoriteBtn.setOnClickListener(view -> {
            SongActionsUtil.toggleFavorite(activity, song);

            song.setFavorite(!song.isFavorite());
            binding.setIsFavorite(song.isFavorite());
        });

        binding.getRoot().setOnLongClickListener(view -> {
            SongActionsUtil.copyToClipboard(view.getContext(), song);
            return true;
        });

        return binding.getRoot();
    }

}
