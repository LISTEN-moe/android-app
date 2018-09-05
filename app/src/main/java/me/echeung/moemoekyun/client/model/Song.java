package me.echeung.moemoekyun.client.model;

import android.text.TextUtils;

import java.util.List;
import java.util.Locale;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import me.echeung.moemoekyun.App;
import me.echeung.moemoekyun.client.api.library.Library;

@Getter
@EqualsAndHashCode
@Builder
public class Song {
    private int id;
    private String title;
    private String titleRomaji;
    private String titleSearchRomaji;
    private List<SongDescriptor> albums;
    private List<SongDescriptor> artists;
    private List<SongDescriptor> sources;
    private List<String> groups;
    private List<String> tags;
    private String notes;
    private int duration;
    private boolean enabled;
    private User uploader;

    @Setter
    private boolean favorite;

    public String getTitleString() {
        if (App.getPreferenceUtil().shouldPreferRomaji() && !TextUtils.isEmpty(titleRomaji)) {
            return titleRomaji;
        }

        return title;
    }

    public String getAlbumsString() {
        return SongDescriptor.getSongDescriptorsString(albums);
    }

    public String getArtistsString() {
        return SongDescriptor.getSongDescriptorsString(artists);
    }

    public String getSourcesString() {
        return SongDescriptor.getSongDescriptorsString(sources);
    }

    public String getDurationString() {
        long minutes = duration / 60;
        long seconds = duration % 60;
        if (minutes < 60) {
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        } else {
            long hours = minutes / 60;
            minutes = minutes % 60;
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        }
    }

    @Override
    public String toString() {
        return String.format("%s - %s", getTitleString(), getArtistsString());
    }

    public String getAlbumArtUrl() {
        if (!albums.isEmpty()) {
            for (SongDescriptor album : albums) {
                if (album.getImage() != null) {
                    return Library.CDN_ALBUM_ART_URL + album.getImage();
                }
            }
        }

        return null;
    }

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
            for (SongDescriptor album : albums) {
                if (album.getName() != null && album.getName().toLowerCase().contains(query)) {
                    return true;
                }
                if (album.getNameRomaji() != null && album.getNameRomaji().toLowerCase().contains(query)) {
                    return true;
                }
            }
        }

        if (artists != null) {
            for (SongDescriptor artist : artists) {
                if (artist.getName() != null && artist.getName().toLowerCase().contains(query)) {
                    return true;
                }
                if (artist.getNameRomaji() != null && artist.getNameRomaji().toLowerCase().contains(query)) {
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

        if (tags != null) {
            for (String tag : tags) {
                if (tag != null && tag.toLowerCase().contains(query)) {
                    return true;
                }
            }
        }

        return false;
    }
}
