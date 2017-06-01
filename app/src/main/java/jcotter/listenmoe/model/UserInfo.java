package jcotter.listenmoe.model;

public class UserInfo {
    private int id;
    private String username;
    // private Object permissions;

    public UserInfo(int id, String username) {
        this.id = id;
        this.username = username;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }
}
