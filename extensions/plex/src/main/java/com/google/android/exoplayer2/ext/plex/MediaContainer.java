package com.google.android.exoplayer2.ext.plex;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "MediaContainer", strict=false)
public class MediaContainer implements Parcelable {
  @Attribute(name = "size", required=false)
  private String size;

  @Attribute(name = "allowSync", required=false)
  private String allowSync;

  @Attribute(name = "identifier", required=false)
  private String identifier;

  @Attribute(name = "mediaTagPrefix", required=false)
  private String mediaTagPrefix;

  @Attribute(name = "mediaTagVersion", required=false)
  private String mediaTagVersion;

  @Attribute(name = "title1", required=false)
  private String title1;

  @Attribute(name = "art", required=false)
  private String art;

  @Attribute(name = "librarySectionID", required=false)
  private String librarySectionID;

  @Attribute(name = "librarySectionTitle", required=false)
  private String librarySectionTitle;

  @Attribute(name = "librarySectionUUID", required=false)
  private String librarySectionUUID;

  @Attribute(name = "nocache", required=false)
  private String nocache;

  @Attribute(name = "thumb", required=false)
  private String thumb;

  @Attribute(name = "title2", required=false)
  private String title2;

  @Attribute(name = "viewGroup", required=false)
  private String viewGroup;

  @Attribute(name = "viewMode", required=false)
  private String viewMode;

  @Attribute(name = "parentTitle", required=false)
  private String parentTitle;

  @Attribute(name = "parentYear", required=false)
  private String parentYear;

  @Attribute(name = "summary", required=false)
  private String summary;

  @ElementList(name = "Directory", required=false, inline=true)
  List<Directory> directory;

  @ElementList(name = "Video", required=false, inline=true)
  List<Video> videos;

  public MediaContainer() {

  }


  protected MediaContainer(Parcel in) {
    size = in.readString();
    allowSync = in.readString();
    identifier = in.readString();
    mediaTagPrefix = in.readString();
    mediaTagVersion = in.readString();
    title1 = in.readString();
    art = in.readString();
    librarySectionID = in.readString();
    librarySectionTitle = in.readString();
    librarySectionUUID = in.readString();
    nocache = in.readString();
    thumb = in.readString();
    title2 = in.readString();
    viewGroup = in.readString();
    viewMode = in.readString();
    parentTitle = in.readString();
    parentYear = in.readString();
    summary = in.readString();
    directory = in.createTypedArrayList(Directory.CREATOR);
    videos = in.createTypedArrayList(Video.CREATOR);
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(size);
    dest.writeString(allowSync);
    dest.writeString(identifier);
    dest.writeString(mediaTagPrefix);
    dest.writeString(mediaTagVersion);
    dest.writeString(title1);
    dest.writeString(art);
    dest.writeString(librarySectionID);
    dest.writeString(librarySectionTitle);
    dest.writeString(librarySectionUUID);
    dest.writeString(nocache);
    dest.writeString(thumb);
    dest.writeString(title2);
    dest.writeString(viewGroup);
    dest.writeString(viewMode);
    dest.writeString(parentTitle);
    dest.writeString(parentYear);
    dest.writeString(summary);
    dest.writeTypedList(directory);
    dest.writeTypedList(videos);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<MediaContainer> CREATOR = new Creator<MediaContainer>() {
    @Override
    public MediaContainer createFromParcel(Parcel in) {
      return new MediaContainer(in);
    }

    @Override
    public MediaContainer[] newArray(int size) {
      return new MediaContainer[size];
    }
  };

  public String getAllowSync() {
    return allowSync;
  }

  public String getIdentifier() {
    return identifier;
  }

  public String getMediaTagPrefix() {
    return mediaTagPrefix;
  }

  public String getMediaTagVersion() {
    return mediaTagVersion;
  }

  public String getTitle1() {
    return title1;
  }

  public String getArt() {
    return art;
  }

  public String getLibrarySectionID() {
    return librarySectionID;
  }

  public String getLibrarySectionTitle() {
    return librarySectionTitle;
  }

  public String getLibrarySectionUUID() {
    return librarySectionUUID;
  }

  public String getNocache() {
    return nocache;
  }

  public String getThumb() {
    return thumb;
  }

  public String getTitle2() {
    return title2;
  }

  public String getViewGroup() {
    return viewGroup;
  }

  public String getViewMode() {
    return viewMode;
  }

  public String getSize() {
    return size;
  }

  public String getParentTitle() {
    return parentTitle;
  }

  public String getParentYear() {
    return parentYear;
  }

  public String getSummary() {
    return summary;
  }

  public ArrayList<Directory> getDirectory() {
    ArrayList<Directory> arrayList = new ArrayList<Directory>();
    arrayList.addAll(directory);
    return arrayList;
  }
  public ArrayList<Video> getVideos() {
    ArrayList<Video> arrayList = new ArrayList<Video>();
    arrayList.addAll(videos);
    return arrayList;
  }
}
