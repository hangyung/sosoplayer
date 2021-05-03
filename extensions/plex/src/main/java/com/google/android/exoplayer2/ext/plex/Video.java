package com.google.android.exoplayer2.ext.plex;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.List;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "Video", strict=false)
public class Video implements Parcelable  {
  @Attribute(name = "ratingKey", required=false)
  private String ratingKey;

  @Attribute(name = "key", required=false)
  private String key;

  @Attribute(name = "studio", required=false)
  private String studio;

  @Attribute(name = "type", required=false)
  private String type;

  @Attribute(name = "title", required=false)
  private String title;

  @Attribute(name = "titleSort", required=false)
  private String titleSort;

  @Attribute(name = "contentRating", required=false)
  private String contentRating;

  @Attribute(name = "summary", required=false)
  private String summary;

  @Attribute(name = "rating", required=false)
  private String rating;

  @Attribute(name = "year", required=false)
  private String year;

  @Attribute(name = "tagline", required=false)
  private String tagline;

  @Attribute(name = "thumb", required=false)
  private String thumb;

  @Attribute(name = "art", required=false)
  private String art;

  @Attribute(name = "duration", required=false)
  private String duration;

  @Attribute(name = "originallyAvailableAt", required=false)
  private String originallyAvailableAt;

  @Attribute(name = "addedAt", required=false)
  private String addedAt;

  @Attribute(name = "updatedAt", required=false)
  private String updatedAt;

  @ElementList(name = "Media", required=false, inline = true)
  private List<Media> media;

  public Video() {

  }


  protected Video(Parcel in) {
    ratingKey = in.readString();
    key = in.readString();
    studio = in.readString();
    type = in.readString();
    title = in.readString();
    titleSort = in.readString();
    contentRating = in.readString();
    summary = in.readString();
    rating = in.readString();
    year = in.readString();
    tagline = in.readString();
    thumb = in.readString();
    art = in.readString();
    duration = in.readString();
    originallyAvailableAt = in.readString();
    addedAt = in.readString();
    updatedAt = in.readString();
    media = in.createTypedArrayList(Media.CREATOR);
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(ratingKey);
    dest.writeString(key);
    dest.writeString(studio);
    dest.writeString(type);
    dest.writeString(title);
    dest.writeString(titleSort);
    dest.writeString(contentRating);
    dest.writeString(summary);
    dest.writeString(rating);
    dest.writeString(year);
    dest.writeString(tagline);
    dest.writeString(thumb);
    dest.writeString(art);
    dest.writeString(duration);
    dest.writeString(originallyAvailableAt);
    dest.writeString(addedAt);
    dest.writeString(updatedAt);
    dest.writeTypedList(media);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<Video> CREATOR = new Creator<Video>() {
    @Override
    public Video createFromParcel(Parcel in) {
      return new Video(in);
    }

    @Override
    public Video[] newArray(int size) {
      return new Video[size];
    }
  };

  public String getRatingKey() {
    return ratingKey;
  }

  public String getKey() {
    return key;
  }

  public String getStudio() {
    return studio;
  }

  public String getType() {
    return type;
  }

  public String getTitle() {
    return title;
  }

  public String getTitleSort() {
    return titleSort;
  }

  public String getContentRating() {
    return contentRating;
  }

  public String getSummary() {
    return summary;
  }

  public String getRating() {
    return rating;
  }

  public String getYear() {
    return year;
  }

  public String getTagline() {
    return tagline;
  }

  public String getThumb() {
    return thumb;
  }

  public String getArt() {
    return art;
  }

  public String getDuration() {
    return duration;
  }

  public String getOriginallyAvailableAt() {
    return originallyAvailableAt;
  }

  public String getAddedAt() {
    return addedAt;
  }

  public String getUpdatedAt() {
    return updatedAt;
  }

  public List<Media> getMedia() {
    return media;
  }

  public String getCurrentMediaKey() {
    return String.format("plex://%s", key);
  }
}

