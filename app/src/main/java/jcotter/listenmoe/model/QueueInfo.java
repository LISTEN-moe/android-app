package jcotter.listenmoe.model;

public class QueueInfo {
    private int songsInQueue;
    private boolean hasSongInQueue;
    private int inQueueBeforeUserSong;
    private int userSongsInQueue;

    public QueueInfo(int songsInQueue, boolean hasSongInQueue, int inQueueBeforeUserSong, int userSongsInQueue) {
        this.songsInQueue = songsInQueue;
        this.hasSongInQueue = hasSongInQueue;
        this.inQueueBeforeUserSong = inQueueBeforeUserSong;
        this.userSongsInQueue = userSongsInQueue;
    }

    public int getSongsInQueue() {
        return songsInQueue;
    }

    public boolean isHasSongInQueue() {
        return hasSongInQueue;
    }

    public int getInQueueBeforeUserSong() {
        return inQueueBeforeUserSong;
    }

    public int getUserSongsInQueue() {
        return userSongsInQueue;
    }
}
