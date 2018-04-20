package me.echeung.moemoekyun.client.api.callback;

import java.util.List;

import me.echeung.moemoekyun.client.model.SongListItem;

public interface SongsCallback extends BaseCallback {
    void onSuccess(List<SongListItem> songs);
}
