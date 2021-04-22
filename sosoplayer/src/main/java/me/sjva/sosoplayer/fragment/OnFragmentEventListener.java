package me.sjva.sosoplayer.fragment;

import com.google.android.exoplayer2.ext.plex.Directory;
import com.google.android.exoplayer2.ext.plex.Video;

public interface OnFragmentEventListener {
  void onSectionSelect(Directory directory);
  void onContentSelect(Directory directory);

  void onItemSelect(Video video);
  void onItemLongSelect(Video video);
  void onError( Throwable t);

  void onLoadingStart();
  void onLoadingEnd();
}
