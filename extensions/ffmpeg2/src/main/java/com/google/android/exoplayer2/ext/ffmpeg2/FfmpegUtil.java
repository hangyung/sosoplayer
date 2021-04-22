package com.google.android.exoplayer2.ext.ffmpeg2;

import static com.google.android.exoplayer2.util.MimeTypes.getMimeTypeFromMp4ObjectType;

import android.media.MediaFormat;
import android.util.Pair;
import androidx.annotation.Nullable;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.ParserException;
import com.google.android.exoplayer2.audio.AacUtil;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.NalUnitUtil;
import com.google.android.exoplayer2.util.ParsableByteArray;
import com.google.android.exoplayer2.video.AvcConfig;
import com.google.android.exoplayer2.video.HevcConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.checkerframework.checker.nullness.compatqual.NullableType;

public class FfmpegUtil {
  public static String getVideoMimeType(String mimeType) {


    if (mimeType.equals("h264")) {
      return MimeTypes.VIDEO_H264;
    } else if (mimeType.equals("h265")) {
      return MimeTypes.VIDEO_H265;
    } else if (mimeType.equals("vp8")) {
      return MimeTypes.VIDEO_VP8;
    } else if (mimeType.equals("vp9")) {
      return MimeTypes.VIDEO_VP9;
    } else if (mimeType.equals("av1")) {
      return MimeTypes.VIDEO_AV1;
    } else if (mimeType.equals("mpeg4")) {
      return MimeTypes.VIDEO_MP4V;
    } else {
      return MediaFormat.MIMETYPE_VIDEO_RAW;
    }
  }

  public static String getAudioMimeType(String mimeType) {
    if (mimeType.equals("aac")) {
      return MimeTypes.AUDIO_AAC;
//    } else if (mimeType.equals("aac_latm")) {
//      return MimeTypes.AUDIO_AAC;
//    } else if (mimeType.equals("ac3")) {
//      return MimeTypes.AUDIO_AC3;
//    } else if (mimeType.equals("eac3")) {
//      return MimeTypes.AUDIO_E_AC3;
//    } else if (mimeType.equals("dts")) {
//      return MimeTypes.AUDIO_DTS;
//    } else if (mimeType.equals("mp3")) {
//      return MimeTypes.AUDIO_MPEG;
    } else {
      return MimeTypes.AUDIO_RAW;
    }
  }

  private static List<byte[]> MakeAVCCodecSpecificData( byte[] extraData) {
    boolean[] prefixFlags = new boolean[3];

    int offset = 0;
    List<byte[]> initializationData = new ArrayList<>();
    while (true) {
      int length = extraData.length - offset;
      int nalUnitOffset = NalUnitUtil.findNalUnit(extraData, offset, length, prefixFlags);

      int nalUnitType = NalUnitUtil.getNalUnitType(extraData, nalUnitOffset);
      int lengthToNalUnit = nalUnitOffset - offset;
      if (lengthToNalUnit <= 0) {
        break;
      }
      if (lengthToNalUnit > 0) {
        if (nalUnitType == 7) {
          byte[] sps =  new byte[3 + lengthToNalUnit];
          sps[2] = 0x01;
          System.arraycopy(extraData, offset, sps, 3, sps.length);
          initializationData.add(Arrays.copyOf(sps, sps.length));
        } else if (nalUnitType == 8) {
          byte[] pps =  new byte[3 + lengthToNalUnit];
          pps[2] = 0x01;
          System.arraycopy(extraData, offset, pps, 3, pps.length);
          initializationData.add(Arrays.copyOf(pps, pps.length));
        }
      }
      offset = nalUnitOffset + 3;
    }

    return initializationData.size() < 2 ? null : initializationData;
  }

  private static int parseExpandableClassSize(ParsableByteArray data) {
    int currentByte = data.readUnsignedByte();
    int size = currentByte & 0x7F;
    while ((currentByte & 0x80) == 0x80) {
      currentByte = data.readUnsignedByte();
      size = (size << 7) | (currentByte & 0x7F);
    }
    return size;
  }

  public static Pair<@NullableType String, byte @NullableType []> parseEsdsFromParent(
      ParsableByteArray parent, int position) {
    parent.setPosition(position + 8 + 4);
    // Start of the ES_Descriptor (defined in ISO/IEC 14496-1)
    parent.skipBytes(1); // ES_Descriptor tag
    parseExpandableClassSize(parent);
    parent.skipBytes(2); // ES_ID

    int flags = parent.readUnsignedByte();
    if ((flags & 0x80 /* streamDependenceFlag */) != 0) {
      parent.skipBytes(2);
    }
    if ((flags & 0x40 /* URL_Flag */) != 0) {
      parent.skipBytes(parent.readUnsignedShort());
    }
    if ((flags & 0x20 /* OCRstreamFlag */) != 0) {
      parent.skipBytes(2);
    }

    // Start of the DecoderConfigDescriptor (defined in ISO/IEC 14496-1)
    parent.skipBytes(1); // DecoderConfigDescriptor tag
    parseExpandableClassSize(parent);

    // Set the MIME type based on the object type indication (ISO/IEC 14496-1 table 5).
    int objectTypeIndication = parent.readUnsignedByte();
    @Nullable String mimeType = getMimeTypeFromMp4ObjectType(objectTypeIndication);
    if (MimeTypes.AUDIO_MPEG.equals(mimeType)
        || MimeTypes.AUDIO_DTS.equals(mimeType)
        || MimeTypes.AUDIO_DTS_HD.equals(mimeType)) {
      return Pair.create(mimeType, null);
    }

    parent.skipBytes(12);

    // Start of the DecoderSpecificInfo.
    parent.skipBytes(1); // DecoderSpecificInfo tag
    int initializationDataSize = parseExpandableClassSize(parent);
    byte[] initializationData = new byte[initializationDataSize];
    parent.readBytes(initializationData, 0, initializationDataSize);
    return Pair.create(mimeType, initializationData);
  }
}
