package com.google.android.exoplayer2.ext.plex;

import android.os.Parcel;
import android.os.Parcelable;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "Country", strict=false)
public class Country implements Parcelable {
  @Attribute(name = "tag", required=false)
  private String tag;

  public Country() {

  }

  protected Country(Parcel in) {
    tag = in.readString();
  }

  public static final Creator<Country> CREATOR = new Creator<Country>() {
    @Override
    public Country createFromParcel(Parcel in) {
      return new Country(in);
    }

    @Override
    public Country[] newArray(int size) {
      return new Country[size];
    }
  };

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    parcel.writeString(tag);
  }

  public String getTag() {
    return tag;
  }
}
