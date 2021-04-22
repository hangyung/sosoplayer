package com.google.android.exoplayer2.ext.extrastream;

import com.google.android.exoplayer2.util.Log;
import com.thegrizzlylabs.sardineandroid.DavPrincipal;
import com.thegrizzlylabs.sardineandroid.DavResource;
import com.thegrizzlylabs.sardineandroid.Sardine;
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WebdavFile {
  private Sardine sardine;

  public WebdavFile(String id, String passwd) {
    sardine = new OkHttpSardine();
    sardine.setCredentials(id, passwd);
  }



  public ArrayList<ExtraStreamFileInfo> list(String root, String url) throws IOException {
    ArrayList<ExtraStreamFileInfo> extraStreamFileInfos = new ArrayList<ExtraStreamFileInfo>();
    url = url.replace("webdav://", "http://");
    List<DavResource> resources = sardine.list(url);
    int count = (resources == null) ? 0 : resources.size();
    for (int i = 0; i < count; i++) {
      if (i == 0){
        continue; // root path
      }
      DavResource davResource = resources.get(i);

      String fullUrl = root + davResource.getPath();
      String name = davResource.getName();

      fullUrl = fullUrl.replace("http://", "webdav://");
  //    fullUrl = fullUrl.replace("https://", "webdav://");
      ExtraStreamFileInfo extraStreamFileInfo = new ExtraStreamFileInfo(fullUrl, name,
          davResource.isDirectory(), davResource.getContentLength());
      extraStreamFileInfos.add(extraStreamFileInfo);
    }
    return extraStreamFileInfos;

  }
}
