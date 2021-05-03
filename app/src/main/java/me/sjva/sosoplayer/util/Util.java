package me.sjva.sosoplayer.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Pair;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.util.MimeTypes;
import java.io.File;
import java.util.List;
import me.sjva.sosoplayer.R;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;

public class Util {
  public static final String APPLICATION_SMI = "application/smil";
  private static final String SETTING_SELECTED_ITEM_UUID = "SELECTED_ITEM_UUID";

  public static final int SPAN_MIN_WIDTH = 100;
  public static void permissionDeniedDialog(final Activity activity) {
    new AlertDialog.Builder(activity)
        .setTitle(R.string.dialog_string_cannot_permit)
        .setMessage(R.string.dialog_string_cannot_permit_play_movie)
        .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
          }
        })
        .setPositiveButton(R.string.dialog_string_setting, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
            intent.setData(uri);
            activity.startActivity(intent);
          }
        })
        .setCancelable(false)
        .create()
        .show();
  }


  public static void showToast(Context context, String message) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
  }

  public static String makeDurationMessage(long duration) {
    long h, m, s;

    if (duration <= 0) {
      return "??:??:??";
    }

    h = (duration / 1000) / 60 / 60;
    m = (duration / 1000) / 60 - h * 60;
    s = (duration / 1000) % 60;

    // String s_h = h < 10 ? "0" + h : "" + h;
    // String s_m = m < 10 ? "0" + m : "" + m;
    // String s_s = s < 10 ? "0" + s : "" + s;
    //
    // return ("" + s_h + ":" + s_m + ":" + s_s);

    String s_h = h <= 0 ? "" : "" + h + ":";
    String s_m = m < 10 ? "0" + m + ":" : "" + m + ":";
    String s_s = s < 10 ? "0" + s : "" + s;

    return ("" + s_h + s_m + s_s);
  }

  public static String[] encodingEntries(Context context) {
    SortedMap m = Charset.availableCharsets();
    Set k = m.keySet();
    Iterator i = k.iterator();

     String defaultLang[] = {"Default", "UTF-8", "euc-kr", "iso8859-1"};


    String entries[] = new String[m.size() + defaultLang.length];

    for (int count = 0; count < defaultLang.length; count++) {
      entries[count] = defaultLang[count];
    }

    int langCount = defaultLang.length;
    while (i.hasNext()) {
      String n = (String)i.next();
      Charset e = (Charset)m.get(n);

      entries[langCount] = n;
      langCount++;
    }


    return entries;
  }

  private static boolean checkExtension(String fleName, ArrayList extList) {
    String lowFileName = fleName.toLowerCase();
    int pos = lowFileName.lastIndexOf( "." );
    if (pos > 0) {
      String ext = lowFileName.substring( pos + 1 );
      return extList.contains(ext);
    }
    return false;
  }

  public static boolean isMovie(Context context, String fleName) {
    Resources res = context.getResources();
    String[] extensions = res.getStringArray(R.array.extension_movie);
    ArrayList list = new ArrayList(Arrays.asList(extensions));
    return checkExtension(fleName, list);
  }

  public static boolean isAudio(Context context,String fleName) {
    Resources res = context.getResources();
    String[] extensions = res.getStringArray(R.array.extension_audio);
    ArrayList list = new ArrayList(Arrays.asList(extensions));
    return checkExtension(fleName, list);
  }

  public static boolean isSubtitleFile(String fleName) {
    String extensions[] = {"smi", "srt", "vtt", "ssa", "ass", "ttml", "tx3g"};
    ArrayList list = new ArrayList(Arrays.asList(extensions));
    return checkExtension(fleName, list);
  }

  public static boolean isFontFile(String fleName) {
    String extensions[] = {"font"};
    ArrayList list = new ArrayList(Arrays.asList(extensions));
    return checkExtension(fleName, list);
  }

  public static  String getTextMimeType(String format) {
    if (TextUtils.isEmpty(format)) {
      return null;
    }
    if(format.equals("srt")) {
      return MimeTypes.APPLICATION_SUBRIP;
    } else if(format.equals("vtt")) {
      return MimeTypes.TEXT_VTT;
    } else if(format.equals("ssa")) {
      return MimeTypes.TEXT_SSA;
    } else if(format.equals("ass")) {
      return MimeTypes.TEXT_SSA;
    } else if(format.equals("ttml")) {
      return MimeTypes.APPLICATION_TTML;
    } else if(format.equals("tx3g")) {
      return MimeTypes.APPLICATION_TX3G;
    } else if(format.equals("smi")) {
      return Util.APPLICATION_SMI;
    }
    return null;
  }

  public static  String getTextMimeType(Uri uri) {
    String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
        .toString());
    return getTextMimeType(fileExtension);
  }


  public static ArrayList<Uri> loadSubtitleList(Uri uri) {
    String extensions[] = {"smi", "srt", "vtt", "ssa", "ass", "ttml", "tx3g"};
    List<String> subtitleExtension = Arrays.asList(extensions);
    String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
        .toString());
    String path = uri.getPath();
    File file = new File(path);
    String[] files = file.list();
    if (files == null || files.length <=0)
      return null;
    ArrayList<Uri> subtitleList = new ArrayList<Uri>();
    for (int i = 0; i < files.length; i++) {
      String curFileExtension = MimeTypeMap.getFileExtensionFromUrl(files[i]);
      if (TextUtils.isEmpty(curFileExtension)) {
        continue;
      }
      curFileExtension = curFileExtension.toLowerCase();
      if (subtitleExtension.contains(curFileExtension)) {
        subtitleList.add(Uri.parse(files[i]));
      }

    }
    return subtitleList;
  }

  public static  Pair<Integer, String> getErrorMessage(Context context, @NonNull ExoPlaybackException e) {
    String errorString = context.getString(R.string.error_generic);
    if (e.type == ExoPlaybackException.TYPE_RENDERER) {
      Exception cause = e.getRendererException();
      if (cause instanceof MediaCodecRenderer.DecoderInitializationException) {
        // Special case for decoder initialization failures.
        MediaCodecRenderer.DecoderInitializationException decoderInitializationException =
                (MediaCodecRenderer.DecoderInitializationException) cause;
        if (decoderInitializationException.codecInfo == null) {
          if (decoderInitializationException.getCause() instanceof MediaCodecUtil.DecoderQueryException) {
            errorString = context.getString(R.string.error_querying_decoders);
          } else if (decoderInitializationException.secureDecoderRequired) {
            errorString =
                    context.getString(
                            R.string.error_no_secure_decoder, decoderInitializationException.mimeType);
          } else {
            errorString =
                    context.getString(R.string.error_no_decoder, decoderInitializationException.mimeType);
          }
        } else {
          errorString =
                  context.getString(
                          R.string.error_instantiating_decoder,
                          decoderInitializationException.codecInfo.name);
        }
      }
    }
    return Pair.create(0, errorString);
  }
}
