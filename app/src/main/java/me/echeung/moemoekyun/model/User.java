package me.echeung.moemoekyun.model;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements Parcelable {
    private String uuid;
    private String email;
    private String username;
    private String displayName;
    private String avatarImage;
    private String bannerImage;
    private String bio;
    private int additionalRequests;
    private int uploads;
    private int uploadLimit;
    private int favorites;

    @Setter
    private int requestsRemaining;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(uuid);
        parcel.writeString(email);
        parcel.writeString(username);
        parcel.writeString(displayName);
        parcel.writeString(avatarImage);
        parcel.writeString(bannerImage);
        parcel.writeString(bio);
        parcel.writeInt(additionalRequests);
        parcel.writeInt(uploads);
        parcel.writeInt(uploadLimit);
        parcel.writeInt(favorites);
    }

    public User(Parcel in) {
        this.uuid = in.readString();
        this.email = in.readString();
        this.username = in.readString();
        this.displayName = in.readString();
        this.avatarImage = in.readString();
        this.bannerImage = in.readString();
        this.bio = in.readString();
        this.additionalRequests = in.readInt();
        this.uploads = in.readInt();
        this.uploadLimit = in.readInt();
        this.favorites = in.readInt();
    }

    public static final Creator CREATOR = new Creator() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };

}
