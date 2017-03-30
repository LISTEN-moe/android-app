package jcotter.listenmoe.model;

public class Song {
    private int id;
    private String artist;
    private String title;
    private String anime;
    private boolean enabled;
    private int favorite;

    public Song(int id, String artist, String title, String anime, boolean enabled, int favorite) {
        this.id = id;
        this.artist = artist;
        this.title = title;
        this.anime = anime;
        this.enabled = enabled;
        this.favorite = favorite;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAnime() {
        return anime;
    }

    public void setAnime(String anime) {
        this.anime = anime;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getFavorite() {
        return favorite;
    }

    public void setFavorite(int favorite) {
        this.favorite = favorite;
    }
}
