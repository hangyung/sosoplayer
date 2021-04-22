package com.google.android.exoplayer2.ext.plex;

import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

public class PlexClient {

  public static PlexService getApiService(String url) {
    return getInstance(url).create(PlexService.class);
  }

  private static Retrofit getInstance(String url) {
    return  new Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(SimpleXmlConverterFactory.create())
        .build();
  }

}
