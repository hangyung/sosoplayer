package com.google.android.exoplayer2.ext.ffmpeg2;

import android.net.Uri;
import androidx.annotation.Nullable;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.Util;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class FfmpegCustomIo implements DataSource{
  private static final String TAG = "FfmpegCustomIo";
  private static final int MAX_SIZE  = 1048576; // 1M
  private static final int AVSEEK_SIZE  = 0x10000;
  private long postion;
  private long contentLength;
  private Uri uri;
  private DataSource dataSource;
  private byte[] tempBuffer;
  private ByteBuffer byteBuffer;

  public FfmpegCustomIo(Uri uri, DataSource dataSource)  {
    contentLength = C.LENGTH_UNSET;
    this.postion = 0;
    this.uri = uri;
    this.dataSource = dataSource;
    this.tempBuffer = new byte[MAX_SIZE];
    this.byteBuffer = ByteBuffer.allocateDirect(MAX_SIZE).order( ByteOrder.LITTLE_ENDIAN );
  }

  public long read( int size) throws IOException {
    long readSize = 0;

    try {
      this.byteBuffer.clear();
      readSize = dataSource.read(this.tempBuffer, 0, size);
      if (readSize > 0) {
        this.postion += readSize;
        this.byteBuffer.put(tempBuffer, 0, (int)readSize);
      }
    }catch (Exception e) {
      Log.e(TAG, "read retry " + size);
      if (readSize <= 0) {
        try {
          close();
          try {
            Thread.sleep(100);
          } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
          }
          open(this.postion);
          return read(size);
        }catch (Exception e2) {
          Log.e(TAG, "retry fail");
          return 0;
        }
      }
    }

    return readSize;
  }

  @Override
  public int read(byte[] target, int offset, int length) throws IOException {
    int maxReadSize = length > MAX_SIZE ? MAX_SIZE : length;

    int readSize =(int) read( maxReadSize);

    System.arraycopy(target, offset, tempBuffer, 0, readSize);
    return readSize;
  }

  public long seek(long pos, int whence) throws IOException {
    switch (whence){
      case 0: // SEEK_SET
        if (pos == this.postion) {
          return 0;
        }
        close();
        open(pos);
        this.postion = pos;
        break;
      case 1: // SEEK_CUR
        if (pos == 0)
          return 0;
        close();
        open(this.postion + pos);
        this.postion += pos;
        break;
      case 2: // SEEK_END
        if (pos == 0)
          return 0;
        close();
        open(contentLength + pos);
        this.postion = contentLength + pos;
        break;
      case AVSEEK_SIZE:
        return  contentLength;
    }
    return 0;
  }

  public void open(long offset) throws IOException {

    DataSpec dataSpec = buildDataSpec(offset);
    long length = C.LENGTH_UNSET;
    while (length == C.LENGTH_UNSET) {
      try {
        length = dataSource.open(dataSpec);
      }catch (Exception e) {
        dataSource.close();
        Log.e(TAG, "retry open " + offset);
        try {
          Thread.sleep(500);
        } catch (InterruptedException interruptedException) {
          interruptedException.printStackTrace();
        }
        length = dataSource.open(dataSpec); // retry
      }
    }

    if (length != C.LENGTH_UNSET) {
      contentLength = length + offset;
      if (contentLength <0 ) {
        throw new IOException("read fail");
      }
    }
  }

  @Override
  public void addTransferListener(TransferListener transferListener) {
    dataSource.addTransferListener(transferListener);
  }

  @Override
  public long open(DataSpec dataSpec) throws IOException {
    open(dataSpec.position);
    return 0;
  }

  @Nullable
  @Override
  public Uri getUri() {
    return dataSource.getUri();
  }

  public void close() {
    Util.closeQuietly(dataSource);
  }

  private DataSpec buildDataSpec(long position) {
    // Disable caching if the content length cannot be resolved, since this is indicative of a
    // progressive live stream.
    return new DataSpec.Builder()
        .setUri(uri)
        .setPosition(position)
        .setFlags(
            DataSpec.FLAG_DONT_CACHE_IF_LENGTH_UNKNOWN | DataSpec.FLAG_ALLOW_CACHE_FRAGMENTATION)
        .build();
  }


  public boolean isEos() {
    if (contentLength == C.LENGTH_UNSET)
      return false;
    if(this.postion == contentLength) {
      return true;
    }
    return false;
  }
}
