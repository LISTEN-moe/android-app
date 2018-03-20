package me.echeung.listenmoeapi.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.echeung.listenmoeapi.APIClient;

@Getter
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Song implements Parcelable {
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

    public String getAlbumsString() {
        StringBuilder s = new StringBuilder();
        if (albums != null) {
            for (SongDescriptor album : albums) {
                if (s.length() != 0)
                    s.append(", ");
                s.append(album.getName());
            }
        }
        return s.toString();
    }

    public String getArtistsString() {
        StringBuilder s = new StringBuilder();
        if (artists != null) {
            for (SongDescriptor artist : artists) {
                if (s.length() != 0)
                    s.append(", ");
                s.append(artist.getName());
            }
        }
        return s.toString();
    }

    public String getSourcesString() {
        StringBuilder s = new StringBuilder();
        if (artists != null) {
            for (SongDescriptor source : sources) {
                if (s.length() != 0)
                    s.append(", ");
                s.append(source.getName());
            }
        }
        return s.toString();
    }

    @Override
    public String toString() {
        return String.format("%s - %s", getTitle(), getArtistsString());
    }

    public String getAlbumArtUrl() {
        if (!albums.isEmpty()) {
            for (SongDescriptor album : albums) {
                if (album.getImage() != null) {
                    return APIClient.CDN_ALBUM_ART_URL + album.getImage();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(title);
        parcel.writeString(titleRomaji);
        parcel.writeString(titleSearchRomaji);
        parcel.writeTypedList(albums);
        parcel.writeTypedList(artists);
        parcel.writeTypedList(sources);
        parcel.writeStringList(groups);
        parcel.writeStringList(tags);
        parcel.writeString(notes);
        parcel.writeInt(duration);
        parcel.writeByte(enabled ? (byte) 1 : 0);
        parcel.writeParcelable(uploader, 0);
        parcel.writeByte(favorite ? (byte) 1 : 0);
    }

    public Song(Parcel in) {
        this.id = in.readInt();
        this.title = in.readString();
        this.titleRomaji = in.readString();
        this.titleSearchRomaji = in.readString();
        in.readTypedList(this.albums, SongDescriptor.CREATOR);
        in.readTypedList(this.artists, SongDescriptor.CREATOR);
        in.readTypedList(this.sources, SongDescriptor.CREATOR);
        in.readStringList(this.groups);
        in.readStringList(this.tags);
        this.notes = in.readString();
        this.duration = in.readInt();
        this.enabled = in.readByte() == 1;
        this.uploader = in.readParcelable(User.class.getClassLoader());
        this.favorite = in.readByte() == 1;
    }

    public static final Creator CREATOR = new Creator() {
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

}
