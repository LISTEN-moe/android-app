package me.echeung.moemoekyun.ui.dialogs;

import android.app.AlertDialog;
import android.content.Context;

import java.util.List;

import me.echeung.listenmoeapi.models.Song;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.adapters.SongDetailAdapter;

public class SongDetailsDialog {

    public SongDetailsDialog(Context context, List<Song> songs) {
        SongDetailAdapter adapter = new SongDetailAdapter(context, songs);

        new AlertDialog.Builder(context, R.style.DialogTheme)
                .setTitle(R.string.last_played)
                .setAdapter(adapter, null)
                .setPositiveButton(R.string.close, null)
                .create()
                .show();
    }

}
