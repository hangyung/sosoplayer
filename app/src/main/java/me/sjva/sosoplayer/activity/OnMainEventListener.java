package me.sjva.sosoplayer.activity;

import android.os.Parcelable;

import me.sjva.sosoplayer.data.FileInfo;
import me.sjva.sosoplayer.data.StorageInfo;

public interface OnMainEventListener {
  void onAddStorage(StorageInfo storageInfo);
  void onModifyStorage(StorageInfo orgStorageInfo, StorageInfo newStorageInfo);
  void onRemoveStorage(StorageInfo storageInfo);
  void onError(String message);
  void onPlexError(Throwable t);
}
