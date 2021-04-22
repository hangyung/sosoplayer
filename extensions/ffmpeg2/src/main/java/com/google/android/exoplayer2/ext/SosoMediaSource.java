
package com.google.android.exoplayer2.ext;

import static com.google.android.exoplayer2.util.Assertions.checkNotNull;

import android.net.Uri;
import androidx.annotation.Nullable;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManagerProvider;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.DrmSessionManagerProvider;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.Extractor;
import com.google.android.exoplayer2.source.BaseMediaSource;

import com.google.android.exoplayer2.source.ForwardingTimeline;
import com.google.android.exoplayer2.source.MediaPeriod;
import com.google.android.exoplayer2.source.MediaSourceFactory;
import com.google.android.exoplayer2.source.SequenceableLoader;
import com.google.android.exoplayer2.source.SinglePeriodTimeline;
import com.google.android.exoplayer2.upstream.Allocator;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultLoadErrorHandlingPolicy;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.LoadErrorHandlingPolicy;
import com.google.android.exoplayer2.upstream.TransferListener;

/**
 * Provides one period that loads data from a {@link Uri} and extracted using an {@link Extractor}.
 *
 * <p>If the possible input stream container formats are known, pass a factory that instantiates
 * extractors for them to the constructor. Otherwise, pass a {@link DefaultExtractorsFactory} to use
 * the default extractors. When reading a new stream, the first {@link Extractor} in the array of
 * extractors created by the factory that returns {@code true} from {@link Extractor#sniff} will be
 * used to extract samples from the input stream.
 *
 * <p>Note that the built-in extractor for FLV streams does not support seeking.
 */
public final class SosoMediaSource extends BaseMediaSource
    implements SosoMediaPeriod.Listener {

  /** Factory for {@link SosoMediaSource}s. */
  public static final class Factory implements MediaSourceFactory {

    private final DataSource.Factory dataSourceFactory;

    // private ExtractorsFactory extractorsFactory;
    private boolean usingCustomDrmSessionManagerProvider;
    private DrmSessionManagerProvider drmSessionManagerProvider;
    private LoadErrorHandlingPolicy loadErrorHandlingPolicy;
    private int continueLoadingCheckIntervalBytes;
    @Nullable private String customCacheKey;
    @Nullable private Object tag;

    /**
     * Creates a new factory for {@link SosoMediaSource}s, using the extractors provided by
     * {@link DefaultExtractorsFactory}.
     *
     * @param dataSourceFactory A factory for {@link DataSource}s to read the media.
     */

    /**
     * Creates a new factory for {@link SosoMediaSource}s.
     *
     * @param dataSourceFactory A factory for {@link DataSource}s to read the media.
     */
    public Factory(DataSource.Factory dataSourceFactory) {
      this.dataSourceFactory = dataSourceFactory;
      drmSessionManagerProvider = new DefaultDrmSessionManagerProvider();
      loadErrorHandlingPolicy = new DefaultLoadErrorHandlingPolicy();
      continueLoadingCheckIntervalBytes = DEFAULT_LOADING_CHECK_INTERVAL_BYTES;
    }



    /**
     * @deprecated Use {@link MediaItem.Builder#setCustomCacheKey(String)} and {@link
     *     #createMediaSource(MediaItem)} instead.
     */
    @Deprecated
    public Factory setCustomCacheKey(@Nullable String customCacheKey) {
      this.customCacheKey = customCacheKey;
      return this;
    }

    /**
     * @deprecated Use {@link MediaItem.Builder#setTag(Object)} and {@link
     *     #createMediaSource(MediaItem)} instead.
     */
    @Deprecated
    public Factory setTag(@Nullable Object tag) {
      this.tag = tag;
      return this;
    }

    /**
     * Sets the {@link LoadErrorHandlingPolicy}. The default value is created by calling {@link
     * DefaultLoadErrorHandlingPolicy#DefaultLoadErrorHandlingPolicy()}.
     *
     * @param loadErrorHandlingPolicy A {@link LoadErrorHandlingPolicy}.
     * @return This factory, for convenience.
     */
    public Factory setLoadErrorHandlingPolicy(
        @Nullable LoadErrorHandlingPolicy loadErrorHandlingPolicy) {
      this.loadErrorHandlingPolicy =
          loadErrorHandlingPolicy != null
              ? loadErrorHandlingPolicy
              : new DefaultLoadErrorHandlingPolicy();
      return this;
    }

    /**
     * Sets the number of bytes that should be loaded between each invocation of {@link
     * MediaPeriod.Callback#onContinueLoadingRequested(SequenceableLoader)}. The default value is
     * {@link #DEFAULT_LOADING_CHECK_INTERVAL_BYTES}.
     *
     * @param continueLoadingCheckIntervalBytes The number of bytes that should be loaded between
     *     each invocation of {@link
     *     MediaPeriod.Callback#onContinueLoadingRequested(SequenceableLoader)}.
     * @return This factory, for convenience.
     */
    public Factory setContinueLoadingCheckIntervalBytes(int continueLoadingCheckIntervalBytes) {
      this.continueLoadingCheckIntervalBytes = continueLoadingCheckIntervalBytes;
      return this;
    }

    @Override
    public Factory setDrmSessionManagerProvider(
        @Nullable DrmSessionManagerProvider drmSessionManagerProvider) {
      if (drmSessionManagerProvider != null) {
        this.drmSessionManagerProvider = drmSessionManagerProvider;
        this.usingCustomDrmSessionManagerProvider = true;
      } else {
        this.drmSessionManagerProvider = new DefaultDrmSessionManagerProvider();
        this.usingCustomDrmSessionManagerProvider = false;
      }
      return this;
    }

    public Factory setDrmSessionManager(@Nullable DrmSessionManager drmSessionManager) {
      if (drmSessionManager == null) {
        setDrmSessionManagerProvider(null);
      } else {
        setDrmSessionManagerProvider(unusedMediaItem -> drmSessionManager);
      }
      return this;
    }

    @Override
    public Factory setDrmHttpDataSourceFactory(
        @Nullable HttpDataSource.Factory drmHttpDataSourceFactory) {
      if (!usingCustomDrmSessionManagerProvider) {
        ((DefaultDrmSessionManagerProvider) drmSessionManagerProvider)
            .setDrmHttpDataSourceFactory(drmHttpDataSourceFactory);
      }
      return this;
    }

    @Override
    public Factory setDrmUserAgent(@Nullable String userAgent) {
      if (!usingCustomDrmSessionManagerProvider) {
        ((DefaultDrmSessionManagerProvider) drmSessionManagerProvider).setDrmUserAgent(userAgent);
      }
      return this;
    }

    /** @deprecated Use {@link #createMediaSource(MediaItem)} instead. */
    @SuppressWarnings("deprecation")
    @Deprecated
    @Override
    public SosoMediaSource createMediaSource(Uri uri) {
      return createMediaSource(new MediaItem.Builder().setUri(uri).build());
    }

    /**
     * Returns a new {@link SosoMediaSource} using the current parameters.
     *
     * @param mediaItem The {@link MediaItem}.
     * @return The new {@link SosoMediaSource}.
     * @throws NullPointerException if {@link MediaItem#playbackProperties} is {@code null}.
     */
    @Override
    public SosoMediaSource createMediaSource(MediaItem mediaItem) {
      checkNotNull(mediaItem.playbackProperties);
      boolean needsTag = mediaItem.playbackProperties.tag == null && tag != null;
      boolean needsCustomCacheKey =
          mediaItem.playbackProperties.customCacheKey == null && customCacheKey != null;
      if (needsTag && needsCustomCacheKey) {
        mediaItem = mediaItem.buildUpon().setTag(tag).setCustomCacheKey(customCacheKey).build();
      } else if (needsTag) {
        mediaItem = mediaItem.buildUpon().setTag(tag).build();
      } else if (needsCustomCacheKey) {
        mediaItem = mediaItem.buildUpon().setCustomCacheKey(customCacheKey).build();
      }
      return new SosoMediaSource(
          mediaItem,
          dataSourceFactory,
          drmSessionManagerProvider.get(mediaItem),
          loadErrorHandlingPolicy,
          continueLoadingCheckIntervalBytes);
    }

    @Override
    public int[] getSupportedTypes() {
      return new int[] {C.TYPE_OTHER};
    }
  }

  /**
   * The default number of bytes that should be loaded between each each invocation of {@link
   * MediaPeriod.Callback#onContinueLoadingRequested(SequenceableLoader)}.
   */
  public static final int DEFAULT_LOADING_CHECK_INTERVAL_BYTES = 1024 * 1024;

  private final MediaItem mediaItem;
  private final MediaItem.PlaybackProperties playbackProperties;
  private final DataSource.Factory dataSourceFactory;
  private final DrmSessionManager drmSessionManager;
  private final LoadErrorHandlingPolicy loadableLoadErrorHandlingPolicy;
  private final int continueLoadingCheckIntervalBytes;

  private boolean timelineIsPlaceholder;
  private long timelineDurationUs;
  private boolean timelineIsSeekable;
  private boolean timelineIsLive;
  @Nullable private TransferListener transferListener;


  // TODO: Make private when ExtractorMediaSource is deleted.
  /* package */ SosoMediaSource(
      MediaItem mediaItem,
      DataSource.Factory dataSourceFactory,
      DrmSessionManager drmSessionManager,
      LoadErrorHandlingPolicy loadableLoadErrorHandlingPolicy,
      int continueLoadingCheckIntervalBytes) {
    this.playbackProperties = checkNotNull(mediaItem.playbackProperties);
    this.mediaItem = mediaItem;
    this.dataSourceFactory = dataSourceFactory;
    this.drmSessionManager = drmSessionManager;
    this.loadableLoadErrorHandlingPolicy = loadableLoadErrorHandlingPolicy;
    this.continueLoadingCheckIntervalBytes = continueLoadingCheckIntervalBytes;
    this.timelineIsPlaceholder = true;
    this.timelineDurationUs = C.TIME_UNSET;
  }

  /**
   * @deprecated Use {@link #getMediaItem()} and {@link MediaItem.PlaybackProperties#tag} instead.
   */
  @SuppressWarnings("deprecation")
  @Deprecated
  @Override
  @Nullable
  public Object getTag() {
    return playbackProperties.tag;
  }

  @Override
  public MediaItem getMediaItem() {
    return mediaItem;
  }

  @Override
  protected void prepareSourceInternal(@Nullable TransferListener mediaTransferListener) {
    transferListener = mediaTransferListener;
    drmSessionManager.prepare();
    notifySourceInfoRefreshed();
  }

  @Override
  public void maybeThrowSourceInfoRefreshError() {
    // Do nothing.
  }

  @Override
  public MediaPeriod createPeriod(MediaPeriodId id, Allocator allocator, long startPositionUs) {
    DataSource dataSource = dataSourceFactory.createDataSource();
    if (transferListener != null) {
      dataSource.addTransferListener(transferListener);
    }
    return new SosoMediaPeriod(
        playbackProperties.uri,
        dataSource,
        drmSessionManager,
        createDrmEventDispatcher(id),
        loadableLoadErrorHandlingPolicy,
        createEventDispatcher(id),
        this,
        allocator,
        playbackProperties.customCacheKey,
        continueLoadingCheckIntervalBytes);
  }

  @Override
  public void releasePeriod(MediaPeriod mediaPeriod) {
    ((SosoMediaPeriod) mediaPeriod).release();
  }

  @Override
  protected void releaseSourceInternal() {
    drmSessionManager.release();
  }

  // FfmpegMediaPeriod.Listener implementation.

  @Override
  public void onSourceInfoRefreshed(long durationUs, boolean isSeekable, boolean isLive) {
    // If we already have the duration from a previous source info refresh, use it.
    durationUs = durationUs == C.TIME_UNSET ? timelineDurationUs : durationUs;
    if (!timelineIsPlaceholder
        && timelineDurationUs == durationUs
        && timelineIsSeekable == isSeekable
        && timelineIsLive == isLive) {
      // Suppress no-op source info changes.
      return;
    }
    timelineDurationUs = durationUs;
    timelineIsSeekable = isSeekable;
    timelineIsLive = isLive;
    timelineIsPlaceholder = false;
    notifySourceInfoRefreshed();
  }

  // Internal methods.

  private void notifySourceInfoRefreshed() {
    // TODO: Split up isDynamic into multiple fields to indicate which values may change. Then
    // indicate that the duration may change until it's known. See [internal: b/69703223].
    Timeline timeline =
        new SinglePeriodTimeline(
            timelineDurationUs,
            timelineIsSeekable,
            /* isDynamic= */ false,
            /* useLiveConfiguration= */ timelineIsLive,
            /* manifest= */ null,
            mediaItem);
    if (timelineIsPlaceholder) {
      // TODO: Actually prepare the extractors during preparation so that we don't need a
      // placeholder. See https://github.com/google/ExoPlayer/issues/4727.
      timeline =
          new ForwardingTimeline(timeline) {
            @Override
            public Window getWindow(
                int windowIndex, Window window, long defaultPositionProjectionUs) {
              super.getWindow(windowIndex, window, defaultPositionProjectionUs);
              window.isPlaceholder = true;
              return window;
            }
          };
    }
    refreshSourceInfo(timeline);
  }
}
