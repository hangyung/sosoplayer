package com.google.android.exoplayer2.ext.soso;

import android.content.Context;
import androidx.annotation.Nullable;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.TransferListener;


/**
 * A {@link DataSource.Factory} that produces {@link DefaultDataSource} instances that delegate to
 * {@link DefaultHttpDataSource}s for non-file/asset/content URIs.
 */
public final class SosoDataSourceFactory implements DataSource.Factory {

  private final Context context;
  @Nullable
  private final TransferListener listener;
  private final DataSource.Factory baseDataSourceFactory;

  /**
   * Creates an instance.
   *
   * @param context A context.
   */
  public SosoDataSourceFactory(Context context) {
    this(context, /* userAgent= */ (String) null, /* listener= */ null);
  }

  /**
   * Creates an instance.
   *
   * @param context A context.
   * @param userAgent The user agent that will be used when requesting remote data, or {@code null}
   *     to use the default user agent of the underlying platform.
   */
  public SosoDataSourceFactory(Context context, @Nullable String userAgent) {
    this(context, userAgent, /* listener= */ null);
  }

  /**
   * Creates an instance.
   *
   * @param context A context.
   * @param userAgent The user agent that will be used when requesting remote data, or {@code null}
   *     to use the default user agent of the underlying platform.
   * @param listener An optional listener.
   */
  public SosoDataSourceFactory(
      Context context, @Nullable String userAgent, @Nullable TransferListener listener) {
    this(context, listener, new DefaultHttpDataSource.Factory().setUserAgent(userAgent));
  }

  /**
   * Creates an instance.
   *
   * @param context A context.
   * @param baseDataSourceFactory A {@link DataSource.Factory} to be used to create a base {@link DataSource}
   *     for {@link DefaultDataSource}.
   * @see DefaultDataSource#DefaultDataSource(Context, DataSource)
   */
  public SosoDataSourceFactory(Context context, DataSource.Factory baseDataSourceFactory) {
    this(context, /* listener= */ null, baseDataSourceFactory);
  }

  /**
   * Creates an instance.
   *
   * @param context A context.
   * @param listener An optional listener.
   * @param baseDataSourceFactory A {@link DataSource.Factory} to be used to create a base {@link DataSource}
   *     for {@link DefaultDataSource}.
   * @see DefaultDataSource#DefaultDataSource(Context, DataSource)
   */
  public SosoDataSourceFactory(
      Context context,
      @Nullable TransferListener listener,
      DataSource.Factory baseDataSourceFactory) {
    this.context = context.getApplicationContext();
    this.listener = listener;
    this.baseDataSourceFactory = baseDataSourceFactory;
  }

  @Override
  public SosoDataSource createDataSource() {
    SosoDataSource dataSource =
        new SosoDataSource(context, baseDataSourceFactory.createDataSource());
    if (listener != null) {
      dataSource.addTransferListener(listener);
    }
    return dataSource;
  }
}
