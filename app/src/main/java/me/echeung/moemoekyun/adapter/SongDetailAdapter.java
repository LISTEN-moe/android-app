package me.echeung.moemoekyun.adapter;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import me.echeung.moemoekyun.App;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.client.model.Song;
import me.echeung.moemoekyun.databinding.SongDetailsBinding;
import me.echeung.moemoekyun.util.SongActionsUtil;

public class SongDetailAdapter extends ArrayAdapter<Song> {

    private Activity activity;

    public SongDetailAdapter(@NonNull Activity activity, List<Song> songs) {
        super(activity, 0, songs);

        this.activity = activity;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        SongDetailsBinding binding;

        if (convertView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.song_details, parent, false);
            convertView = binding.getRoot();
            convertView.setTag(binding);
        } else {
            binding = (SongDetailsBinding) convertView.getTag();
        }

        Song song = getItem(position);
        binding.setSong(song);
        binding.setIsAuthenticated(App.getAuthUtil().isAuthenticated());
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
