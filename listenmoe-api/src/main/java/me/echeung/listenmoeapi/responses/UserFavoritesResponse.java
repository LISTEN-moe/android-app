package me.echeung.listenmoeapi.responses;

import java.util.List;

import lombok.Getter;
import me.echeung.listenmoeapi.models.Song;

@Getter
public class UserFavoritesResponse extends BaseResponse {
    private List<Song> songs;
    private SongsExtra extra;

    @Getter
    public class SongsExtra {
        private int requests;
    }
}
