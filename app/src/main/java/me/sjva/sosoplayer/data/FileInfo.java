package me.sjva.sosoplayer.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Size;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ext.extrastream.ExtraStreamUri;
import com.google.android.exoplayer2.ext.extrastream.IOType;
import java.io.IOException;
import java.util.ArrayList;
import me.sjva.sosoplayer.util.Util;


public class FileInfo implements Parcelable {
  final String id;
  final int storageType;

  final String name;
  final String bucket;
  final boolean directory;
  final String path;
  final long duration;
  final long size;

  final String thumbnails;
  final ArrayList<String> subtitles;
  final String currentMediaKey;

  public static final class Builder {
    final  StorageType storageType;
    String id;
    String name;
    String bucket;
    boolean directory;
    String path;
    long duration;
    long size;

    String thumbnails;
    String currentMediaKey;
    ArrayList<String> subtitles;

    public Builder(StorageType storageType) {
      this.storageType = storageType;
      subtitles = new ArrayList<String>();
    }

    public void setId(String id) {
      this.id = id;
    }

    public void setName(String name) {
      this.name = name;
    }

    public void setBucket(String bucket) {
      this.bucket = bucket;
    }

    public void setDirectory(boolean directory) {
      this.directory = directory;
    }

    public void setPath(String path) {
      this.path = path;
    }

    public void setDuration(long duration) {
      this.duration = duration;
    }

    public void setSize(long size) {
      this.size = size;
    }

    public void setThumbnails(String thumbnails) {
      this.thumbnails = thumbnails;
    }

    public void setCurrentMediaKey(String currentMediaKey) {
      this.currentMediaKey = currentMediaKey;
    }

    public void addSubtitle(String subtitle) {
      this.subtitles.add(subtitle) ;
    }


    public FileInfo build() {
      return new FileInfo(id, storageType, name, bucket, directory, path,
          duration, size, thumbnails, currentMediaKey,  subtitles);
    }
  }
  public FileInfo(String id, StorageType storageType, String name, String bucket,
      boolean directory, String path, long duration, long size, String thumbnails, String currentMediaKey,
      ArrayList<String> subtitles) {
    this.id = id;
    this.storageType = storageType.getInt();
    this.name = name;
    this.bucket = bucket;
    this.directory = directory;
    this.path = path;
    this.duration = duration;
    this.size = size;
    this.thumbnails = thumbnails;
    this.subtitles = subtitles;
    this.currentMediaKey = currentMediaKey;
  }


  protected FileInfo(Parcel in) {
    id = in.readString();
    storageType = in.readInt();
    name = in.readString();
    bucket = in.readString();
    directory = in.readByte() != 0;
    path = in.readString();
    duration = in.readLong();
    size = in.readLong();
    thumbnails = in.readString();
    currentMediaKey = in.readString();
    subtitles = in.createStringArrayList();
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(id);
    dest.writeInt(storageType);
    dest.writeString(name);
    dest.writeString(bucket);
    dest.writeByte((byte) (directory ? 1 : 0));
    dest.writeString(path);
    dest.writeLong(duration);
    dest.writeLong(size);
    dest.writeString(thumbnails);
    dest.writeString(currentMediaKey);
    dest.writeStringList(subtitles);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<FileInfo> CREATOR = new Creator<FileInfo>() {
    @Override
    public FileInfo createFromParcel(Parcel in) {
      return new FileInfo(in);
    }

    @Override
    public FileInfo[] newArray(int size) {
      return new FileInfo[size];
    }
  };

  public void loadImage(Context context, ImageView imageView) throws IOException {
    switch (StorageType.fromInt(storageType)){
      case MediaStore: {
        Bitmap thumbnailBitmap = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            thumbnailBitmap =
                context.getContentResolver().loadThumbnail(
                    Uri.parse(thumbnails), new Size(320, 180), null);
            Glide.with(context)
                .load(thumbnailBitmap)
                .into(imageView);
          }else {

            thumbnailBitmap = MediaStore.Video.Thumbnails.getThumbnail( context.getContentResolver(), Long.parseLong(id), MediaStore.Video.Thumbnails.MINI_KIND, null );
          }
          if (thumbnailBitmap != null) {
            Glide.with(context)
                .load(thumbnailBitmap)
                .into(imageView);
          }
        }
        break;
      default:
        {
          Glide.with(context)
              .load(thumbnails)
              .into(imageView);

        }
        break;
    }
  }





  public String getId() {
    return id;
  }

  public StorageType getStorageType() {
    return StorageType.fromInt(storageType);
  }

  public String getName() {
    return name;
  }

  public String getBucket() {
    return bucket;
  }

  public boolean isDirectory() {
    return directory;
  }

  public String getPath() {
    return path;
  }

  public long getDuration() {
    return duration;
  }

  public long getSize() {
    return size;
  }

  public String getThumbnails() {
    return thumbnails;
  }

  public ArrayList<String> getSubtitles() {
    return subtitles;
  }

  public String getInfo() {
    return getInfo(C.TIME_UNSET);
  }

  public String getCurrentMediaKey() {
    return currentMediaKey;
  }

  public String getInfo(long currentPostion) {
    if (directory)
      return "";

    String info = getSizeString();
//    if (currentPostion != C.TIME_UNSET) {
//      info += ", <font color='#00FF00'>";
//      info += Util.makeDurationMessage(currentPostion);
//      info += "</font>";
//    }

    if (subtitles.size() > 0) {
      info += ", ";
      info += "<font color='#FF0000'>SUB</font>";
    }

    return info;
  }

  private String getSizeString() {
    String sizeString = null;

    long len = this.size;
    if (len > 0 && len < 1024 * 1024) {
      len = len / 1024;
      sizeString = len + "KB";
    } else if (len <= 0) {
      return "";
    } else {
      len = len / 1024 / 1024;
      sizeString = len + "MB";
    }

    return sizeString;
  }

  public static  Uri getExtraUri(StorageInfo storageInfo, String curPath) {
    switch (storageInfo.getStorageType()){
      case Samba: {
          Uri curUri = new ExtraStreamUri(IOType.Samba, curPath)
              .setName(storageInfo.getName())
              .setUserId(storageInfo.getId())
              .setPasswd(storageInfo.getPasswd()).build();
          return curUri;
        }
      case Ftp: {
          Uri curUri = new ExtraStreamUri(IOType.Ftp, curPath)
              .setName(storageInfo.getName())
              .setUserId(storageInfo.getId())
              .setPasswd(storageInfo.getPasswd())
              .setHost(storageInfo.getPath())
              .setEncoding(storageInfo.getEncoding())
              .setPort(storageInfo.getPort())
              .setActiveMode(storageInfo.isActivieMode()).build();
          return curUri;
        }
      case WebDav: {
          Uri curUri = new ExtraStreamUri(IOType.WebDav, curPath)
              .setName(storageInfo.getName())
              .setUserId(storageInfo.getId())
              .setPasswd(storageInfo.getPasswd()).build();
          return curUri;
        }

      default:
          return Uri.parse(curPath);
    }
  }

//  public Uri getExtraUri(StorageInfo storageInfo) {
//    return getExtraUri(storageInfo, path);
//  }

  public  ArrayList<MediaItem> getMediaItems(StorageInfo storageInfo) {
    ArrayList<MediaItem> mediaItems = new ArrayList<MediaItem>();
    MediaItem.Builder builder = new MediaItem.Builder();


    Uri curUri = getExtraUri(storageInfo, path);
    builder.setUri(curUri);
    ArrayList<MediaItem.Subtitle> subtitleList = new ArrayList<MediaItem.Subtitle>();
    for(String subtitle : subtitles) {
      String lowFileName = subtitle.toLowerCase();
      int pos = lowFileName.lastIndexOf( "." );
      if (pos > 0) {
        String ext = lowFileName.substring(pos + 1);
        String mimeType =  Util.getTextMimeType(ext);
        subtitleList.add(new MediaItem.Subtitle(getExtraUri(storageInfo, subtitle), mimeType, "Unknown"));
      }
    }
    if (subtitleList.size() > 0) {
      builder.setSubtitles(subtitleList);
    }

    mediaItems.add(builder.build());

    return mediaItems;
  }
}
