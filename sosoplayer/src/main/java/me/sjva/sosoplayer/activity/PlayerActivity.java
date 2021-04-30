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
package me.sjva.sosoplayer.activity;

import static com.google.android.exoplayer2.util.Assertions.checkNotNull;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.ext.SosoMediaSourceFactory;
import me.sjva.sosoplayer.R;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
//import com.google.android.exoplayer2.ext.ima.ImaAdsLoader;
import com.google.android.exoplayer2.ext.plex.Video;
import com.google.android.exoplayer2.ext.ui.SubtitleView;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer.DecoderInitializationException;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil.DecoderQueryException;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
//import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSourceFactory;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.ads.AdsLoader;

import com.google.android.exoplayer2.text.CaptionStyleCompat;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector.MappedTrackInfo;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ext.ui.StyledPlayerControlView;
import com.google.android.exoplayer2.ext.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.util.ErrorMessageProvider;
import com.google.android.exoplayer2.util.EventLogger;
import com.google.android.exoplayer2.util.Util;

import net.margaritov.preference.colorpicker.ColorPickerDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import me.sjva.sosoplayer.data.FileInfo;
import me.sjva.sosoplayer.data.StorageInfo;
import me.sjva.sosoplayer.util.DemoUtil;
import me.sjva.sosoplayer.util.IntentUtil;
import me.sjva.sosoplayer.util.PlexUtil;
import me.sjva.sosoplayer.util.SharedPreferencesUtil;
import me.sjva.sosoplayer.widget.ConfirmDialog;
import me.sjva.sosoplayer.widget.SubtitleEdgeTypeDialog;

/** An activity that plays media using {@link SimpleExoPlayer}. */
public class PlayerActivity extends AppCompatActivity
        implements OnClickListener, StyledPlayerControlView.VisibilityListener, StyledPlayerControlView.OnButtonClickEventListener, OnPlayerEventListener {
  // Saved instance state key.
  private static final int MSG_ERROR = 0;
  private static final int MSG_PLAYBACK_COMPLTE= 1;

  public static final String KEY_FILE_INFO_TYPE = "fileinfo_type";
  public static final int KEY_FILE_INFO_TYPE_COMMON = 0;
  public static final int KEY_FILE_INFO_TYPE_PLEX = 1;
  public static final String KEY_STORAGE_INFO = "storageInfo";
  public static final String KEY_FILE_INFO_LIST = "fileinfo_list";
  public static final String KEY_PLEXFILE_INFO_LIST = "plex_fileinfo_list";
  public static final String KEY_SELECTED_POSITION = "selected_position";

  private static final String KEY_TRACK_SELECTOR_PARAMETERS = "track_selector_parameters";
  private static final String KEY_WINDOW = "window";
  private static final String KEY_POSITION = "position";
  private static final String KEY_AUTO_PLAY = "auto_play";

  protected StyledPlayerView playerView;
  protected SubtitleView subtitleView;
  protected LinearLayout debugRootView;
  protected SimpleExoPlayer player;


  private DataSource.Factory dataSourceFactory;
  //private List<MediaItem> mediaItems;

  private DefaultTrackSelector trackSelector;
  private DefaultTrackSelector.Parameters trackSelectorParameters;

  private TrackGroupArray lastSeenTrackGroupArray;
  private boolean startAutoPlay;
  private int startWindow;
  private long startPosition;

  private ArrayList<? extends Parcelable > fileInfos;
  private int mediaItemIndex;
  private int fileInfoType;
  private StorageInfo storageInfo;

  private String currentMediaKey;
  // For ad playback only.


  private SharedPreferencesUtil sharedPreferencesUtil;
  private CaptionStyleCompat captionStyleCompat;
  // Activity lifecycle.
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    dataSourceFactory = DemoUtil.getDataSourceFactory(/* context= */ this);
    currentMediaKey = null;
    setContentView();
    debugRootView = findViewById(R.id.controls_root);
    sharedPreferencesUtil = SharedPreferencesUtil.getInstance(this);

    playerView = findViewById(R.id.player_view);
    playerView.setControllerVisibilityListener(this);
    playerView.setErrorMessageProvider(new PlayerErrorMessageProvider(this));
    playerView.requestFocus();
    playerView.setOnButtonClickEventListener(this);

    subtitleView = playerView.getSubtitleView();
    captionStyleCompat = sharedPreferencesUtil.getCaptionStyleCompat();
    subtitleView.setStyle(captionStyleCompat);

    if (savedInstanceState != null) {
      trackSelectorParameters = savedInstanceState.getParcelable(KEY_TRACK_SELECTOR_PARAMETERS);
      startAutoPlay = savedInstanceState.getBoolean(KEY_AUTO_PLAY);
      startWindow = savedInstanceState.getInt(KEY_WINDOW);
      startPosition = savedInstanceState.getLong(KEY_POSITION);

      mediaItemIndex = savedInstanceState.getInt(KEY_SELECTED_POSITION, 0);
      fileInfoType = savedInstanceState.getInt(KEY_FILE_INFO_TYPE, KEY_FILE_INFO_TYPE_COMMON);
      storageInfo  = savedInstanceState.getParcelable(KEY_STORAGE_INFO);
      switch (fileInfoType){
        case KEY_FILE_INFO_TYPE_COMMON: {
          fileInfos = new ArrayList<FileInfo>();
          fileInfos = savedInstanceState.getParcelableArrayList(KEY_FILE_INFO_LIST);
        }
        break;
        case KEY_FILE_INFO_TYPE_PLEX:  {
          fileInfos = new ArrayList<Video>();
          fileInfos = savedInstanceState.getParcelableArrayList(KEY_PLEXFILE_INFO_LIST);
        }
        break;
      }

    } else {

      Intent intent = getIntent();
      mediaItemIndex = intent.getIntExtra(KEY_SELECTED_POSITION, 0);
      fileInfoType = intent.getIntExtra(KEY_FILE_INFO_TYPE, KEY_FILE_INFO_TYPE_COMMON);
      storageInfo  = intent.getParcelableExtra(KEY_STORAGE_INFO);
      switch (fileInfoType){
        case KEY_FILE_INFO_TYPE_COMMON: {
          fileInfos = new ArrayList<FileInfo>();
          fileInfos = intent.getParcelableArrayListExtra(KEY_FILE_INFO_LIST);
        }
        break;
        case KEY_FILE_INFO_TYPE_PLEX:  {
          fileInfos = new ArrayList<Video>();
          fileInfos = intent.getParcelableArrayListExtra(KEY_PLEXFILE_INFO_LIST);
        }
        break;
      }

      DefaultTrackSelector.ParametersBuilder builder =
          new DefaultTrackSelector.ParametersBuilder(/* context= */ this);
      trackSelectorParameters = builder.build();
      clearStartPosition();
    }


    if (fileInfos.size() > 0) {
      playerView.updateAnotherFileButton(mediaItemIndex, fileInfos);
    }
  }

  @Override
  public void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    releasePlayer();
    clearStartPosition();
    setIntent(intent);
  }

  @Override
  public void onStart() {
    super.onStart();
    if (Util.SDK_INT > 23) {
      initializePlayer();
      if (playerView != null) {
        playerView.onResume();
      }
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    if (Util.SDK_INT <= 23 || player == null) {
      initializePlayer();
      if (playerView != null) {
        playerView.onResume();
      }
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    if (Util.SDK_INT <= 23) {
      if (playerView != null) {
        playerView.onPause();
      }
      releasePlayer();
    }
  }

  @Override
  public void onStop() {
    super.onStop();
    if (Util.SDK_INT > 23) {
      if (playerView != null) {
        playerView.onPause();
      }
      releasePlayer();
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

  private void showErrorExitPopup(String errorMessage) {
    new ConfirmDialog(this, getString(R.string.error_title),
            errorMessage,
            new ConfirmDialog.OnComfirmDialogEventListener(){
              @Override
              public void onComfirm(boolean comfirm) {
                if (comfirm) {
                  finish();
                }
              }
            }).show();
  }

  private void showPlaybackComplte() {
    me.sjva.sosoplayer.util.Util.showToast(this, getString(R.string.playback_complete));
    finish();
  }


  private Handler handler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      switch (msg.what){
        case MSG_ERROR:
          showErrorExitPopup((String)msg.obj);
//          new ConfirmDialog(this, getString(R.string.error_title))
          break;
        case MSG_PLAYBACK_COMPLTE:
          showPlaybackComplte();
          break;
      }
    }
  };

  @Override
  public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    updateTrackSelectorParameters();
    updateStartPosition();
    outState.putParcelable(KEY_TRACK_SELECTOR_PARAMETERS, trackSelectorParameters);
    outState.putBoolean(KEY_AUTO_PLAY, startAutoPlay);
    outState.putInt(KEY_WINDOW, startWindow);
    outState.putLong(KEY_POSITION, startPosition);

    outState.putInt(KEY_SELECTED_POSITION, mediaItemIndex);
    outState.putInt(KEY_FILE_INFO_TYPE, fileInfoType);
    outState.putParcelable(KEY_STORAGE_INFO, storageInfo);


    switch (fileInfoType){
      case KEY_FILE_INFO_TYPE_COMMON: {
        outState.putParcelableArrayList(KEY_FILE_INFO_LIST, fileInfos);
      }
      break;
      case KEY_FILE_INFO_TYPE_PLEX:  {
        outState.putParcelableArrayList(KEY_PLEXFILE_INFO_LIST, fileInfos);
      }
      break;
    }
  }

  // Activity input


  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    // See whether the player view wants to handle media or DPAD keys events.
    if (event.getKeyCode() == KeyEvent.KEYCODE_BACK){
      if (player != null) {
        int lastWindow = player.getCurrentWindowIndex();
        long lastPosition = Math.max(0, player.getContentPosition());
        if (!TextUtils.isEmpty(currentMediaKey)) {
          sharedPreferencesUtil.saveLastPosition(currentMediaKey, lastWindow, lastPosition);
          sharedPreferencesUtil.saveContentDuration(currentMediaKey, player.getDuration());
        }
      }
    }


    return playerView.dispatchKeyEvent(event) || super.dispatchKeyEvent(event);
  }

  // OnClickListener methods

  @Override
  public void onClick(View view) {

  }

  // PlayerControlView.VisibilityListener implementation

  @Override
  public void onVisibilityChange(int visibility) {
    debugRootView.setVisibility(visibility);
  }

  // Internal methods

  protected void setContentView() {
    setContentView(R.layout.activity_player);
  }

  /** @return Whether initialization was successful. */
  protected boolean initializePlayer() {

    if (player == null) {
      RenderersFactory renderersFactory =
          DemoUtil.buildRenderersFactory(/* context= */ this, true);
      MediaSourceFactory mediaSourceFactory =
          new SosoMediaSourceFactory(dataSourceFactory, sharedPreferencesUtil.isUseFfmpeg())
              .setAdViewProvider(playerView);

      trackSelector = new DefaultTrackSelector(/* context= */ this);
      trackSelector.setParameters(trackSelectorParameters);
      lastSeenTrackGroupArray = null;
      player =
          new SimpleExoPlayer.Builder(/* context= */ this, renderersFactory)
              .setMediaSourceFactory(mediaSourceFactory)
              .setTrackSelector(trackSelector)
              .build();
      player.addListener(new PlayerEventListener());
      player.addAnalyticsListener(new EventLogger(trackSelector));
      player.setAudioAttributes(AudioAttributes.DEFAULT, /* handleAudioFocus= */ true);
      player.setPlayWhenReady(startAutoPlay);
      playerView.setPlayer(player);
    }
    if (sharedPreferencesUtil.isKeepLastPlaybackSpeed()) {
      player.setPlaybackParameters(player.getPlaybackParameters().withSpeed(sharedPreferencesUtil.loadLastPlaybackSpeed()));
      playerView.setDefaultPlaybackSpeed(sharedPreferencesUtil.loadLastPlaybackSpeed());
    }

    ArrayList<MediaItem> mediaItems = null;
    switch (fileInfoType) {
      case KEY_FILE_INFO_TYPE_COMMON: {
          FileInfo fileInfo = (FileInfo)fileInfos.get(mediaItemIndex);

          currentMediaKey = fileInfo.getExtraUri(storageInfo).toString();
          mediaItems =fileInfo.getMediaItems(storageInfo);
        }
        break;
      case KEY_FILE_INFO_TYPE_PLEX: {
          Video fileInfo = (Video)fileInfos.get(mediaItemIndex);
          currentMediaKey = PlexUtil.getCurrentMediaKey(storageInfo, fileInfo);
          PlexUtil.startPlayer(storageInfo, fileInfo, this);
          return true;
        }
    }

    if (mediaItems.isEmpty()) {
      return false;
    }
    boolean haveStartPosition = false;
    if (sharedPreferencesUtil.isContinuationPlayback()) {
      Pair<Integer, Long>  lastPostion = sharedPreferencesUtil.loadLastPosition(currentMediaKey);
      if (lastPostion != null) {
        haveStartPosition = lastPostion.first != C.INDEX_UNSET;
        if (haveStartPosition) {
          player.seekTo(lastPostion.first, lastPostion.second);
        }
      } else {
        haveStartPosition = startWindow != C.INDEX_UNSET;
        if (haveStartPosition) {
          player.seekTo(startWindow, startPosition);
        }
      }
    }

    player.setMediaItems(mediaItems, /* resetPosition= */ !haveStartPosition);
    player.prepare();

    return true;
  }



  protected void releasePlayer() {
    if (player != null) {
      updateTrackSelectorParameters();
      updateStartPosition();
      player.release();
      player = null;
  //    mediaItems = Collections.emptyList();
      trackSelector = null;
    }

  }

  private void updateTrackSelectorParameters() {
    if (trackSelector != null) {
      trackSelectorParameters = trackSelector.getParameters();
    }
  }

  private void updateStartPosition() {
    if (player != null) {
      startAutoPlay = player.getPlayWhenReady();
      startWindow = player.getCurrentWindowIndex();
      startPosition = Math.max(0, player.getContentPosition());
    }
  }

  protected void clearStartPosition() {
    startAutoPlay = true;
    startWindow = C.INDEX_UNSET;
    startPosition = C.TIME_UNSET;
  }

  // User controls

  private void showControls() {
    debugRootView.setVisibility(View.VISIBLE);
  }

  private void showToast(int messageId) {
    showToast(getString(messageId));
  }

  private void showToast(String message) {
    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
  }

  private static boolean isBehindLiveWindow(ExoPlaybackException e) {
    if (e.type != ExoPlaybackException.TYPE_SOURCE) {
      return false;
    }
    Throwable cause = e.getSourceException();
    while (cause != null) {
      if (cause instanceof BehindLiveWindowException) {
        return true;
      }
      cause = cause.getCause();
    }
    return false;
  }

  @Override
  public void onPipButtonClick() {

  }

  @Override
  public void onPreButtonClick() {
    mediaItemIndex--;
    if (mediaItemIndex < 0) {
      mediaItemIndex = 0;
      return;
    }

    if (!TextUtils.isEmpty(currentMediaKey)) {
      sharedPreferencesUtil.saveContentDuration(currentMediaKey, player.getDuration());
    }

    playerView.updateAnotherFileButton(mediaItemIndex, fileInfos);
    releasePlayer();
    initializePlayer();
  }

  @Override
  public void onNextButtonClick() {
    mediaItemIndex++;
    if (mediaItemIndex >=  fileInfos.size()) {
      mediaItemIndex = fileInfos.size() - 1;
      return;
    }

    if (!TextUtils.isEmpty(currentMediaKey)) {
      sharedPreferencesUtil.saveContentDuration(currentMediaKey, player.getDuration());
    }


    playerView.updateAnotherFileButton(mediaItemIndex, fileInfos);
    releasePlayer();
    initializePlayer();
  }

  @Override
  public void onSettingsButtonClick(View anchor) {
    showSettings(anchor);
  }

  @Override
  public void onSpeedChange(float speed) {
    sharedPreferencesUtil.saveLastPlaybackSpeed(speed);
  }

  @Override
  public void onStartPlayer(ArrayList<MediaItem> mediaItems) {
    boolean haveStartPosition = startWindow != C.INDEX_UNSET;
    if (haveStartPosition) {
      player.seekTo(startWindow, startPosition);
    }
    player.setMediaItems(mediaItems, /* resetPosition= */ !haveStartPosition);
    player.prepare();
  }

  @Override
  public void onPlexError(Throwable t) {
    handler.sendMessage(handler.obtainMessage(MSG_ERROR, getString(R.string.error_plex)));
  }


  private class PlayerEventListener implements Player.EventListener {

    @Override
    public void onPlaybackStateChanged(@Player.State int playbackState) {
      if (playbackState == Player.STATE_ENDED) {
        if (mediaItemIndex + 1 < fileInfos.size()) {
          sharedPreferencesUtil.removeLastPosition(currentMediaKey);
          onNextButtonClick();
        } else {
          if (currentMediaKey != null) {
            sharedPreferencesUtil.removeLastPosition(currentMediaKey);
            sharedPreferencesUtil.saveContentDuration(currentMediaKey, player.getDuration());
            currentMediaKey = null;
          }
          handler.sendEmptyMessage(MSG_PLAYBACK_COMPLTE);
        }
      }
    }

    @Override
    public void onPlayerError(@NonNull ExoPlaybackException e) {
      if (isBehindLiveWindow(e)) {
        clearStartPosition();
        initializePlayer();
      } else {
        Pair<Integer, String>  result = me.sjva.sosoplayer.util.Util.getErrorMessage(getApplication(), e);
        handler.sendMessage(handler.obtainMessage(MSG_ERROR, result.second));
      }
    }

    @Override
    @SuppressWarnings("ReferenceEquality")
    public void onTracksChanged(
        @NonNull TrackGroupArray trackGroups, @NonNull TrackSelectionArray trackSelections) {
      if (trackGroups != lastSeenTrackGroupArray) {
        MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        if (mappedTrackInfo != null) {
          if (mappedTrackInfo.getTypeSupport(C.TRACK_TYPE_VIDEO)
              == MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
            showToast(R.string.error_unsupported_video);
          }
          if (mappedTrackInfo.getTypeSupport(C.TRACK_TYPE_AUDIO)
              == MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
            showToast(R.string.error_unsupported_audio);
          }
        }
        lastSeenTrackGroupArray = trackGroups;
      }
    }
  }

  private class PlayerErrorMessageProvider implements ErrorMessageProvider<ExoPlaybackException> {
    private Context context;
    public PlayerErrorMessageProvider(Context context) {
      this.context = context;
    }
    @Override
    @NonNull
    public Pair<Integer, String> getErrorMessage(@NonNull ExoPlaybackException e) {
      return me.sjva.sosoplayer.util.Util.getErrorMessage(context, e);
    }
  }

  @Nullable
  private static Map<String, String> getDrmRequestHeaders(MediaItem item) {
    MediaItem.DrmConfiguration drmConfiguration = item.playbackProperties.drmConfiguration;
    return drmConfiguration != null ? drmConfiguration.requestHeaders : null;
  }

  private PopupWindow settingsWindow;
  private void hideSettings() {
    if (settingsWindow != null){
      settingsWindow.dismiss();
      settingsWindow = null;
    }
  }
  private void showSettings(View anchor) {
    hideSettings();
    settingsWindow = new PopupWindow(this);
    RecyclerView settingsView =
            (RecyclerView)
                    LayoutInflater.from(this).inflate(com.google.android.exoplayer2.ext.soso.R.layout.soso_styled_settings_list, null);
//    settingsView.setAdapter(settingsAdapter);
    settingsView.setLayoutManager(new LinearLayoutManager(this));
    settingsWindow =
            new PopupWindow(settingsView, FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, true);
    if (Util.SDK_INT < 23) {
      // Work around issue where tapping outside of the menu area or pressing the back button
      // doesn't dismiss the menu as expected. See: https://github.com/google/ExoPlayer/issues/8272.
      settingsWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }
    settingsWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
      @Override
      public void onDismiss() {
        hideSettings();
      }
    });

    String[] mainTexts = new String[] {
            getString(R.string.soso_settings_title_subtitle_foregroundcolor),
            getString(R.string.soso_settings_title_subtitle_backgroundcolor),
            getString(R.string.soso_settings_title_subtitle_windowcolor),
            getString(R.string.soso_settings_title_subtitle_edgetype),
            getString(R.string.soso_settings_title_subtitle_edgecolor)
    };
    Resources resources = getResources();
    Drawable[] iconIds = new Drawable[] {
            resources.getDrawable(R.drawable.outline_palette_white_24),
            resources.getDrawable(R.drawable.outline_palette_white_24),
            resources.getDrawable(R.drawable.outline_palette_white_24),
            resources.getDrawable(R.drawable.outline_fact_check_white_24),
            resources.getDrawable(R.drawable.outline_palette_white_24)
    };
    SettingsAdapter settingsAdapter = new SettingsAdapter(mainTexts, iconIds);
    settingsView.setAdapter(settingsAdapter);

    settingsWindow.showAsDropDown(anchor);
  }

  private class SettingsAdapter extends RecyclerView.Adapter<SettingViewHolder> {

    private final String[] mainTexts;
    private final String[] subTexts;
    private final Drawable[] iconIds;

    public SettingsAdapter(String[] mainTexts, Drawable[] iconIds) {
      this.mainTexts = mainTexts;
      this.subTexts = new String[mainTexts.length];
      this.iconIds = iconIds;
    }

    @Override
    public SettingViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
      View v =
              LayoutInflater.from(getApplication()).inflate(com.google.android.exoplayer2.ext.soso.R.layout.soso_styled_settings_list_item, null);
      return new SettingViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SettingViewHolder holder, int position) {
      holder.mainTextView.setText(mainTexts[position]);

      if (subTexts[position] == null) {
        holder.subTextView.setVisibility(View.GONE);
      } else {
        holder.subTextView.setText(subTexts[position]);
      }

      if (iconIds[position] == null) {
        holder.iconView.setVisibility(View.GONE);
      } else {
        holder.iconView.setImageDrawable(iconIds[position]);
      }
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public int getItemCount() {
      return mainTexts.length;
    }

    public void setSubTextAtPosition(int position, String subText) {
      this.subTexts[position] = subText;
    }
  }


  private final class SettingViewHolder extends RecyclerView.ViewHolder {

    private final TextView mainTextView;
    private final TextView subTextView;
    private final ImageView iconView;

    public SettingViewHolder(View itemView) {
      super(itemView);
      mainTextView = itemView.findViewById(com.google.android.exoplayer2.ext.soso.R.id.soso_main_text);
      subTextView = itemView.findViewById(com.google.android.exoplayer2.ext.soso.R.id.soso_sub_text);
      iconView = itemView.findViewById(com.google.android.exoplayer2.ext.soso.R.id.soso_icon);
      itemView.setOnClickListener(v -> onSettingViewClicked(getAdapterPosition()));
    }
  }

  private void onSettingViewClicked(int adapterPosition) {
    switch (adapterPosition){
      case 0: {
          new ColorPickerDialog(this, captionStyleCompat.foregroundColor,
                  getString(R.string.soso_settings_title_subtitle_foregroundcolor) )
                  .setOnColorChangedListener(new ColorPickerDialog.OnColorChangedListener() {
                    @Override
                    public void onColorChanged(int color) {
                      updateSutitleInfo(color, captionStyleCompat.backgroundColor,
                              captionStyleCompat.windowColor, captionStyleCompat.edgeType, captionStyleCompat.edgeColor);
                    }
                  }).show();
        }
        break;
      case 1: {
          new ColorPickerDialog(this, captionStyleCompat.backgroundColor,
                  getString(R.string.soso_settings_title_subtitle_backgroundcolor) )
                  .setOnColorChangedListener(new ColorPickerDialog.OnColorChangedListener() {
                    @Override
                    public void onColorChanged(int color) {
                      updateSutitleInfo(captionStyleCompat.foregroundColor, color,
                              captionStyleCompat.windowColor, captionStyleCompat.edgeType, captionStyleCompat.edgeColor);
                    }
                  }).show();
        }
        break;
      case 2: {
          new ColorPickerDialog(this, captionStyleCompat.windowColor,
                  getString(R.string.soso_settings_title_subtitle_windowcolor) )
                  .setOnColorChangedListener(new ColorPickerDialog.OnColorChangedListener() {
                    @Override
                    public void onColorChanged(int color) {
                      updateSutitleInfo(captionStyleCompat.foregroundColor, captionStyleCompat.backgroundColor,
                              color, captionStyleCompat.edgeType, captionStyleCompat.edgeColor);
                    }
                  }).show();
        }
        break;
      case 3: {
          new SubtitleEdgeTypeDialog(this, captionStyleCompat.edgeType, new SubtitleEdgeTypeDialog.OnSubtitleEdgeTypeSelectListener() {
            @Override
            public void onSubtitleEdgeTypeSelect(int type) {
              updateSutitleInfo(captionStyleCompat.foregroundColor, captionStyleCompat.backgroundColor,
                      captionStyleCompat.windowColor, type, captionStyleCompat.edgeColor);
            }
          }).show();
        }
        break;
      case 4: {
          new ColorPickerDialog(this, captionStyleCompat.windowColor,
                  getString(R.string.soso_settings_title_subtitle_windowcolor) )
                  .setOnColorChangedListener(new ColorPickerDialog.OnColorChangedListener() {
                    @Override
                    public void onColorChanged(int color) {
                      updateSutitleInfo(captionStyleCompat.foregroundColor, captionStyleCompat.backgroundColor,
                              captionStyleCompat.windowColor, captionStyleCompat.edgeType, color);
                    }
                  }).show();
        }
        break;
    }
    hideSettings();
  }

  private void updateSutitleInfo(int textColor, int bgColor, int windowColor, int edgeType, int edgeColor) {
    captionStyleCompat = new CaptionStyleCompat(
            textColor,
            bgColor,
            windowColor,
            edgeType,
            edgeColor,
            /* typeface= */ null);
    subtitleView.setStyle(captionStyleCompat);
  }

}
