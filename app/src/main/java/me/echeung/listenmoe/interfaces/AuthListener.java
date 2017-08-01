package me.echeung.listenmoe.interfaces;

public interface AuthListener {
    void onFailure(final String result);

    void onSuccess(final String result);
}
