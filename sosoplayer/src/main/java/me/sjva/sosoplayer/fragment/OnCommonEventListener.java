package me.sjva.sosoplayer.fragment;

import com.google.android.exoplayer2.ext.plex.Directory;
import com.google.android.exoplayer2.ext.plex.Video;

import java.util.List;

import me.sjva.sosoplayer.data.FileInfo;

public interface OnListSelectEventListener {
  void onSectionSelect(Directory directory);
  void onContentSelect(Directory directory);

  void onItemSelect(List<Video> videos, int position);
  void onItemLongSelect(Video video);
  void onError( Throwable t);

  void onItemSelect(String name);
  void onItemSelect(FileInfo fileInfo);
  void onItemLongSelect(FileInfo fileInfo);


  void onLoadingStart();
  void onLoadingEnd();
}
