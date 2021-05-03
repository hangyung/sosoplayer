package me.sjva.sosoplayer.activity;

import com.google.android.exoplayer2.MediaItem;

import java.util.ArrayList;

public interface OnPlayerEventListener {
    void onStartPlayer(ArrayList<MediaItem> mediaItems);
    void onPlexError(Throwable t);
}
