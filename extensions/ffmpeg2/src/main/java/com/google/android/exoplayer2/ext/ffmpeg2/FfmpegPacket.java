package com.google.android.exoplayer2.ext.ffmpeg2;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.util.ParsableByteArray;
import java.nio.ByteBuffer;

public class FfmpegPacket {
  public int stream_index;
  public ParsableByteArray data;
  public long pts;
  public long dts;

  public int flags;

  public FfmpegPacket() {
    this.stream_index = -1;
    this.data = null;
    this.pts = C.TIME_UNSET;
    this.dts = C.TIME_UNSET;
    this.flags = 0;
  }

  public void set(int stream_index, ByteBuffer data, long pts, long dts,  int flags) {
    this.stream_index = stream_index;
    this.data = new ParsableByteArray(data.limit());
    data.get(this.data.getData(), 0, data.limit());
    this.pts = pts;
    this.dts = dts;
    this.flags = flags;
  }

  public long getTimeUs() {
    if (this.dts != C.TIME_UNSET) {
      return this.dts;
    } else if (this.pts != C.TIME_UNSET) {
      return this.pts;
    }
    return C.TIME_UNSET;
  }
}
