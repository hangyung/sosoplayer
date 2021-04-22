package com.google.android.exoplayer2.ext.plex;

import android.os.Parcel;
import android.os.Parcelable;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "Genre", strict=false)
public class Genre implements Parcelable {
  @Attribute(name = "tag", required=false)
  private String tag;

  public Genre() {

  }

  protected Genre(Parcel in) {
    tag = in.readString();
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(tag);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<Genre> CREATOR = new Creator<Genre>() {
    @Override
    public Genre createFromParcel(Parcel in) {
      return new Genre(in);
    }

    @Override
    public Genre[] newArray(int size) {
      return new Genre[size];
    }
  };

  public String getTag() {
    return tag;
  }
}
