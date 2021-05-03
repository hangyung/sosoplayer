/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.sjva.sosoplayer.util;

import android.content.Context;
import android.os.Build;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerLibraryInfo;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.database.DatabaseProvider;
import com.google.android.exoplayer2.ext.soso.SosoDataSourceFactory;
import me.sjva.sosoplayer.BuildConfig;
//import com.google.android.exoplayer2.ext.cronet.CronetDataSource;
//import com.google.android.exoplayer2.ext.cronet.CronetEngineWrapper;
import com.google.android.exoplayer2.ext.ffmpeg2.FfmpegRenderersFactory;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.cache.Cache;
import java.io.File;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.concurrent.Executors;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

/** Utility methods for the demo app. */
public final class DemoUtil {

  private static final String USER_AGENT =
      "SoSoPlayer/"
          + ExoPlayerLibraryInfo.VERSION
          + " (Linux; Android "
          + Build.VERSION.RELEASE
          + ") "
          + ExoPlayerLibraryInfo.VERSION_SLASHY;

  private static DataSource.@MonotonicNonNull Factory dataSourceFactory;
  private static HttpDataSource.@MonotonicNonNull Factory httpDataSourceFactory;


  /** Returns whether extension renderers should be used. */
  public static boolean useExtensionRenderers() {
    return BuildConfig.USE_DECODER_EXTENSIONS;
  }

  public static RenderersFactory buildRenderersFactory(
      Context context, boolean preferExtensionRenderer) {
    @DefaultRenderersFactory.ExtensionRendererMode
    int extensionRendererMode =
        useExtensionRenderers()
            ? (preferExtensionRenderer
                ? DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
                : DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
            : DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF;
    return new FfmpegRenderersFactory(context.getApplicationContext())
        .setExtensionRendererMode(extensionRendererMode);
  }

  public static synchronized HttpDataSource.Factory getHttpDataSourceFactory(Context context) {
    if (httpDataSourceFactory == null) {
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
        CookieHandler.setDefault(cookieManager);
        httpDataSourceFactory = new DefaultHttpDataSource.Factory().setUserAgent(USER_AGENT);
    }
    return httpDataSourceFactory;
  }

  /** Returns a {@link DataSource.Factory}. */
  public static synchronized DataSource.Factory getDataSourceFactory(Context context) {
    if (dataSourceFactory == null) {
      context = context.getApplicationContext();
      SosoDataSourceFactory upstreamFactory =
          new SosoDataSourceFactory(context, getHttpDataSourceFactory(context));
      dataSourceFactory = upstreamFactory;
    }
    return dataSourceFactory;
  }


  private DemoUtil() {}
}
