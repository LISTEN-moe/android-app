package me.echeung.moemoekyun.client.model;

import android.text.TextUtils;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import me.echeung.moemoekyun.App;

@Getter
@Builder
public class SongDescriptor {
    private int id;
    private String name;
    private String nameRomaji;
    private String image;
    private String releaseDate;

    public static String getSongDescriptorsString(List<SongDescriptor> songDescriptors) {
        boolean preferRomaji = App.getPreferenceUtil().shouldPreferRomaji();

        StringBuilder s = new StringBuilder();
        if (songDescriptors != null) {
            for (SongDescriptor songDescriptor : songDescriptors) {
                if (songDescriptor == null || songDescriptor.getName() == null) {
                    continue;
                }

                if (s.length() != 0) {
                    s.append(", ");
                }

                if (preferRomaji && !TextUtils.isEmpty(songDescriptor.getNameRomaji())) {
                    s.append(songDescriptor.getNameRomaji());
                } else {
                    s.append(songDescriptor.getName());
                }
            }
        }
        return s.toString();
    }
}
