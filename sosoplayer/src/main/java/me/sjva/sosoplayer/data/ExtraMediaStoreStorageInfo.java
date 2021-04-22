package me.sjva.sosoplayer.data;

import java.util.ArrayList;
import java.util.TreeMap;

class ExtraMediaStoreStorageInfo implements ExtraStorageInfo {
  TreeMap<String, ArrayList<FileInfo>> bucketMap;
  private String bucketFilter;
  public ExtraMediaStoreStorageInfo() {
    bucketFilter = null;
    bucketMap = new TreeMap<String, ArrayList<FileInfo>>();
  }

  public void clear() {
    bucketMap.clear();
  }

  public TreeMap<String, ArrayList<FileInfo>> getBucketMap() {
    return bucketMap;
  }

  public String getBucketFilter() {
    return bucketFilter;
  }

  public ArrayList<FileInfo> getBucketList() {
    return bucketMap.get(bucketFilter);
  }

  public void setBucketFilter(String bucketFilter) {
    this.bucketFilter = bucketFilter;
  }
}