package me.sjva.sosoplayer.data;

public enum StorageType {

  MediaStore(0), Mount(1),  Samba(2), Ftp(3), WebDav(4), Plex(5);
  private final int mData;
  StorageType(int data)
  {
    mData = data;
  }

  public int getInt()
  {
    return mData;
  }

  public static StorageType fromInt(final int data)
  {
    for (StorageType method : StorageType.values())
    {
      if (method.mData == data)
      {
        return method;
      }
    }

    return null;
  }
}
