package me.echeung.listenmoeapi.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import lombok.Getter;

@Getter
public class Song implements Parcelable {

    private int id;
    private String title;
    private List<SongDescriptor> albums;
    private List<SongDescriptor> artists;
    private List<String> source;
    private int duration;
    private boolean favorite;

    public String getAlbumString() {
        StringBuilder s = new StringBuilder();
        for (SongDescriptor album : albums) {
            if (s.length() != 0)
                s.append(", ");
            s.append(album.getName());
        }
        return s.toString();
    }

    public String getArtistString() {
        StringBuilder s = new StringBuilder();
        for (SongDescriptor artist : artists) {
            if (s.length() != 0)
                s.append(", ");
            s.append(artist.getName());
        }
        return s.toString();
    }

    public String getSourceString() {
        return String.join(", ", source);
    }

    @Override
    public String toString() {
        return String.format("%s - %s - %s", title, getArtistString(), getAlbumString());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(title);
        parcel.writeTypedList(albums);
        parcel.writeTypedList(artists);
        parcel.writeStringList(source);
        parcel.writeInt(duration);
        parcel.writeByte(favorite ? (byte) 1 : 0);
    }

    public Song(Parcel in) {
        this.id = in.readInt();
        this.title = in.readString();
        in.readTypedList(this.albums, SongDescriptor.CREATOR);
        in.readTypedList(this.artists, SongDescriptor.CREATOR);
        in.readStringList(this.source);
        this.duration = in.readInt();
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
