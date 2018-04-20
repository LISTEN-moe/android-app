package me.echeung.moemoekyun.api.callback;

import java.util.List;

import me.echeung.moemoekyun.model.SongListItem;

public interface SongsCallback extends BaseCallback {
    void onSuccess(List<SongListItem> songs);
}
