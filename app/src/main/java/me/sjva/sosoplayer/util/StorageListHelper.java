package me.sjva.sosoplayer.util;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;

public class StorageListHelper implements Parcelable {
  private ArrayList<String> storageInfos;

  public StorageListHelper() {
    storageInfos = new ArrayList<String>();
  }
  public void addStorageInfoString(String storageInfoString){
    storageInfos.add(storageInfoString);
  }

  public ArrayList<String> getStorageInfos() {
    return storageInfos;
  }

  protected StorageListHelper(Parcel in) {
    storageInfos = in.createStringArrayList();
  }

  public static final Creator<StorageListHelper> CREATOR = new Creator<StorageListHelper>() {
    @Override
    public StorageListHelper createFromParcel(Parcel in) {
      return new StorageListHelper(in);
    }

    @Override
    public StorageListHelper[] newArray(int size) {
      return new StorageListHelper[size];
    }
  };

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    parcel.writeStringList(storageInfos);
  }
}
