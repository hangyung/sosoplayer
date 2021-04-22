/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.exoplayer2.ext.extrastream;


import android.net.Uri;
import android.text.TextUtils;
import androidx.annotation.Nullable;
import com.google.android.exoplayer2.upstream.BaseDataSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.TransferListener;
import java.io.File;
import java.io.IOException;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbRandomAccessFile;

public class ExtraStreamDataSource extends BaseDataSource {
  private final IOType ioType;


  private Uri uri;

  ExtraInputStream extraInputStream;

  private FtpFile ftpFile;
  private TransferListener transferListener;


  public static final class Factory implements DataSource.Factory {
    private IOType ioType;
    private TransferListener transferListener;
    public Factory setIOType(@Nullable IOType ioType) {
      this.ioType = ioType;
      return this;
    }
    public Factory setTransferListener(@Nullable TransferListener transferListener) {
      this.transferListener = transferListener;
      return this;
    }
    @Override
    public DataSource createDataSource() {
      ExtraStreamDataSource extraStreamDataSource = new ExtraStreamDataSource(ioType);
      if (transferListener != null) {
        extraStreamDataSource.addTransferListener(transferListener);
      }
      return extraStreamDataSource;
    }
  }

  /**
   * Creates base data source.

   */
  protected ExtraStreamDataSource(IOType ioType) {
    super(true);
    this.ioType = ioType;
  }

  @Override
  public long open(DataSpec dataSpec) throws IOException {
    long offset = dataSpec.position;

    ExtraStreamUriParser parser = new ExtraStreamUriParser(dataSpec.uri);
    transferInitializing(dataSpec);
    String id = parser.getId();
    String passwd = parser.getPasswd();
    uri = parser.getOrgUri();
    String path = uri.toString();
    switch (ioType){
      case Samba: {
          if (!TextUtils.isEmpty(parser.getPasswd())) {
            SmbFile sambaFile = new SmbFile(path, new NtlmPasswordAuthentication(null, id, passwd));
            extraInputStream = new SambaInputStream(new SmbRandomAccessFile(sambaFile, "r"), offset);
          } else {
            SmbFile sambaFile = new SmbFile(path);
            extraInputStream = new SambaInputStream(new SmbRandomAccessFile(sambaFile, "r"), offset);
          }
        }
        break;
      case Ftp:
        ftpFile = new FtpFile(parser.getId(), parser.getPasswd(), parser.getEncoding(), parser.getHost(), parser.getPort("21"), parser.isActiveMode(), null);
        try {
          File file = new File(path);
          String name = file.getName();
          path = path.replace("http://", "");
          path = path.replace("http:/", "");
          path = path.replace(name, "");
          ftpFile.connect();
          ftpFile.setWorkingDirectory(parser.getHost(), path);

          extraInputStream = ftpFile.newFtpRandomAccessFile(name, offset);
        } catch (Exception e) {
          extraInputStream = null;
        }
        break;
      case WebDav: {
          extraInputStream = new WebdavInputStream(path, id, passwd,  offset);
        }
        break;
    }

    long length = extraInputStream.length();


    transferStarted(dataSpec);
    return length - offset;
  }

  @Nullable
  @Override
  public Uri getUri() {
    return uri;
  }

  @Override
  public void close() throws IOException {
    if(extraInputStream != null)
    {
      extraInputStream.setInterrupt();

      try {
        extraInputStream.close();
      } catch (IOException e) {}
      extraInputStream = null;

      transferEnded();
    }
    if(ftpFile != null)
    {
      ftpFile.disconnect();
      ftpFile = null;
    }

//    new Thread(new Runnable(){
//      @Override
//      public void run()
//      {
//      }}).start();
  }

  @Override
  public int read(byte[] target, int offset, int length) throws IOException {
    int bytesRead = extraInputStream.read(target, offset, length);
    bytesTransferred(bytesRead);
    return bytesRead;
  }
}
