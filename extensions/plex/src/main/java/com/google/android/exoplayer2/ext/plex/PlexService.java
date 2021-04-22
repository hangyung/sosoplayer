package com.google.android.exoplayer2.ext.plex;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface PlexService {
  @GET("/library/sections")
  Call<MediaContainer> getLibraryList(@Query("X-Plex-Token") String token);

  @GET("/library/sections/{key}/all")
  Call<MediaContainer> getContentsForSections(@Path("key") String key, @Query("X-Plex-Token") String token);


  @GET
  Call<MediaContainer> getDetails(@Url String key,  @Query("X-Plex-Token") String token);

  @GET
  Call<MediaContainer> getMetadata(@Url String key,  @Query("X-Plex-Token") String token);
}
