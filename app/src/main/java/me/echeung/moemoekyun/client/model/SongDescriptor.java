package me.echeung.moemoekyun.client.model;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SongDescriptor {
    private int id;
    private String name;
    private String nameRomaji;
    private String image;
    private String releaseDate;

    public static String getSongDescriptorsString(List<SongDescriptor> songDescriptors) {
        StringBuilder s = new StringBuilder();
        if (songDescriptors != null) {
            for (SongDescriptor songDescriptor : songDescriptors) {
                if (songDescriptor == null || songDescriptor.getName() == null) {
                    continue;
                }

                if (s.length() != 0) {
                    s.append(", ");
                }

                s.append(songDescriptor.getName());
            }
        }
        return s.toString();
    }
}
