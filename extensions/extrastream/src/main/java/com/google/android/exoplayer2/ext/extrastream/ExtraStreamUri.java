package com.google.android.exoplayer2.ext.extrastream;

import android.net.Uri;
import android.text.TextUtils;

public class ExtraStreamUri {
  public static final String EXTRA_STREAM_NAME = "extra_name";
  public static final String EXTRA_STREAM_USERID= "extra_userid";
  public static final String EXTRA_STREAM_PASSWD= "extra_passwd";
  public static final String EXTRA_STREAM_ENCODING= "extra_encoding";
  public static final String EXTRA_STREAM_HOST= "extra_host";
  public static final String EXTRA_STREAM_PORT= "extra_port";
  public static final String EXTRA_STREAM_ACTIVEMODE= "extra_activiemode";
  public Uri.Builder builder;

  public ExtraStreamUri(IOType ioType, String path){
//    path = path.replace("samba://","");
//    path = path.replace("ftp://","");
//    path = path.replace("webdav://","");
    Uri orgUri = Uri.parse(path);

    builder =orgUri.buildUpon();

    switch (ioType){
      case Samba:
        builder.scheme("samba");
        break;
      case Ftp:
        builder.scheme("ftp");
        break;
      case WebDav:
        builder.scheme("webdav");
    }
  }


  public ExtraStreamUri setName(String name) {
    if (!TextUtils.isEmpty(name)) {
      builder.appendQueryParameter(EXTRA_STREAM_NAME, name);
    }
    return this;
  }

  public ExtraStreamUri setUserId(String userId) {
    if (!TextUtils.isEmpty(userId)) {
      builder.appendQueryParameter(EXTRA_STREAM_USERID, userId);
    }
    return this;
  }
  public ExtraStreamUri setPasswd(String passwd) {
    if (!TextUtils.isEmpty(passwd)) {
      builder.appendQueryParameter(EXTRA_STREAM_PASSWD, passwd);
    }
    return this;
  }

  public ExtraStreamUri setHost(String host) {
    if (!TextUtils.isEmpty(host)) {
      builder.appendQueryParameter(EXTRA_STREAM_HOST, host);
    }
    return this;
  }
  public ExtraStreamUri setEncoding(String encoding) {
    if (!TextUtils.isEmpty(encoding)) {
      builder.appendQueryParameter(EXTRA_STREAM_ENCODING, encoding);
    }
    return this;
  }

  public ExtraStreamUri setPort(String port) {
    if (!TextUtils.isEmpty(port)) {
      builder.appendQueryParameter(EXTRA_STREAM_PORT, port);
    }
    return this;
  }

  public ExtraStreamUri setActiveMode(boolean activeMode) {
    builder.appendQueryParameter(EXTRA_STREAM_ACTIVEMODE, activeMode ? "true" : " false");
    return this;
  }

  public Uri build() {
    return builder.build();
  }

}
