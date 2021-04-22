package com.google.android.exoplayer2.ext.extrastream;

import android.net.Uri;
import android.text.TextUtils;
import java.util.Set;

public class ExtraStreamUriParser {
  private Uri uri;
  public ExtraStreamUriParser(Uri uri) {
    this.uri = uri;
  }
  public String getId() {
    return  uri.getQueryParameter(ExtraStreamUri.EXTRA_STREAM_USERID);
  }

  public String getPasswd() {
    return  uri.getQueryParameter(ExtraStreamUri.EXTRA_STREAM_PASSWD);
  }

  public String getEncoding() {
    return  uri.getQueryParameter(ExtraStreamUri.EXTRA_STREAM_ENCODING);
  }

  public String getHost() {
    return  uri.getQueryParameter(ExtraStreamUri.EXTRA_STREAM_HOST);
  }

  public String getPort(String defaultVal) {
    String portString = uri.getQueryParameter(ExtraStreamUri.EXTRA_STREAM_PORT);
    if (TextUtils.isEmpty(portString)) {
      return defaultVal;
    }
    return  portString;
  }

  public boolean isActiveMode() {
    boolean isActiveMode = false;
    try {
      String isActiveModeString = uri.getQueryParameter(ExtraStreamUri.EXTRA_STREAM_ACTIVEMODE);
      isActiveMode = Boolean.parseBoolean(isActiveModeString);
    }catch (Exception e) {

    }
    return isActiveMode;
  }

  public Uri getOrgUri() {
    final Set<String> params = uri.getQueryParameterNames();
    final Uri.Builder newUri = uri.buildUpon().clearQuery();
    newUri.scheme("http");
    for (String param : params) {
      if (!param.equals(ExtraStreamUri.EXTRA_STREAM_NAME)
       && !param.equals(ExtraStreamUri.EXTRA_STREAM_USERID)
       && !param.equals(ExtraStreamUri.EXTRA_STREAM_PASSWD)
       && !param.equals(ExtraStreamUri.EXTRA_STREAM_HOST)
       && !param.equals(ExtraStreamUri.EXTRA_STREAM_ENCODING)
       && !param.equals(ExtraStreamUri.EXTRA_STREAM_PORT)
       && !param.equals(ExtraStreamUri.EXTRA_STREAM_ACTIVEMODE)) {
        newUri.appendQueryParameter(param, uri.getQueryParameter(param));
      }
    }
    return newUri.build();
  }



}
