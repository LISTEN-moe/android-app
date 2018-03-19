package me.echeung.moemoekyun.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import me.echeung.listenmoeapi.models.Song;
import me.echeung.moemoekyun.R;

public class SongDetailAdapter extends ArrayAdapter<Song> {

    public SongDetailAdapter(@NonNull Context context, List<Song> songs) {
        super(context, R.layout.song_detail_item, songs);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();

            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.song_detail_item, parent, false);

            viewHolder.title = convertView.findViewById(R.id.title);
            viewHolder.artist = convertView.findViewById(R.id.artist);
            viewHolder.albumArt = convertView.findViewById(R.id.album_art);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final Song song = getItem(position);
        viewHolder.title.setText(song.getTitle());
        viewHolder.artist.setText(song.getArtistString());

        if (!TextUtils.isEmpty(song.getAlbumArtUrl())) {
            Glide.with(getContext())
                    .asBitmap()
                    .load(song.getAlbumArtUrl())
                    .apply(new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .centerCrop())
                    .into(viewHolder.albumArt);
        } else {
            viewHolder.albumArt.setBackgroundResource(R.drawable.blank);
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView title;
        TextView artist;
        ImageView albumArt;
    }

}
