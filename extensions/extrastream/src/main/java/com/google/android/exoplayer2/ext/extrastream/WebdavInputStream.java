package com.google.android.exoplayer2.ext.extrastream;

import com.thegrizzlylabs.sardineandroid.DavResource;
import com.thegrizzlylabs.sardineandroid.Sardine;
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebdavInputStream implements ExtraInputStream {

  private Sardine sardine;
  private InputStream inputStream;
  private long contentLength;
  public WebdavInputStream(String url, String id, String passwd, long offset) throws IOException {
    sardine = new OkHttpSardine();
    sardine.setCredentials(id, passwd);
    List<DavResource> resources = sardine.list(url);
    if (resources.isEmpty())
      throw new IOException();
    contentLength = resources.get(0).getContentLength();
    if (offset > 0) { // SKIP이 너무 걸림...
      Map<String, String> headers = new HashMap<String, String  >();
      headers.put("Range", "bytes=" + offset+ "-");
      inputStream = sardine.get(url, headers);
    } else {
      inputStream = sardine.get(url);
    }
  }


  public long length() {
    return contentLength;
  }

  public void setInterrupt() {

  }

  public void close() throws IOException {
    if (inputStream != null) {
      inputStream.close();
    }
    inputStream = null;
  }

  public int read(byte[] target, int offset, int length) throws IOException {
    int readSize =  inputStream.read(target,offset,length);
    return readSize;
  }
}
