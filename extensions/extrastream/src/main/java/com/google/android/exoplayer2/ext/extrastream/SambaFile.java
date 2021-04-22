package com.google.android.exoplayer2.ext.extrastream;

import java.io.IOException;
import java.util.ArrayList;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;

public class SambaFile {
  private SmbFile sambaFile;
  public SambaFile(String url) throws IOException {
    sambaFile = new SmbFile(url);
  }

  public SambaFile(String url, String userId, String passwd) throws IOException{
    sambaFile = new SmbFile(url, new NtlmPasswordAuthentication(null, userId, passwd));
 //   sambaFile.listFiles()
  }

  public ArrayList<ExtraStreamFileInfo> list() throws IOException {
    ArrayList<ExtraStreamFileInfo> extraStreamFileInfos = new ArrayList<ExtraStreamFileInfo>();
    SmbFile[] sambaFiles = sambaFile.listFiles();
    int count = (sambaFiles == null) ? 0 : sambaFiles.length;
    for (SmbFile smbFile : sambaFiles) {
      if (smbFile.isHidden())
        continue;
      String path = smbFile.getPath();
      String name = smbFile.getName();
      boolean isDirectory = smbFile.isDirectory();
      if (isDirectory) {
        int pos = name.lastIndexOf("/");
        name = name.substring(0, pos);
      }

      ExtraStreamFileInfo extraStreamFileInfo = new ExtraStreamFileInfo(path, name,
          isDirectory, smbFile.length());
      extraStreamFileInfos.add(extraStreamFileInfo);
    }
    return extraStreamFileInfos;
  }


}
