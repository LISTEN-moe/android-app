package me.echeung.listenmoeapi.callbacks;

import java.util.List;

import me.echeung.listenmoeapi.models.SongListItem;

public interface SongsCallback extends BaseCallback {
    void onSuccess(List<SongListItem> songs);
}
