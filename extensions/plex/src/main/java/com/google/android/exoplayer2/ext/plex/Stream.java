package com.google.android.exoplayer2.ext.plex;

import android.os.Parcel;
import android.os.Parcelable;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "Stream", strict=false)
public class Stream implements Parcelable {
  @Attribute(name = "id", required=false)
  private String id;

  @Attribute(name = "streamType", required=false)
  private String streamType;

  @Attribute(name = "codec", required=false)
  private String codec;

  @Attribute(name = "index", required=false)
  private String index;

  @Attribute(name = "bitrate", required=false)
  private String bitrate;

  @Attribute(name = "language", required=false)
  private String language;

  @Attribute(name = "languageCode", required=false)
  private String languageCode;

  @Attribute(name = "anamorphic", required=false)
  private String anamorphic;

  @Attribute(name = "bitDepth", required=false)
  private String bitDepth;

  @Attribute(name = "cabac", required=false)
  private String cabac;

  @Attribute(name = "chromaSubsampling", required=false)
  private String chromaSubsampling;

  @Attribute(name = "codecID", required=false)
  private String codecID;

  @Attribute(name = "colorSpace", required=false)
  private String colorSpace;

  @Attribute(name = "pixelAspectRatio", required=false)
  private String pixelAspectRatio;

  @Attribute(name = "profile", required=false)
  private String profile;

  @Attribute(name = "refFrames", required=false)
  private String refFrames;

  @Attribute(name = "scanType", required=false)
  private String scanType;

  @Attribute(name = "title", required=false)
  private String title;

  @Attribute(name = "width", required=false)
  private String width;

  @Attribute(name = "selected", required=false)
  private String selected;

  @Attribute(name = "channels", required=false)
  private String channels;

  @Attribute(name = "samplingRate", required=false)
  private String samplingRate;

  @Attribute(name = "bitrateMode", required=false)
  private String bitrateMode;

  @Attribute(name = "key", required=false)
  private String key;

  @Attribute(name = "format", required=false)
  private String format;

  public Stream() {

  }


  protected Stream(Parcel in) {
    id = in.readString();
    streamType = in.readString();
    codec = in.readString();
    index = in.readString();
    bitrate = in.readString();
    language = in.readString();
    languageCode = in.readString();
    anamorphic = in.readString();
    bitDepth = in.readString();
    cabac = in.readString();
    chromaSubsampling = in.readString();
    codecID = in.readString();
    colorSpace = in.readString();
    pixelAspectRatio = in.readString();
    profile = in.readString();
    refFrames = in.readString();
    scanType = in.readString();
    title = in.readString();
    width = in.readString();
    selected = in.readString();
    channels = in.readString();
    samplingRate = in.readString();
    bitrateMode = in.readString();
    key = in.readString();
    format = in.readString();
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(id);
    dest.writeString(streamType);
    dest.writeString(codec);
    dest.writeString(index);
    dest.writeString(bitrate);
    dest.writeString(language);
    dest.writeString(languageCode);
    dest.writeString(anamorphic);
    dest.writeString(bitDepth);
    dest.writeString(cabac);
    dest.writeString(chromaSubsampling);
    dest.writeString(codecID);
    dest.writeString(colorSpace);
    dest.writeString(pixelAspectRatio);
    dest.writeString(profile);
    dest.writeString(refFrames);
    dest.writeString(scanType);
    dest.writeString(title);
    dest.writeString(width);
    dest.writeString(selected);
    dest.writeString(channels);
    dest.writeString(samplingRate);
    dest.writeString(bitrateMode);
    dest.writeString(key);
    dest.writeString(format);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<Stream> CREATOR = new Creator<Stream>() {
    @Override
    public Stream createFromParcel(Parcel in) {
      return new Stream(in);
    }

    @Override
    public Stream[] newArray(int size) {
      return new Stream[size];
    }
  };

  public String getId() {
    return id;
  }

  public String getStreamType() {
    return streamType;
  }

  public String getCodec() {
    return codec;
  }

  public String getIndex() {
    return index;
  }

  public String getBitrate() {
    return bitrate;
  }

  public String getLanguage() {
    return language;
  }

  public String getLanguageCode() {
    return languageCode;
  }

  public String getAnamorphic() {
    return anamorphic;
  }

  public String getBitDepth() {
    return bitDepth;
  }

  public String getCabac() {
    return cabac;
  }

  public String getChromaSubsampling() {
    return chromaSubsampling;
  }

  public String getCodecID() {
    return codecID;
  }

  public String getColorSpace() {
    return colorSpace;
  }

  public String getPixelAspectRatio() {
    return pixelAspectRatio;
  }

  public String getProfile() {
    return profile;
  }

  public String getRefFrames() {
    return refFrames;
  }

  public String getScanType() {
    return scanType;
  }

  public String getTitle() {
    return title;
  }

  public String getWidth() {
    return width;
  }

  public String getSelected() {
    return selected;
  }

  public String getChannels() {
    return channels;
  }

  public String getSamplingRate() {
    return samplingRate;
  }

  public String getBitrateMode() {
    return bitrateMode;
  }

  public String getKey() {
    return key;
  }

  public String getFormat() {
    return format;
  }
}

