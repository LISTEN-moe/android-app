package me.echeung.moemoekyun.ui.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.List;

import me.echeung.listenmoeapi.models.Song;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.utils.SongActionsUtil;

public class SongDetailsDialog {

    private AlertDialog dialog;

    public SongDetailsDialog(Context context, List<Song> songs) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1);
        for (Song song : songs) {
            adapter.add(song.toString());
        }

        dialog = new AlertDialog.Builder(context, R.style.DialogTheme)
                .setAdapter(adapter, (dialogInterface, i) -> SongActionsUtil.copyToClipboard(context, songs.get(i)))
                .setPositiveButton(R.string.close, null)
                .create();
    }

    public void show() {
        dialog.show();
    }

}
