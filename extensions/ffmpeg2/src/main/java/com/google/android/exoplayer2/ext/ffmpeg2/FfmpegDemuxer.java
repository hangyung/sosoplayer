package com.google.android.exoplayer2.ext.ffmpeg2;

import android.media.MediaFormat;
import android.net.Uri;
import android.util.Pair;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.extractor.Extractor;
import com.google.android.exoplayer2.extractor.ExtractorOutput;
import com.google.android.exoplayer2.extractor.PositionHolder;
import com.google.android.exoplayer2.extractor.SeekMap;

import com.google.android.exoplayer2.extractor.SeekPoint;
import com.google.android.exoplayer2.extractor.TrackOutput;
import com.google.android.exoplayer2.extractor.mp4.Mp4Extractor;
import com.google.android.exoplayer2.extractor.mp4.Track;
import com.google.android.exoplayer2.mediacodec.MediaCodecInfo;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.metadata.id3.BinaryFrame;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.ParsableByteArray;
import com.google.android.exoplayer2.video.AvcConfig;
import com.google.android.exoplayer2.video.HevcConfig;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.checkerframework.checker.nullness.compatqual.NullableType;

public class FfmpegDemuxer implements SeekMap {
  private static final int OUTPUT_BUFFER_SIZE_16BIT = 65536;
  private static final int OUTPUT_BUFFER_SIZE_32BIT = OUTPUT_BUFFER_SIZE_16BIT * 2;

  private long nativeContext; // May be reassigned on resetting the codec.
  private boolean extractorInitialized;

  private long duration;
  private FfmpegPacket ffmpegPacket;

  private FfmpegCustomIo customIo;

  private class FfmpegTrack{
    public int trackType;
    public TrackOutput trackOutput;
    public int nalUnitLengthFieldLength;
    public int trackIndex;
    public ParsableByteArray nalPrefixData;

    public FfmpegTrack(int trackType, int trackIndex, TrackOutput trackOutput, int nalUnitLengthFieldLength) {
      this.trackType = trackType;
      this.trackIndex = trackIndex;
      this.trackOutput = trackOutput;
      this.nalUnitLengthFieldLength = nalUnitLengthFieldLength;
      if (nalUnitLengthFieldLength == 0) {
        nalPrefixData = null;
      } else {
        byte[] nalPreFix = new byte[nalUnitLengthFieldLength + 1];
        nalPreFix[nalUnitLengthFieldLength] = 0x01;
        nalPrefixData = new ParsableByteArray(nalPreFix);
      }
    }

  }

  private Map<Integer, FfmpegTrack> trackMap;

  public void init( Uri uri, DataSource dataSource, ExtractorOutput output) throws IOException {
    if(!extractorInitialized) {
      if (!FfmpegLibrary.isAvailable()) {
        throw new IOException("Failed to load decoder native libraries.");
      }

      customIo = new FfmpegCustomIo(uri, dataSource);
      customIo.open(0);

//      nativeContext = FfmpegDemuxerOpen(uri.toString());
      nativeContext = FfmpegDemuxerCustomOpen(customIo);
      if (nativeContext == 0) {
        throw new IOException();
      }

      int ret = 0;
      ffmpegPacket = new FfmpegPacket();
      trackMap = new HashMap<Integer, FfmpegTrack>();
      int streamCount = FfmpegDemuxerGetStreamCount(nativeContext);
      for (int i = 0; i < streamCount; i++) {
        int streamType = FfmpegDemuxerGetStreamType(nativeContext, i);
        switch (streamType){
          case 0: {//AVMEDIA_TYPE_VIDEO
              ret = FfmpegDemuxerOpenStream(nativeContext, i);
              if (ret < 0) {
                continue;
              }
              TrackOutput trackOutput = output.track(i, C.TRACK_TYPE_VIDEO);
              Format.Builder builder = new Format.Builder();
              String mimeType = FfmpegDemuxerGetCodecId(nativeContext, i);
              String codecs = null;

              mimeType = FfmpegUtil.getVideoMimeType(mimeType);

              builder.setWidth(FfmpegDemuxerGetVideoWidth(nativeContext, i));
              builder.setHeight(FfmpegDemuxerGetVideoHeight(nativeContext, i));
              int nalUnitLengthFieldLength = 0;
              byte[] extradata = FfmpegDemuxerGetExtraData(nativeContext, i);
              if (extradata.length > 0) {
                ParsableByteArray parsableByteArray = new ParsableByteArray(extradata);
                if (mimeType.equals(MimeTypes.VIDEO_H264)) {
                  AvcConfig avcConfig = AvcConfig.parse(parsableByteArray);
                  builder.setInitializationData(avcConfig.initializationData);
                  nalUnitLengthFieldLength = avcConfig.nalUnitLengthFieldLength;
                  codecs = avcConfig.codecs;
                } else if (mimeType.equals(MimeTypes.VIDEO_H265)) {
                  HevcConfig hevcConfig = HevcConfig.parse(parsableByteArray);
                  builder.setInitializationData(hevcConfig.initializationData);
                  nalUnitLengthFieldLength = hevcConfig.nalUnitLengthFieldLength;
                  codecs = hevcConfig.codecs;
                } else if(mimeType.equals(MimeTypes.VIDEO_MP4V)){
                  builder.setInitializationData(ImmutableList.of(extradata));
                }
              }
              if (codecs != null) {
                builder.setCodecs(codecs);
              }

              MediaCodecSelector mediaCodecSelector = MediaCodecSelector.DEFAULT;
              boolean isSupport = false;
              List<MediaCodecInfo> decoderInfos =          null;
              try {
                decoderInfos = mediaCodecSelector.getDecoderInfos(
                    mimeType, false, false);
                builder.setSampleMimeType(mimeType);

                if (decoderInfos != null && decoderInfos.size() > 0) {
                  for (int j = 0; j <  decoderInfos.size(); j++) {
                    isSupport = decoderInfos.get(j).isFormatSupported(builder.build());
                    if (isSupport) {
                      break;
                    }
                  }
                }

              } catch (MediaCodecUtil.DecoderQueryException e) {
                e.printStackTrace();
              }


              if (!isSupport) {
                builder.setSampleMimeType(MediaFormat.MIMETYPE_VIDEO_RAW);
                nalUnitLengthFieldLength = 0;
              } else {
                builder.setSampleMimeType(mimeType);
              }


              if (!isSupport) {
                ByteBuffer byteBuffer = ByteBuffer.allocate(12);
                byteBuffer.putLong(nativeContext);
                byteBuffer.putInt(i);
                byteBuffer.position(0);
                BinaryFrame binaryFrame = new BinaryFrame(FfmpegVideoDecoder.TAG, byteBuffer.array());
                Metadata metadata = new Metadata(binaryFrame);
                builder.setMetadata(metadata);
              }

              Format format = builder.build();
              trackOutput.format(format);
              FfmpegTrack ffmpegTrack = new FfmpegTrack(C.TRACK_TYPE_VIDEO, i, trackOutput, nalUnitLengthFieldLength);
              trackMap.put(i, ffmpegTrack);
            }
            break;
          case 1: {//AVMEDIA_TYPE_AUDIO
              ret = FfmpegDemuxerOpenStream(nativeContext, i);
              if (ret < 0) {
                continue;
              }
              TrackOutput trackOutput =  output.track(i, C.TRACK_TYPE_AUDIO);
              Format.Builder builder = new Format.Builder();
              String mimeType = FfmpegDemuxerGetCodecId(nativeContext, i);
              mimeType = FfmpegUtil.getAudioMimeType(mimeType);

              if(mimeType.equals(MimeTypes.AUDIO_RAW)){
                builder.setPcmEncoding(C.ENCODING_PCM_16BIT);
              }
              builder.setSampleMimeType(mimeType);
              builder.setChannelCount(FfmpegDemuxerGetAudioChannelCount(nativeContext, i));
              builder.setSampleRate(FfmpegDemuxerGetAudioSampleRate(nativeContext, i));

              byte[] extradata = FfmpegDemuxerGetExtraData(nativeContext, i);
              if (extradata != null && extradata.length > 0) {
                builder.setInitializationData(ImmutableList.of(extradata));
              }

              Format format = builder.build();
              trackOutput.format(format);
              FfmpegTrack ffmpegTrack = new FfmpegTrack(C.TRACK_TYPE_AUDIO, i, trackOutput, 0);
              trackMap.put(i, ffmpegTrack);
            }
            break;
        }
      }

      duration = FfmpegDemuxerGetDuration(nativeContext);

      output.seekMap(this);
      output.endTracks();

      extractorInitialized = true;
    }
  }


  public void release() {
    if (nativeContext != 0) {
      FfmpegDemuxerClose(nativeContext);
      nativeContext = 0;
    }
    if (customIo != null) {
      customIo.close();
      customIo = null;
    }
  }

  public void seek( long seekTimeUs) {
    FfmpegDemuxerSeekTo(nativeContext, seekTimeUs);
  }

  public int read() throws IOException {
    int ret = FfmpegDemuxerReadPacket(nativeContext, ffmpegPacket);
    if (ret == 0) {
      FfmpegTrack track = trackMap.get(ffmpegPacket.stream_index);
      switch (track.trackType){
        case C.TRACK_TYPE_AUDIO:
          if (ffmpegPacket.data.capacity() > 0) {
            ffmpegPacket.data.setPosition(0);
            track.trackOutput.sampleData(ffmpegPacket.data, ffmpegPacket.data.capacity());
            track.trackOutput.sampleMetadata(ffmpegPacket.getTimeUs(),C.BUFFER_FLAG_KEY_FRAME, ffmpegPacket.data.capacity(), 0, null);
          }
          break;
        case C.TRACK_TYPE_VIDEO: {
            if (track.nalUnitLengthFieldLength > 0) {
              int sampleBytesWritten = 0;
              int sampleSize = ffmpegPacket.data.limit();
              while (ffmpegPacket.data.bytesLeft() > 4) {
                int size = ffmpegPacket.data.readInt();
                track.nalPrefixData.setPosition(0);
                track.trackOutput.sampleData(track.nalPrefixData, track.nalUnitLengthFieldLength + 1);
                sampleBytesWritten += track.nalUnitLengthFieldLength + 1;
                track.trackOutput.sampleData(ffmpegPacket.data, size);
                sampleBytesWritten += size;
              }
              track.trackOutput.sampleMetadata(ffmpegPacket.pts,ffmpegPacket.flags == 1 ? C.BUFFER_FLAG_KEY_FRAME : 0, sampleBytesWritten, 0, null);

            } else {
              if (ffmpegPacket.data.capacity() > 0) {
                ffmpegPacket.data.setPosition(0);
                track.trackOutput.sampleData(ffmpegPacket.data, ffmpegPacket.data.capacity());
                track.trackOutput.sampleMetadata(ffmpegPacket.getTimeUs(),ffmpegPacket.flags == 1 ? C.BUFFER_FLAG_KEY_FRAME : 0, ffmpegPacket.data.capacity(), 0, null);
              }
             }
          }
          break;
      }
    } else {
      return Extractor.RESULT_END_OF_INPUT;
    }

    return  Extractor.RESULT_CONTINUE ;
  }



  private native long FfmpegDemuxerOpen(
      String path);
  private native long FfmpegDemuxerCustomOpen(
      FfmpegCustomIo customIo);


  private native long FfmpegDemuxerClose(
      long context);

  private native int FfmpegDemuxerGetStreamCount(long context);
  private native int FfmpegDemuxerGetStreamType(long context, int index);

  private native int FfmpegDemuxerOpenStream(long context, int index);

  private native long FfmpegDemuxerGetDuration(long context);

  private native String FfmpegDemuxerGetCodecId(long context, int index);

  private native int FfmpegDemuxerGetAudioChannelCount(long context, int index);
  private native int FfmpegDemuxerGetAudioSampleRate(long context, int index);

  private native int FfmpegDemuxerGetVideoWidth(long context, int index);
  private native int FfmpegDemuxerGetVideoHeight(long context, int index);

  private native byte[] FfmpegDemuxerGetExtraData(long context, int index);


  private native int FfmpegDemuxerReadPacket(long context, FfmpegPacket packet);

  private native int FfmpegDemuxerSeekTo(long context, long seekTimeUs);

  @Override
  public boolean isSeekable() {
    return true;
  }

  @Override
  public long getDurationUs() {
    return duration;
  }

  @Override
  public SeekPoints getSeekPoints(long timeUs) {
    return new SeekPoints(SeekPoint.START);
  }
}
