package me.sjva.sosoplayer.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.TreeMap;

public class StorageInfo implements Parcelable {
  int storageType;
  String name;
  String path;
  private String port;
  private String id;
  private String passwd;
  private String encoding;
  private boolean activieMode;
  String token;

  private transient ExtraStorageInfo extraStorageInfo;

  public StorageInfo(StorageType storageType) {
    this(storageType, storageType.toString(), "");
  }

  public StorageInfo(StorageType storageType, String name, String path) {
    this(storageType, name, path, null);
  }

  public StorageInfo(StorageType storageType, String name, String path, String token) {
    this(storageType, name, path, null, null, null, token, null, false);
  }

  public StorageInfo(StorageType storageType, String name, String path, String userId, String passwd) {
    this(storageType, name, path, userId, passwd, null, null, null, false);
  }

  public StorageInfo(StorageType storageType, String name, String path, String userId, String passwd, String port, String token, String encoding, boolean isActivieMode) {
    this.storageType = storageType.getInt();
    this.name = name;
    this.path = path;
    this.id = userId;
    this.passwd = passwd;
    this.port = port;
    this.token = token;
    this.encoding = encoding;
    this.activieMode = isActivieMode;
    createExtraStorageInfo(this.storageType);
  }


  private void createExtraStorageInfo(int storageType) {
    if(extraStorageInfo != null)
      return;
    StorageType type = StorageType.fromInt(storageType);
    switch (type) {
      case MediaStore:
        extraStorageInfo = new ExtraMediaStoreStorageInfo();
        break;
      case Plex:
        extraStorageInfo = new ExtraPlexStorageInfo();
        break;
      default:
        extraStorageInfo = new ExtraNormalStorageInfo();
        break;
    }
  }


  public StorageType getStorageType() {
    return StorageType.fromInt(storageType);
  }

  public String getName() {
    return name;
  }

  public String getPath() {
    return path;
  }

  public String getPort() {
    return port;
  }

  public String getId() {
    return id;
  }

  public String getPasswd() {
    return passwd;
  }

  public String getEncoding() {
    return encoding;
  }

  public boolean isActivieMode() {
    return activieMode;
  }

  public String getToken() {
    return token;
  }


  protected StorageInfo(Parcel in) {
    storageType = in.readInt();
    name = in.readString();
    path = in.readString();
    port = in.readString();
    id = in.readString();
    passwd = in.readString();
    encoding = in.readString();
    activieMode = in.readByte() != 0;
    token = in.readString();

    StorageType type = StorageType.fromInt(storageType);
    switch (type) {
      case MediaStore:
        extraStorageInfo = new ExtraMediaStoreStorageInfo();
        break;
      case Plex:
        extraStorageInfo = new ExtraPlexStorageInfo();
        break;
      default:
        extraStorageInfo = new ExtraNormalStorageInfo();
        break;
    }
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(storageType);
    dest.writeString(name);
    dest.writeString(path);
    dest.writeString(port);
    dest.writeString(id);
    dest.writeString(passwd);
    dest.writeString(encoding);
    dest.writeByte((byte) (activieMode ? 1 : 0));
    dest.writeString(token);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<StorageInfo> CREATOR = new Creator<StorageInfo>() {
    @Override
    public StorageInfo createFromParcel(Parcel in) {
      return new StorageInfo(in);
    }

    @Override
    public StorageInfo[] newArray(int size) {
      return new StorageInfo[size];
    }
  };

  public boolean equals(StorageInfo storageInfo) {
    switch (storageInfo.getStorageType()){
      case MediaStore:
        return true;
      case Mount:
        return getName().equals(storageInfo.getName()) && getPath().equals(storageInfo.getPath());
      case Samba:
      case Ftp:
      case WebDav:
        return getName().equals(storageInfo.getName()) && getPath().equals(storageInfo.getPath());
      case Plex:
        return getPath().equals(storageInfo.getPath()) && getToken().equals(storageInfo.getToken());
    }
    return false;
  }

  // Extra
  public void clear() {
    createExtraStorageInfo(storageType);
    if (extraStorageInfo instanceof ExtraMediaStoreStorageInfo){
      ExtraMediaStoreStorageInfo extraInfo = (ExtraMediaStoreStorageInfo)extraStorageInfo;
      extraInfo.clear();
    } else if (extraStorageInfo instanceof ExtraNormalStorageInfo) {
      ExtraNormalStorageInfo extraInfo = (ExtraNormalStorageInfo)extraStorageInfo;
      extraInfo.clear();
    }
  }

  public TreeMap<String, ArrayList<FileInfo>> getBucketMap() {
    createExtraStorageInfo(storageType);
    if (extraStorageInfo instanceof ExtraMediaStoreStorageInfo){
      ExtraMediaStoreStorageInfo extraInfo = (ExtraMediaStoreStorageInfo)extraStorageInfo;
      return extraInfo.getBucketMap();
    }
    return null;
  }

  public String getBucketFilter() {
    createExtraStorageInfo(storageType);
    if (extraStorageInfo instanceof ExtraMediaStoreStorageInfo){
      ExtraMediaStoreStorageInfo extraInfo = (ExtraMediaStoreStorageInfo)extraStorageInfo;
      return extraInfo.getBucketFilter();
    }
    return null;
  }

  public ArrayList<FileInfo> getBucketList() {
    createExtraStorageInfo(storageType);
    if (extraStorageInfo instanceof ExtraMediaStoreStorageInfo){
      ExtraMediaStoreStorageInfo extraInfo = (ExtraMediaStoreStorageInfo)extraStorageInfo;
      return extraInfo.getBucketList();
    }
    return null;
  }
  public void setBucketFilter(String bucketFilter) {
    createExtraStorageInfo(storageType);
    if (extraStorageInfo instanceof ExtraMediaStoreStorageInfo){
      ExtraMediaStoreStorageInfo extraInfo = (ExtraMediaStoreStorageInfo)extraStorageInfo;
      extraInfo.setBucketFilter(bucketFilter);
    }
  }

  public void addFileInfos(FileInfo fileInfo) {
    createExtraStorageInfo(storageType);
    if (extraStorageInfo instanceof ExtraNormalStorageInfo) {
      ExtraNormalStorageInfo extraInfo = (ExtraNormalStorageInfo)extraStorageInfo;
      extraInfo.addFileInfos(fileInfo);
    }
  }

  public ArrayList<FileInfo> getFileInfos() {
    createExtraStorageInfo(storageType);
    if (extraStorageInfo instanceof ExtraNormalStorageInfo) {
      ExtraNormalStorageInfo extraInfo = (ExtraNormalStorageInfo)extraStorageInfo;
      return  extraInfo.getFileInfos();
    }
    return null;
  }


  public void sort() {
    createExtraStorageInfo(storageType);
    if (extraStorageInfo instanceof ExtraNormalStorageInfo) {
      ExtraNormalStorageInfo extraInfo = (ExtraNormalStorageInfo)extraStorageInfo;
      extraInfo.sort();
    }
  }


  public void selectSection(String title, String key) {
    createExtraStorageInfo(storageType);
    if (extraStorageInfo instanceof ExtraPlexStorageInfo) {
      ExtraPlexStorageInfo extraInfo = (ExtraPlexStorageInfo)extraStorageInfo;
      extraInfo.selectSection(title, key);
    }
  }

  public void load(ExtraPlexStorageInfo.OnPlexLoadEventListener onPlexLoadEventListener) {
    createExtraStorageInfo(storageType);
    if (extraStorageInfo instanceof ExtraPlexStorageInfo) {
      ExtraPlexStorageInfo extraInfo = (ExtraPlexStorageInfo)extraStorageInfo;
      extraInfo.load(getPath(), getToken(),onPlexLoadEventListener);
    }
  }

  public void setContent(String title, String key) {
    if (extraStorageInfo instanceof ExtraPlexStorageInfo) {
      ExtraPlexStorageInfo extraInfo = (ExtraPlexStorageInfo)extraStorageInfo;
      extraInfo.setContent(title, key);
    }
  }

  public boolean onBackPressed(ExtraPlexStorageInfo.OnPlexLoadEventListener onPlexLoadEventListener) {
    createExtraStorageInfo(storageType);
    if (extraStorageInfo instanceof ExtraPlexStorageInfo) {
      ExtraPlexStorageInfo extraInfo = (ExtraPlexStorageInfo)extraStorageInfo;
      return extraInfo.onBackPressed(getPath(), getToken(), onPlexLoadEventListener);
    }
    return true;
  }

  public String getTitle() {
    createExtraStorageInfo(storageType);
    if (extraStorageInfo instanceof ExtraPlexStorageInfo) {
      ExtraPlexStorageInfo extraInfo = (ExtraPlexStorageInfo)extraStorageInfo;
      return extraInfo.getTitle(getName());
    }
    return null;
  }

}
