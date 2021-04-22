package com.google.android.exoplayer2.ext.ffmpeg2;

import static com.google.android.exoplayer2.decoder.DecoderReuseEvaluation.REUSE_RESULT_YES_WITHOUT_RECONFIGURATION;

import android.media.MediaFormat;
import android.os.Handler;
import android.view.Surface;
import androidx.annotation.Nullable;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.RendererCapabilities;
import com.google.android.exoplayer2.decoder.DecoderException;
import com.google.android.exoplayer2.decoder.DecoderReuseEvaluation;
import com.google.android.exoplayer2.drm.ExoMediaCrypto;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.metadata.id3.BinaryFrame;
import com.google.android.exoplayer2.util.TraceUtil;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.DecoderVideoRenderer;
import com.google.android.exoplayer2.video.VideoDecoderOutputBuffer;
import com.google.android.exoplayer2.video.VideoRendererEventListener;
import java.nio.ByteBuffer;

public class FfmpegVideoRenderer extends DecoderVideoRenderer {
  private static final String TAG = "FfmpegRenderer";

  public static final int THREAD_COUNT_AUTODETECT = 0;
  private static final int DEFAULT_NUM_OF_INPUT_BUFFERS = 4;
  private static final int DEFAULT_NUM_OF_OUTPUT_BUFFERS = 4;
  /**
   * Default input buffer size in bytes, based on 720p resolution video compressed by a factor of
   * two.
   */
  private static final int DEFAULT_INPUT_BUFFER_SIZE =
      Util.ceilDivide(1920, 64) * Util.ceilDivide(1080, 64) * (64 * 64 * 3 / 2) / 2;

  /** The number of input buffers. */
  private final int numInputBuffers;
  /**
   * The number of output buffers. The renderer may limit the minimum possible value due to
   * requiring multiple output buffers to be dequeued at a time for it to make progress.
   */
  private final int numOutputBuffers;


  @Nullable private FfmpegVideoDecoder decoder;

  /**
   * @param allowedJoiningTimeMs     The maximum duration in milliseconds for which this video
   *                                 renderer can attempt to seamlessly join an ongoing playback.
   * @param eventHandler             A handler to use when delivering events to {@code eventListener}.
   *                                 May be null if delivery of events is not required.
   * @param eventListener            A listener of events. May be null if delivery of events is not
   *                                 required.
   * @param maxDroppedFramesToNotify The maximum number of frames that can be dropped between
   *                                 invocations of {@link VideoRendererEventListener#onDroppedFrames(int,
   *                                 long)}.
   */
  public FfmpegVideoRenderer(long allowedJoiningTimeMs,
      @Nullable Handler eventHandler,
      @Nullable VideoRendererEventListener eventListener,
      int maxDroppedFramesToNotify) {
    super(allowedJoiningTimeMs, eventHandler, eventListener, maxDroppedFramesToNotify);
    this.numInputBuffers = DEFAULT_NUM_OF_INPUT_BUFFERS;
    this.numOutputBuffers = DEFAULT_NUM_OF_OUTPUT_BUFFERS;
  }


  @Override
  protected FfmpegVideoDecoder createDecoder(Format format, @Nullable ExoMediaCrypto mediaCrypto)
      throws FfmpegDecoderException {
    TraceUtil.beginSection("createFfmpegDecoder");
    int initialInputBufferSize =
        format.maxInputSize != Format.NO_VALUE ? format.maxInputSize : DEFAULT_INPUT_BUFFER_SIZE;

    long nativeContext = -1;
    int streamIndex = -1;
    if(format == null || format.metadata == null) {
      throw new FfmpegDecoderException("FFmpeg index not found");
    }

    for(int i =0; i < format.metadata.length(); i++) {
      Metadata.Entry entry = format.metadata.get(i);
      if (entry instanceof BinaryFrame) {
        BinaryFrame binaryFrame = (BinaryFrame)entry;
        if (!binaryFrame.id.equals(FfmpegVideoDecoder.TAG)) {
          continue;
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(binaryFrame.data);
        nativeContext = byteBuffer.getLong();
        streamIndex = byteBuffer.getInt();
        break;
      }
    }

    if (nativeContext == -1 || streamIndex == -1) {
      throw new FfmpegDecoderException("FFmpeg index not found");
    }


    FfmpegVideoDecoder decoder =
        new FfmpegVideoDecoder(numInputBuffers, numOutputBuffers, initialInputBufferSize, nativeContext, streamIndex);
    this.decoder = decoder;
    TraceUtil.endSection();
    return decoder;
  }


  @Override
  protected void renderOutputBufferToSurface(VideoDecoderOutputBuffer outputBuffer, Surface surface)
      throws DecoderException {
    if (decoder == null) {
      throw new FfmpegDecoderException(
          "Failed to render output buffer to surface: decoder is not initialized.");
    }
    decoder.renderToSurface(outputBuffer, surface);
    outputBuffer.release();
  }

  @Override
  protected void setDecoderOutputMode(int outputMode) {
    if (decoder != null) {
      decoder.setOutputMode(outputMode);
    }
  }

  @Override
  public String getName() {
    return TAG;
  }

  @Override
  public int supportsFormat(Format format) throws ExoPlaybackException {
    if (!MediaFormat.MIMETYPE_VIDEO_RAW.equalsIgnoreCase(format.sampleMimeType)
        || !FfmpegLibrary.isAvailable()) {
      return RendererCapabilities.create(C.FORMAT_UNSUPPORTED_TYPE);
    }
    if (format.exoMediaCryptoType != null) {
      return RendererCapabilities.create(C.FORMAT_UNSUPPORTED_DRM);
    }
    return RendererCapabilities.create(
        C.FORMAT_HANDLED, ADAPTIVE_SEAMLESS, TUNNELING_NOT_SUPPORTED);
  }

  @Override
  protected DecoderReuseEvaluation canReuseDecoder(
      String decoderName, Format oldFormat, Format newFormat) {
    return new DecoderReuseEvaluation(
        decoderName,
        oldFormat,
        newFormat,
        REUSE_RESULT_YES_WITHOUT_RECONFIGURATION,
        /* discardReasons= */ 0);
  }
}
