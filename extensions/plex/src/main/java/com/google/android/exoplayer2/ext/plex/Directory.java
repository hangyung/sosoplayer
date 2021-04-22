package com.google.android.exoplayer2.ext.plex;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.List;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "Directory", strict=false)
public class Directory implements Parcelable {
  @Attribute(name = "allowSync", required=false)
  private String allowSync;

  @Attribute(name = "art", required=false)
  private String art;

  @Attribute(name = "composite", required=false)
  private String composite;

  @Attribute(name = "filters", required=false)
  private String filters;

  @Attribute(name = "refreshing", required=false)
  private String refreshing;

  @Attribute(name = "thumb", required=false)
  private String thumb;

  @Attribute(name = "key", required=false)
  private String key;

  @Attribute(name = "type", required=false)
  private String type;

  @Attribute(name = "title", required=false)
  private String title;

  @Attribute(name = "agent", required=false)
  private String agent;

  @Attribute(name = "scanner", required=false)
  private String scanner;

  @Attribute(name = "language", required=false)
  private String language;

  @Attribute(name = "uuid", required=false)
  private String uuid;

  @Attribute(name = "updatedAt", required=false)
  private String updatedAt;

  @Attribute(name = "createdAt", required=false)
  private String createdAt;

  @Attribute(name = "scannedAt", required=false)
  private String scannedAt;

  @Attribute(name = "content", required=false)
  private String content;

  @Attribute(name = "directory", required=false)
  private String directory;

  @Attribute(name = "contentChangedAt", required=false)
  private String contentChangedAt;

  @Attribute(name = "hidden", required=false)
  private String hidden;

  @Attribute(name = "ratingKey", required=false)
  private String ratingKey;

  @Attribute(name = "guid", required=false)
  private String guid;

  @Attribute(name = "studio", required=false)
  private String studio;

  @Attribute(name = "titleSort", required=false)
  private String titleSort;

  @Attribute(name = "summary", required=false)
  private String summary;

  @Attribute(name = "index", required=false)
  private String index;

  @Attribute(name = "year", required=false)
  private String year;

  @Attribute(name = "banner", required=false)
  private String banner;

  @Attribute(name = "duration", required=false)
  private String duration;

  @Attribute(name = "originallyAvailableAt", required=false)
  private String originallyAvailableAt;

  @Attribute(name = "leafCount", required=false)
  private String leafCount;

  @Attribute(name = "viewedLeafCount", required=false)
  private String viewedLeafCount;

  @Attribute(name = "childCount", required=false)
  private String childCount;

  @Attribute(name = "addedAt", required=false)
  private String addedAt;

  @ElementList(name = "Genre", required=false, inline=true)
  List<Genre> genres;

  @ElementList(name = "Location", required=false, inline=true)
  List<Location> location;

  public Directory() {

  }

  protected Directory(Parcel in) {
    allowSync = in.readString();
    art = in.readString();
    composite = in.readString();
    filters = in.readString();
    refreshing = in.readString();
    thumb = in.readString();
    key = in.readString();
    type = in.readString();
    title = in.readString();
    agent = in.readString();
    scanner = in.readString();
    language = in.readString();
    uuid = in.readString();
    updatedAt = in.readString();
    createdAt = in.readString();
    scannedAt = in.readString();
    content = in.readString();
    directory = in.readString();
    contentChangedAt = in.readString();
    hidden = in.readString();
    ratingKey = in.readString();
    guid = in.readString();
    studio = in.readString();
    titleSort = in.readString();
    summary = in.readString();
    index = in.readString();
    year = in.readString();
    banner = in.readString();
    duration = in.readString();
    originallyAvailableAt = in.readString();
    leafCount = in.readString();
    viewedLeafCount = in.readString();
    childCount = in.readString();
    addedAt = in.readString();
    genres = in.createTypedArrayList(Genre.CREATOR);
    location = in.createTypedArrayList(Location.CREATOR);
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(allowSync);
    dest.writeString(art);
    dest.writeString(composite);
    dest.writeString(filters);
    dest.writeString(refreshing);
    dest.writeString(thumb);
    dest.writeString(key);
    dest.writeString(type);
    dest.writeString(title);
    dest.writeString(agent);
    dest.writeString(scanner);
    dest.writeString(language);
    dest.writeString(uuid);
    dest.writeString(updatedAt);
    dest.writeString(createdAt);
    dest.writeString(scannedAt);
    dest.writeString(content);
    dest.writeString(directory);
    dest.writeString(contentChangedAt);
    dest.writeString(hidden);
    dest.writeString(ratingKey);
    dest.writeString(guid);
    dest.writeString(studio);
    dest.writeString(titleSort);
    dest.writeString(summary);
    dest.writeString(index);
    dest.writeString(year);
    dest.writeString(banner);
    dest.writeString(duration);
    dest.writeString(originallyAvailableAt);
    dest.writeString(leafCount);
    dest.writeString(viewedLeafCount);
    dest.writeString(childCount);
    dest.writeString(addedAt);
    dest.writeTypedList(genres);
    dest.writeTypedList(location);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<Directory> CREATOR = new Creator<Directory>() {
    @Override
    public Directory createFromParcel(Parcel in) {
      return new Directory(in);
    }

    @Override
    public Directory[] newArray(int size) {
      return new Directory[size];
    }
  };

  public String getAllowSync() {
    return allowSync;
  }

  public String getArt() {
    return art;
  }

  public String getComposite() {
    return composite;
  }

  public String getFilters() {
    return filters;
  }

  public String getRefreshing() {
    return refreshing;
  }

  public String getThumb() {
    return thumb;
  }

  public String getKey() {
    return key;
  }

  public String getType() {
    return type;
  }

  public String getTitle() {
    return title;
  }

  public String getAgent() {
    return agent;
  }

  public String getScanner() {
    return scanner;
  }

  public String getLanguage() {
    return language;
  }

  public String getUuid() {
    return uuid;
  }

  public String getUpdatedAt() {
    return updatedAt;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public String getScannedAt() {
    return scannedAt;
  }

  public String getContent() {
    return content;
  }

  public String getDirectory() {
    return directory;
  }

  public String getContentChangedAt() {
    return contentChangedAt;
  }

  public String getHidden() {
    return hidden;
  }

  public String getRatingKey() {
    return ratingKey;
  }

  public String getGuid() {
    return guid;
  }

  public String getStudio() {
    return studio;
  }

  public String getTitleSort() {
    return titleSort;
  }

  public String getSummary() {
    return summary;
  }

  public String getIndex() {
    return index;
  }

  public String getYear() {
    return year;
  }

  public String getBanner() {
    return banner;
  }

  public String getDuration() {
    return duration;
  }

  public String getOriginallyAvailableAt() {
    return originallyAvailableAt;
  }

  public String getLeafCount() {
    return leafCount;
  }

  public String getViewedLeafCount() {
    return viewedLeafCount;
  }

  public String getChildCount() {
    return childCount;
  }

  public String getAddedAt() {
    return addedAt;
  }

  public List<Genre> getGenres() {
    return genres;
  }

  public List<Location> getLocation() {
    return location;
  }


}
