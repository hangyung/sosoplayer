package com.google.android.exoplayer2.ext.extrastream;

import java.io.IOException;
import jcifs.smb.SmbException;
import jcifs.smb.SmbRandomAccessFile;

public class SambaInputStream implements ExtraInputStream {
  private SmbRandomAccessFile file;
  public SambaInputStream(SmbRandomAccessFile file, long offset) throws SmbException {
    this.file = file;
    if (offset > 0) {
      file.seek(offset);
    }
  }

  @Override
  public long length(){
    try {
      return file.length();
    } catch (SmbException e) {
      e.printStackTrace();
    }
    return -1;
  }


  @Override
  public void setInterrupt() {

  }

  @Override
  public void close() throws IOException {
    if (file != null) {
      file.close();
    }
    file = null;
  }

  @Override
  public int read(byte[] target, int offset, int length) throws IOException {
    return file.read(target,offset,length);
  }
}
