package com.google.android.exoplayer2.ext.extrastream;

import java.io.IOException;

public interface ExtraInputStream {

  long length();

  void setInterrupt();

  void close() throws IOException;

  int read(byte[] target, int offset, int length) throws IOException;
}
