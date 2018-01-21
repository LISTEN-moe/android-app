package me.echeung.listenmoeapi.models;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

@Getter
public class SongListItem {
    private List<String> albums;
    private List<Integer> albumsId;
    private List<String> albumsCover;
    private List<String> albumsReleaseDate;
    private List<String> albumsRomaji;
    private List<String> albumsSearchRomaji;
    private List<Integer> albumsTrackNumber;
    private List<Integer> albumsType;
    private List<String> artists;
    private List<Integer> artistsId;
    private List<String> artistsRomaji;
    private List<String> artistsSearchRomaji;
    private int duration;
    private boolean enabled;
    private boolean favorite;
    private List<String> groups;
    private List<Integer> groupsId;
    private List<String> groupsRomaji;
    private List<String> groupsSearchRomaji;
    private int id;
    private String lastPlayed;
    private String snippet;
    private List<String> sources;
    private List<String> sourcesRomaji;
    private List<String> tags;
    private String title;
    private String titleRomaji;
    private String titleSearchRomaji;
    private String uploaderUuid;
    private String uploaderUsername;
    private String uploaderDisplayName;

    public boolean search(String query) {
        query = query.toLowerCase().trim();

        if (title != null && title.toLowerCase().contains(query)) {
            return true;
        }

        if (titleRomaji != null && titleRomaji.toLowerCase().contains(query)) {
            return true;
        }

        if (titleSearchRomaji != null && titleSearchRomaji.toLowerCase().contains(query)) {
            return true;
        }

        if (albums != null) {
            for (String album : albums) {
                if (album != null && album.toLowerCase().contains(query)) {
                    return true;
                }
            }
        }

        if (albumsRomaji != null) {
            for (String album : albumsRomaji) {
                if (album != null && album.toLowerCase().contains(query)) {
                    return true;
                }
            }
        }

        if (albumsSearchRomaji != null) {
            for (String album : albumsSearchRomaji) {
                if (album != null && album.toLowerCase().contains(query)) {
                    return true;
                }
            }
        }

        if (artists != null) {
            for (String artist : artists) {
                if (artist != null && artist.toLowerCase().contains(query)) {
                    return true;
                }
            }
        }

        if (artistsRomaji != null) {
            for (String artist : artistsRomaji) {
                if (artist != null && artist.toLowerCase().contains(query)) {
                    return true;
                }
            }
        }

        if (artistsSearchRomaji != null) {
            for (String artist : artistsSearchRomaji) {
                if (artist != null && artist.toLowerCase().contains(query)) {
                    return true;
                }
            }
        }

        if (groups != null) {
            for (String group : groups) {
                if (group != null && group.toLowerCase().contains(query)) {
                    return true;
                }
            }
        }

        if (groupsRomaji != null) {
            for (String group : groupsRomaji) {
                if (group != null && group.toLowerCase().contains(query)) {
                    return true;
                }
            }
        }

        if (groupsSearchRomaji != null) {
            for (String group : groupsSearchRomaji) {
                if (group != null && group.toLowerCase().contains(query)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static Song toSong(SongListItem song) {
        User uploader = User.builder()
                .uuid(song.getUploaderUuid())
                .displayName(song.getUploaderDisplayName())
                .username(song.getUploaderUsername())
                .build();

        List<SongDescriptor> albums = new ArrayList<>();
        for (int i = 0; i < song.getAlbums().size(); i++) {
            albums.add(SongDescriptor.builder()
                    .id(song.getAlbumsId().get(i))
                    .name(song.getAlbums().get(i))
                    .nameRomaji(song.getAlbumsRomaji().get(i))
                    .image(song.getAlbumsCover().get(i))
                    .releaseDate(song.getAlbumsReleaseDate().get(i))
                    .build());
        }

        List<SongDescriptor> artists = new ArrayList<>();
        for (int i = 0; i < song.getArtists().size(); i++) {
            artists.add(SongDescriptor.builder()
                    .id(song.getArtistsId().get(i))
                    .name(song.getArtists().get(i))
                    .nameRomaji(song.getArtistsRomaji().get(i))
                    .build());
        }

        return Song.builder()
                .id(song.getId())
                .title(song.getTitle())
                .titleRomaji(song.getTitleRomaji())
                .albums(albums)
                .artists(artists)
                .sources(song.getSources())
                .groups(song.getGroups())
                .tags(song.getTags())
                .duration(song.getDuration())
                .favorite(song.isFavorite())
                .enabled(song.isEnabled())
                .uploader(uploader)
                .build();
    }
}
