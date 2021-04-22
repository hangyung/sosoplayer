package com.google.android.exoplayer2.ext.plex;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.List;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "Part", strict=false)
public class Part implements Parcelable {
  @Attribute(name = "id", required=false)
  private String id;

  @Attribute(name = "key", required=false)
  private String key;

  @Attribute(name = "duration", required=false)
  private String duration;

  @Attribute(name = "file", required=false)
  private String file;

  @Attribute(name = "size", required=false)
  private String size;

  @Attribute(name = "container", required=false)
  private String container;

  @ElementList(name = "Stream", required=false, inline=true)
  private List<Stream> stream;

  public Part() {

  }


  protected Part(Parcel in) {
    id = in.readString();
    key = in.readString();
    duration = in.readString();
    file = in.readString();
    size = in.readString();
    container = in.readString();
    stream = in.createTypedArrayList(Stream.CREATOR);
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(id);
    dest.writeString(key);
    dest.writeString(duration);
    dest.writeString(file);
    dest.writeString(size);
    dest.writeString(container);
    dest.writeTypedList(stream);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<Part> CREATOR = new Creator<Part>() {
    @Override
    public Part createFromParcel(Parcel in) {
      return new Part(in);
    }

    @Override
    public Part[] newArray(int size) {
      return new Part[size];
    }
  };

  public String getId() {
    return id;
  }

  public String getKey() {
    return key;
  }

  public String getDuration() {
    return duration;
  }

  public String getFile() {
    return file;
  }

  public String getSize() {
    return size;
  }

  public String getContainer() {
    return container;
  }

  public List<Stream> getStream() {
    return stream;
  }
}
