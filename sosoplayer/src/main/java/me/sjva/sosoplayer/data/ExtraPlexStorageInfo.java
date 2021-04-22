package me.sjva.sosoplayer.data;

import com.google.android.exoplayer2.ext.plex.MediaContainer;
import com.google.android.exoplayer2.ext.plex.PlexApi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExtraPlexStorageInfo implements ExtraStorageInfo{

  public interface OnPlexLoadEventListener {
    void onSectionLoaded(MediaContainer sectionMediaContainer);
    void onContentsLoaded(MediaContainer contentMediaContainer);
    void onContentsDetailsLoaded(MediaContainer detailsMediaContainer);
    void onError(Throwable t);
  }

  enum PlexState{
    Secttion,
    Contents,
    Details
  }
  private PlexState plexState;
  private String sectionTitle;
  private String sectionKey;

  private String contentKey;
  private String contentTitle;

  public ExtraPlexStorageInfo(){
    plexState = PlexState.Secttion;
  }


  public void load(String path, String token, OnPlexLoadEventListener onPlexLoadEventListener) {
    switch (plexState){
      case Secttion:
        PlexApi.getSections(path, token, new Callback<MediaContainer>(){
          @Override
          public void onResponse(Call<MediaContainer> call, Response<MediaContainer> response) {
            MediaContainer sectionMediaContainer = response.body();
            onPlexLoadEventListener.onSectionLoaded(sectionMediaContainer);
          }

          @Override
          public void onFailure(Call<MediaContainer> call, Throwable t) {
            onPlexLoadEventListener.onError(t);
          }
        });
        break;
      case Contents:
        PlexApi.getContentsForSections(path, token, sectionKey, new Callback<MediaContainer>() {
          @Override
          public void onResponse(Call<MediaContainer> call, Response<MediaContainer> response) {
            MediaContainer contentMediaContainer = response.body();
            onPlexLoadEventListener.onContentsLoaded(contentMediaContainer);
          }

          @Override
          public void onFailure(Call<MediaContainer> call, Throwable t) {
            onPlexLoadEventListener.onError(t);
          }
        });
        break;
      case Details:
        PlexApi.getDetails(path, token, contentKey, new Callback<MediaContainer>() {
          @Override
          public void onResponse(Call<MediaContainer> call, Response<MediaContainer> response) {
            MediaContainer detailsMediaContainer = response.body();
            onPlexLoadEventListener.onContentsDetailsLoaded(detailsMediaContainer);
          }

          @Override
          public void onFailure(Call<MediaContainer> call, Throwable t) {
            onPlexLoadEventListener.onError(t);
          }
        });
        break;
    }
  }
  public boolean onBackPressed(String path, String token,OnPlexLoadEventListener onPlexLoadEventListener) {
    switch (plexState) {
      case Secttion:
        return  true;
      case Contents:
        plexState = PlexState.Secttion;
        sectionKey = null;
        sectionTitle = null;
        load(path, token, onPlexLoadEventListener);
        return false;
      case Details:
        plexState = PlexState.Contents;
        contentKey = null;
        contentTitle = null;
        load(path, token, onPlexLoadEventListener);
        return false;
    }
    return false;
  }

  public String getTitle(String name) {
    switch (plexState) {
      case Secttion:
        return name;
      case Contents:
        return String.format("%s's %s", name, sectionTitle);
      case Details:
        return String.format("%s's %s", name, contentTitle);
    }
    return name;
  }

  public void selectSection(String title, String key) {
    plexState = PlexState.Contents;
    sectionKey = key;
    sectionTitle = title;
  }

  public void setContent(String title, String key) {
    plexState = PlexState.Details;
    contentKey = key;
    contentTitle = title;
  }


}
