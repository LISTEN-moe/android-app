package me.echeung.listenmoeapi.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import lombok.Getter;

@Getter
public class Track implements Parcelable {

    private int id;
    private String title;
    private List<SongDescriptor> albums;
    private List<SongDescriptor> artists;
    private List<String> source;
    private int duration;

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
    }

    public Track(Parcel in) {
        this.id = in.readInt();
        this.title = in.readString();
        in.readTypedList(this.albums, SongDescriptor.CREATOR);
        in.readTypedList(this.artists, SongDescriptor.CREATOR);
        in.readStringList(this.source);
        this.duration = in.readInt();
    }

    public static final Creator CREATOR = new Creator() {
        public Track createFromParcel(Parcel in) {
            return new Track(in);
        }

        public Track[] newArray(int size) {
            return new Track[size];
        }
    };

}
