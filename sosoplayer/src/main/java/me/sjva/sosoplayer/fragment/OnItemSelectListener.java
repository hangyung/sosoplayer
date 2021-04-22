package me.sjva.sosoplayer.fragment;

import java.util.ArrayList;
import me.sjva.sosoplayer.data.FileInfo;

public interface OnItemSelectListener {
  void onItemSelect(String name, ArrayList<FileInfo> fileInfos);
  void onItemSelect(FileInfo fileInfo);
  void onItemLongSelect(FileInfo fileInfo);
}
