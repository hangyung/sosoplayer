package me.sjva.sosoplayer.fragment;

import com.google.android.exoplayer2.ext.plex.Directory;
import com.google.android.exoplayer2.ext.plex.Video;

import java.util.ArrayList;
import java.util.List;

import me.sjva.sosoplayer.data.FileInfo;

public interface OnCommonEventListener {
  void onSectionSelect(Directory directory);
  void onContentSelect(Directory directory);

  void onPlexItemSelect(ArrayList<Video> videos, int position);
  void onPlexItemLongSelect(ArrayList<Video> videos,  int postion);
  void onError( Throwable t);

  void onItemSelect(String name);
  void onItemSelect(ArrayList<FileInfo> fileInfos, int postion);
  void onItemLongSelect(ArrayList<FileInfo> fileInfos, int postion);


  void onLoadingStart();
  void onLoadingEnd();
}
