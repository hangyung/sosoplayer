package com.google.android.exoplayer2.ext.extrastream;

public enum IOType {
  Samba(0),
  Ftp(1),
  WebDav(2);

  private final int mData;
  IOType(int data)
  {
    mData = data;
  }

  public int getInt()
  {
    return mData;
  }

  public static IOType fromInt(final int data)
  {
    for (IOType method : IOType.values())
    {
      if (method.mData == data)
      {
        return method;
      }
    }
    return null;
  }
}
