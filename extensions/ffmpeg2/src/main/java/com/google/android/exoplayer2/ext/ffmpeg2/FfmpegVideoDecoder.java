package com.google.android.exoplayer2.ext.ffmpeg2;

import android.view.Surface;
import androidx.annotation.Nullable;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.decoder.DecoderInputBuffer;
import com.google.android.exoplayer2.decoder.SimpleDecoder;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoDecoderInputBuffer;
import com.google.android.exoplayer2.video.VideoDecoderOutputBuffer;
import java.nio.ByteBuffer;

public final class FfmpegVideoDecoder
      extends SimpleDecoder<VideoDecoderInputBuffer, VideoDecoderOutputBuffer, FfmpegDecoderException> {
  public static final String TAG = "video/ffmpeg";

  private final long nativeContext;
  private final int streamIndex;

  private static final int FFMPEG_DECODE_ONLY = 1000;

  @C.VideoOutputMode private volatile int outputMode;

  public FfmpegVideoDecoder(int numInputBuffers, int numOutputBuffers, int initialInputBufferSize, long nativeContext , int streamIndex) {
    super(
        new VideoDecoderInputBuffer[numInputBuffers],
        new VideoDecoderOutputBuffer[numOutputBuffers]);
    this.setInitialInputBufferSize(initialInputBufferSize);
    this.nativeContext = nativeContext;
    this.streamIndex = streamIndex;
  }

  @Override
  protected VideoDecoderInputBuffer createInputBuffer() {
    return new VideoDecoderInputBuffer(DecoderInputBuffer.BUFFER_REPLACEMENT_MODE_DIRECT);
  }

  @Override
  protected VideoDecoderOutputBuffer createOutputBuffer() {
    return new VideoDecoderOutputBuffer(this::releaseOutputBuffer);
  }

  @Nullable
  @Override
  protected FfmpegDecoderException decode(VideoDecoderInputBuffer inputBuffer, VideoDecoderOutputBuffer outputBuffer, boolean reset) {


    ByteBuffer inputData = Util.castNonNull(inputBuffer.data);
    int inputSize = inputData.limit();
    if (FfmpegDecode(nativeContext, streamIndex, inputData, inputSize) < 0) {
      return new FfmpegDecoderException(
          "FfmpegDecodercode error" );
    }

    boolean decodeOnly = inputBuffer.isDecodeOnly();
    if (!decodeOnly) {
      outputBuffer.init(inputBuffer.timeUs, outputMode, /* supplementalData= */ null);
    }

    // We need to dequeue the decoded frame from the decoder even when the input data is
    // decode-only.
    int getFrameResult = FfmpegGetFrame(nativeContext, streamIndex, outputBuffer, outputMode);
    if (getFrameResult < 0) {
      return new FfmpegDecoderException(
          "FfmpegDecodercode error" );
    }
    if (getFrameResult == FFMPEG_DECODE_ONLY) {
      outputBuffer.addFlag(C.BUFFER_FLAG_DECODE_ONLY);
    }
    if (!decodeOnly) {
      outputBuffer.format = inputBuffer.format;
    }

    return null;
  }


  @Override
  protected FfmpegDecoderException createUnexpectedDecodeException(Throwable error) {
    return new FfmpegDecoderException("Unexpected decode error", error);
  }

  @Override
  public void release() {
    super.release();
  }

  @Override
  protected void releaseOutputBuffer(VideoDecoderOutputBuffer buffer) {
    // Decode only frames do not acquire a reference on the internal decoder buffer and thus do not
    // require a call to gav1ReleaseFrame.
    if (buffer.mode == C.VIDEO_OUTPUT_MODE_SURFACE_YUV && !buffer.isDecodeOnly()) {
      FfmpegReleaseFrame(nativeContext, streamIndex, buffer);
    }
    super.releaseOutputBuffer(buffer);
  }
  @Override
  public String getName() {
    return TAG;
  }

  public void setOutputMode(@C.VideoOutputMode int outputMode) {
    this.outputMode = outputMode;
  }

  public void renderToSurface(VideoDecoderOutputBuffer outputBuffer, Surface surface)
      throws FfmpegDecoderException {
    if (outputBuffer.mode != C.VIDEO_OUTPUT_MODE_SURFACE_YUV) {
      throw new FfmpegDecoderException("Invalid output mode.");
    }
    if (FfmpegRenderFrame(nativeContext, streamIndex, surface, outputBuffer, outputBuffer.mode) < 0) {
      throw new  FfmpegDecoderException(
          "FfmpegDecodercode error" );
    }
  }

  private native int FfmpegDecode(long context, int streamIndex,  ByteBuffer encodedData, int length);
  private native int FfmpegGetFrame(
      long context, int streamIndex, VideoDecoderOutputBuffer outputBuffer, int outputMode);

  private native int FfmpegRenderFrame(
      long context, int streamIndex, Surface surface, VideoDecoderOutputBuffer outputBuffer, int outputMode);
  private native void FfmpegReleaseFrame(long context, int streamIndex, VideoDecoderOutputBuffer outputBuffer);
}
