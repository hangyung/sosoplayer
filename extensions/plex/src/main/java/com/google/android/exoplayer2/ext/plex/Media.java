package com.google.android.exoplayer2.ext.plex;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.List;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "Media", strict=false)
public class Media implements Parcelable {
  @Attribute(name = "videoResolution", required=false)
  private String videoResolution;

  @Attribute(name = "id", required=false)
  private String id;

  @Attribute(name = "duration", required=false)
  private String duration;

  @Attribute(name = "bitrate", required=false)
  private String bitrate;

  @Attribute(name = "aspectRatio", required=false)
  private String aspectRatio;

  @Attribute(name = "audioChannels", required=false)
  private String audioChannels;

  @Attribute(name = "videoCodec", required=false)
  private String videoCodec;

  @Attribute(name = "container", required=false)
  private String container;

  @ElementList(name = "Part", required=false, inline = true)
  private List<Part> part;

  public Media() {

  }


  protected Media(Parcel in) {
    videoResolution = in.readString();
    id = in.readString();
    duration = in.readString();
    bitrate = in.readString();
    aspectRatio = in.readString();
    audioChannels = in.readString();
    videoCodec = in.readString();
    container = in.readString();
    part = in.createTypedArrayList(Part.CREATOR);
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(videoResolution);
    dest.writeString(id);
    dest.writeString(duration);
    dest.writeString(bitrate);
    dest.writeString(aspectRatio);
    dest.writeString(audioChannels);
    dest.writeString(videoCodec);
    dest.writeString(container);
    dest.writeTypedList(part);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<Media> CREATOR = new Creator<Media>() {
    @Override
    public Media createFromParcel(Parcel in) {
      return new Media(in);
    }

    @Override
    public Media[] newArray(int size) {
      return new Media[size];
    }
  };

  public String getVideoResolution() {
    return videoResolution;
  }

  public String getId() {
    return id;
  }

  public String getDuration() {
    return duration;
  }

  public String getBitrate() {
    return bitrate;
  }

  public String getAspectRatio() {
    return aspectRatio;
  }

  public String getAudioChannels() {
    return audioChannels;
  }

  public String getVideoCodec() {
    return videoCodec;
  }

  public String getContainer() {
    return container;
  }

  public List<Part> getPart() {
    return part;
  }
}
