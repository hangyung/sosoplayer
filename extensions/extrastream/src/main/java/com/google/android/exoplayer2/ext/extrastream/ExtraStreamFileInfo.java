package com.google.android.exoplayer2.ext.extrastream;

import android.os.Parcel;
import android.os.Parcelable;

public class ExtraStreamFileInfo implements Parcelable {
  String path;
  String name;
  boolean isDirectory;
  long length;

  public ExtraStreamFileInfo(String path, String name, boolean isDirectory, long length) {
    this.path = path;
    this.name = name;
    this.isDirectory = isDirectory;
    this.length = length;
  }

  protected ExtraStreamFileInfo(Parcel in) {
    path= in.readString();
    name = in.readString();
    isDirectory = in.readByte() != 0;
    length = in.readLong();
  }

  public static final Creator<ExtraStreamFileInfo> CREATOR = new Creator<ExtraStreamFileInfo>() {
    @Override
    public ExtraStreamFileInfo createFromParcel(Parcel in) {
      return new ExtraStreamFileInfo(in);
    }

    @Override
    public ExtraStreamFileInfo[] newArray(int size) {
      return new ExtraStreamFileInfo[size];
    }
  };

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    parcel.writeString(path);
    parcel.writeString(name);
    parcel.writeByte((byte) (isDirectory ? 1 : 0));
    parcel.writeLong(length);
  }

  public String getName() {
    return name;
  }

  public boolean isDirectory() {
    return isDirectory;
  }

  public long getLength() {
    return length;
  }

  public String getPath() {
    return path;
  }
}
