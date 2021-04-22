package com.google.android.exoplayer2.ext.plex;

import android.os.Parcel;
import android.os.Parcelable;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "Location", strict=false)
public class Location implements Parcelable {
  @Attribute(name = "id", required=false)
  private String id;

  @Attribute(name = "path", required=false)
  private String path;

  public Location() {

  }

  protected Location(Parcel in) {
    id = in.readString();
    path = in.readString();
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(id);
    dest.writeString(path);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<Location> CREATOR = new Creator<Location>() {
    @Override
    public Location createFromParcel(Parcel in) {
      return new Location(in);
    }

    @Override
    public Location[] newArray(int size) {
      return new Location[size];
    }
  };

  public String getId() {
    return id;
  }

  public String getPath() {
    return path;
  }
}
