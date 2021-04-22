package com.google.android.exoplayer2.ext.plex;

import retrofit2.Call;
import retrofit2.Callback;

public class PlexApi {
  public static  void getSections(String url, String token, Callback<MediaContainer> callback) {
    Call<MediaContainer> call =  PlexClient.getApiService(url).getLibraryList(token);

    call.enqueue(callback);
  }

  public static  void getContentsForSections(String url,  String token,  String section, Callback<MediaContainer> callback) {
    Call<MediaContainer> call =  PlexClient.getApiService(url).getContentsForSections(section, token);
    call.enqueue(callback);
  }

  public static void getDetails(String url,  String token, String key,Callback<MediaContainer> callback) {
    Call<MediaContainer> call =  PlexClient.getApiService(url).getDetails( key, token);
    call.enqueue(callback);
  }

  public static void getMetadata(String url,  String token, String key,Callback<MediaContainer> callback) {
    Call<MediaContainer> call =  PlexClient.getApiService(url).getMetadata( key, token);
    call.enqueue(callback);
  }

  public static String getContentPath(String base_url, String base_token, String path) {
    return String.format("%s%s?X-Plex-Token=%s", base_url, path, base_token);
  }

  public static String getContentPath(String base_url, String path) {
    return String.format("%s%s", base_url, path );
  }
}
