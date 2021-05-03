package me.sjva.sosoplayer.data;

import java.util.ArrayList;
import java.util.Collections;

class ExtraNormalStorageInfo implements  ExtraStorageInfo{
  private ArrayList<FileInfo> fileInfos;
  public ExtraNormalStorageInfo() {
    fileInfos = new ArrayList<FileInfo>();
  }

  public void clear() {
    fileInfos.clear();
  }

  public void addFileInfos(FileInfo fileInfo) {
    fileInfos.add(fileInfo);
  }

  public void sort() {
    Collections.sort(fileInfos, this::compare);
  }

  public int compare(FileInfo fileInfo, FileInfo fileInfo2) {
    if (fileInfo.isDirectory()) {
      if (fileInfo2.isDirectory()) {
        return fileInfo.getName().compareTo(fileInfo2.getName());
      }
      return -1;
    } else if (fileInfo2.isDirectory()) {
      return 1;
    }
    return fileInfo.getName().compareTo(fileInfo2.getName());
  }

  public ArrayList<FileInfo>  getFileInfos() {
    return fileInfos;
  }
}
