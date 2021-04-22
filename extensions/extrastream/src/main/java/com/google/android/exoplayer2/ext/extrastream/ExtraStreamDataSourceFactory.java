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
package com.google.android.exoplayer2.ext.extrastream;


import androidx.annotation.Nullable;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.TransferListener;


/** @deprecated Use {@link ExtraStreamDataSource.Factory} instead. */
@Deprecated
public final class ExtraStreamDataSourceFactory implements DataSource.Factory  {


  private final IOType ioType;
  @Nullable private final TransferListener listener;


  public ExtraStreamDataSourceFactory(IOType ioType) {
    this(ioType, null);
  }

  /** @param listener An optional listener. */
  public ExtraStreamDataSourceFactory(@Nullable IOType ioType, @Nullable TransferListener listener) {
    this.listener = listener;
    this.ioType = ioType;
  }

  @Override
  public ExtraStreamDataSource createDataSource() {
    ExtraStreamDataSource dataSource = new ExtraStreamDataSource(ioType);
    if (listener != null) {
      dataSource.addTransferListener(listener);
    }
    return dataSource;
  }
}
