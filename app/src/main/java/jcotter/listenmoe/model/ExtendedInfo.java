package jcotter.listenmoe.model;

public class ExtendedInfo {
    private boolean favorite;
    private QueueInfo queue;

    public ExtendedInfo(boolean favorite, QueueInfo queue) {
        this.favorite = favorite;
        this.queue = queue;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public QueueInfo getQueue() {
        return queue;
    }

    public void setQueue(QueueInfo queue) {
        this.queue = queue;
    }
}
