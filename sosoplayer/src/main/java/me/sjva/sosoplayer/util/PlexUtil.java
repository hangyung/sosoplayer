package me.sjva.sosoplayer.util;

import android.net.Uri;
import android.text.TextUtils;
import com.google.android.exoplayer2.MediaItem;
import java.util.ArrayList;
import java.util.List;
import me.sjva.sosoplayer.activity.MainActivity;
import me.sjva.sosoplayer.data.StorageInfo;
import me.sjva.sosoplayer.fragment.OnFragmentEventListener;
import com.google.android.exoplayer2.ext.plex.Media;
import com.google.android.exoplayer2.ext.plex.MediaContainer;
import com.google.android.exoplayer2.ext.plex.Part;
import com.google.android.exoplayer2.ext.plex.PlexApi;
import com.google.android.exoplayer2.ext.plex.Stream;
import com.google.android.exoplayer2.ext.plex.Video;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlexUtil {

  public static boolean haveSubtitle(Video video) {
    List<Media> medias = video.getMedia();
    for (int i = 0; i < medias.size(); i++) {
      Media media = medias.get(i);
      List<Part> parts = media.getPart();
      if (parts == null)
        continue;
      for (int j = 0; j < parts.size(); j++) {
        Part part = parts.get(j);
        List<Stream> streams = part.getStream();
        if (streams == null)
          continue;
        for (int k = 0 ; k < streams.size(); k++) {
          Stream stream = streams.get(k);
          if (stream.getStreamType().equals("3") && !TextUtils.isEmpty(stream.getKey()) && !TextUtils.isEmpty(Util.getTextMimeType(stream.getFormat()))) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public static void startPlayer(MainActivity activity, StorageInfo storageInfo, Video video, OnFragmentEventListener onFragmentEventListener) {
        String basePath = storageInfo.getPath();
        String token = storageInfo.getToken();
        PlexApi.getMetadata(basePath, token, video.getKey(),
        new Callback<MediaContainer>() {
          @Override
          public void onResponse(Call<MediaContainer> call, Response<MediaContainer> response) {
            MediaContainer container = response.body();
            List<Video> videos =  container.getVideos();
            Video curVideo = videos.get(0);
            List<Media> medias = curVideo.getMedia();

            ArrayList<MediaItem> mediaItems = new ArrayList<MediaItem>();

            for (int i = 0; i < medias.size(); i++) {
              Media media = medias.get(i);
              List<Part> parts = media.getPart();
              if (parts == null ||parts.size() < 0)
                continue;

              for (int j = 0; j < parts.size(); j++) {
                Part part = parts.get(j);
                MediaItem.Builder mediaItem = new MediaItem.Builder();

                String uri = PlexApi.getContentPath(basePath, token, part.getKey());
                mediaItem.setUri(uri);

                List<Stream> streams = part.getStream();
                if (streams == null ||streams.size() < 0)
                  continue;
                ArrayList<MediaItem.Subtitle> subtitles = new ArrayList<MediaItem.Subtitle>();
                if (streams != null && streams.size() > 0) {
                  for (int k = 0 ; k < streams.size(); k++) {
                    Stream stream = streams.get(k);
                    if (stream.getStreamType().equals("3") && !TextUtils.isEmpty(stream.getKey()) && !TextUtils.isEmpty(Util.getTextMimeType(stream.getFormat()))) {
                      String subtitleUri = PlexApi.getContentPath(basePath, token, stream.getKey());
                      MediaItem.Subtitle subtitle = new MediaItem.Subtitle(Uri.parse(subtitleUri), Util.getTextMimeType(stream.getFormat()), stream.getLanguage());
                      subtitles.add(subtitle);
                    }
                  }
                }
                if (subtitles.size() > 0) {
                  mediaItem.setSubtitles(subtitles);
                }
                mediaItems.add(mediaItem.build());
              }
            }

            activity.startPlayer( mediaItems);
          }

          @Override
          public void onFailure(Call<MediaContainer> call, Throwable t) {
            onFragmentEventListener.onError(t);
          }
        });
  }


}
