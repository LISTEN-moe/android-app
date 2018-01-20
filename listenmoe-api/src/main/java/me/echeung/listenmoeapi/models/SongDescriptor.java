package me.echeung.listenmoeapi.models;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SongDescriptor implements Parcelable {
    private int id;
    private String name;
    private String nameRomaji;
    private String image;
    private String releaseDate;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(name);
        parcel.writeString(nameRomaji);
        parcel.writeString(image);
        parcel.writeString(releaseDate);
    }

    public SongDescriptor(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.nameRomaji = in.readString();
        this.image = in.readString();
        this.releaseDate = in.readString();
    }

    public static final Creator CREATOR = new Creator() {
        public SongDescriptor createFromParcel(Parcel in) {
            return new SongDescriptor(in);
        }

        public SongDescriptor[] newArray(int size) {
            return new SongDescriptor[size];
        }
    };
}
