package me.sjva.sosoplayer.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Pair;

import androidx.preference.PreferenceManager;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.text.CaptionStyleCompat;
import com.google.android.exoplayer2.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import me.sjva.sosoplayer.data.StorageInfo;
import me.sjva.sosoplayer.data.StorageType;
import me.sjva.sosoplayer.fragment.SettingsFragment;

import com.google.gson.Gson;
public class SharedPreferencesUtil {
  private static final String SETTING_FILE_NAME = "soso";
  private static final String KEY_STORAGE_LIST = "KEY_STORAGE_LIST";
  private static final String KEY_LAST_STORAGE = "KEY_LAST_STORAGE";

  private static final String KEY_LAST_WINDOW_INDEX = "KEY_LAST_WINDOW_INDEX";
  private static final String KEY_LAST_POSTION= "KEY_LAST_POSTION";
  private static final String KEY_CONTENT_DURATION= "KEY_CONTENT_DURATION";
  private static final String KEY_PLAYBACK_SPEED= "KEY_PLAYBACK_SPEED";
  private static SharedPreferencesUtil instance;
  public static SharedPreferencesUtil getInstance(Context context){
    if (instance == null) {
      instance = new SharedPreferencesUtil(context);
    }
    return instance;
  }

  private SharedPreferences preferences;
  private SharedPreferences.Editor editor;
  private SharedPreferences settingsPreferences;
  private SharedPreferencesUtil(Context context){
    preferences = context.getSharedPreferences(SETTING_FILE_NAME, Activity.MODE_PRIVATE);
    editor = preferences.edit();
    settingsPreferences = PreferenceManager.getDefaultSharedPreferences(context);
  }

  private StorageListHelper loadStorageListHelper() {
    Gson gson = new Gson();
    StorageListHelper storageListHelper = null;
    String storageListJson =  preferences.getString(KEY_STORAGE_LIST, null);
    if (TextUtils.isEmpty(storageListJson)) {
      storageListHelper = new StorageListHelper();
    } else {
      storageListHelper = gson.fromJson(storageListJson, StorageListHelper.class);
    }
    return storageListHelper;
  }

  public boolean haveSameStorageInfo(StorageInfo storageInfo) {
    ArrayList<StorageInfo>  storageInfos  = getStorageInfoList();
    for (StorageInfo curStorageInfo : storageInfos ) {
      if(compareStorageInfo(curStorageInfo,storageInfo) == 0) {
        return true;
      }
    }
    return false;
  }

  public void addStorageInfo(StorageInfo storageInfo) {
    StorageListHelper storageListHelper = loadStorageListHelper();

    Gson gson = new Gson();
    String json = gson.toJson(storageInfo);
    storageListHelper.addStorageInfoString(json);
    json = gson.toJson(storageListHelper);

    editor.putString(KEY_STORAGE_LIST, json);
    editor.commit();
  }

  public ArrayList<StorageInfo> getStorageInfoList() {
    ArrayList<StorageInfo> storageInfos = new ArrayList<>();
    StorageListHelper storageListHelper = loadStorageListHelper();
    ArrayList<String> savedStorageInfos = storageListHelper.getStorageInfos();
    for (String json : savedStorageInfos){
      Gson gson = new Gson();
      StorageInfo storageInfo = gson.fromJson(json, StorageInfo.class);
      storageInfos.add(storageInfo);
    }
    Collections.sort(storageInfos,this::compareStorageInfo);
    return storageInfos;
  }

  public void setLastStorage(StorageInfo storageInfo) {
    Gson gson = new Gson();
    String json = gson.toJson(storageInfo);
    editor.putString(KEY_LAST_STORAGE, json);
    editor.commit();
  }

  public StorageInfo getLastStorage() {
    Gson gson = new Gson();
    String json = preferences.getString(KEY_LAST_STORAGE, null);
    if (TextUtils.isEmpty(json)) {
      return  new StorageInfo(StorageType.MediaStore);
    }
    StorageInfo storageInfo = gson.fromJson(json, StorageInfo.class);
    return storageInfo;
  }


  public int compareStorageInfo(StorageInfo storageInfo1, StorageInfo storageInfo2) {
    if(storageInfo1.getStorageType().getInt() > storageInfo1.getStorageType().getInt()) {
      return -1;
    }
    return storageInfo1.getName().compareTo(storageInfo2.getName());
  }

  public void modifyStorageInfo(StorageInfo orgStorageInfo, StorageInfo newStorageInfo) {
    ArrayList<StorageInfo>  orgStorageInfoList =   getStorageInfoList();
    StorageListHelper storageListHelper = new StorageListHelper();
    for(StorageInfo storageInfo : orgStorageInfoList) {
      if (storageInfo.equals(orgStorageInfo)) {
        Gson gson = new Gson();
        String json = gson.toJson(newStorageInfo);
        storageListHelper.addStorageInfoString(json);
      } else {
        Gson gson = new Gson();
        String json = gson.toJson(storageInfo);
        storageListHelper.addStorageInfoString(json);
      }
    }
    Gson gson = new Gson();
    String  json = gson.toJson(storageListHelper);
    editor.putString(KEY_STORAGE_LIST, json);
    editor.commit();
  }

  public void removeStorageInfo(StorageInfo orgStorageInfo) {
    ArrayList<StorageInfo>  orgStorageInfoList =   getStorageInfoList();
    StorageListHelper storageListHelper = new StorageListHelper();
    for(StorageInfo storageInfo : orgStorageInfoList) {
      if (storageInfo.equals(orgStorageInfo)) {
        continue; // skip
      } else {
        Gson gson = new Gson();
        String json = gson.toJson(storageInfo);
        storageListHelper.addStorageInfoString(json);
      }
    }

    Gson gson = new Gson();
    String  json = gson.toJson(storageListHelper);
    editor.putString(KEY_STORAGE_LIST, json);
    editor.commit();
  }


  public CaptionStyleCompat getCaptionStyleCompat() {
    int textColor = settingsPreferences.getInt(SettingsFragment.KEY_SUBTITLE_TEXTCOLOR, Color.BLACK);
    int bgColor = settingsPreferences.getInt(SettingsFragment.KEY_SUBTITLE_BGCOLOR, Color.WHITE);
    int windowColor = settingsPreferences.getInt(SettingsFragment.KEY_SUBTITLE_WINDOWCOLOR, Color.TRANSPARENT);
    int edgeColor = settingsPreferences.getInt(SettingsFragment.KEY_SUBTITLE_EDGECOLOR, Color.WHITE);
    String edgeTypeString = settingsPreferences.getString(SettingsFragment.KEY_SUBTITLE_EDGETYPE, "None");

    int edgeType = CaptionStyleCompat.EDGE_TYPE_NONE;
    if (edgeTypeString.equals("None")) {
      edgeType = CaptionStyleCompat.EDGE_TYPE_NONE;
    } else if(edgeTypeString.equals("OutLine")) {
      edgeType = CaptionStyleCompat.EDGE_TYPE_OUTLINE;
    } else if(edgeTypeString.equals("DropShadow")) {
      edgeType = CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW;
    } else if(edgeTypeString.equals("Raised")) {
      edgeType = CaptionStyleCompat.EDGE_TYPE_RAISED;
    } else if(edgeTypeString.equals("Deperssed")) {
      edgeType = CaptionStyleCompat.EDGE_TYPE_DEPRESSED;
    }
    return       new CaptionStyleCompat(
            textColor,
            bgColor,
            windowColor,
            edgeType,
            edgeColor,
            /* typeface= */ null);
  }


  public void saveLastPosition(String currentMediaKey, int lastWindow, long lastPosition) {
    editor.putInt(KEY_LAST_WINDOW_INDEX + currentMediaKey , lastWindow);
    editor.putLong(KEY_LAST_POSTION + currentMediaKey , lastPosition);
    editor.commit();
  }
  public Pair<Integer, Long> loadLastPosition(String currentMediaKey) {
    int lastWindow = preferences.getInt(KEY_LAST_WINDOW_INDEX + currentMediaKey, -1);
    long lastPosition = preferences.getLong(KEY_LAST_POSTION + currentMediaKey, -1);
    if (lastWindow < 0 | lastPosition < 0) {
      return null;
    }
    return new Pair<Integer, Long>(lastWindow, lastPosition);
  }
  public void removeLastPosition(String currentMediaKey) {
    editor.remove(KEY_LAST_WINDOW_INDEX + currentMediaKey );
    editor.remove(KEY_LAST_POSTION + currentMediaKey);
    editor.commit();
  }
  public void saveContentDuration(String currentMediaKey, long duration) {
    editor.putLong(KEY_CONTENT_DURATION + currentMediaKey , duration);
    editor.commit();
  }

  public long loadContentDuration(String currentMediaKey) {
    return preferences.getLong(KEY_CONTENT_DURATION + currentMediaKey, C.TIME_UNSET);
  }

  public boolean isContinuationPlayback() {
    return settingsPreferences.getBoolean(SettingsFragment.KEY_PLAYER_CONTINUATION, true);
  }
  public boolean isNextFileAutoPlay() {
    return settingsPreferences.getBoolean(SettingsFragment.KEY_PLAYER_NEXTFILE_AUTOPLAY, true);
  }

  public boolean isKeepLastPlaybackSpeed() {
    return settingsPreferences.getBoolean(SettingsFragment.KEY_PLAYER_KEEP_LAST_PLAYBACKSPEED, true);
  }

  public boolean isUseFfmpeg() {
    return settingsPreferences.getBoolean(SettingsFragment.KEY_PLAYER_USE_FFMPEG_PROGRESS, true);
  }

  public void saveLastPlaybackSpeed(float speed) {
    editor.putFloat(KEY_PLAYBACK_SPEED, speed);
    editor.commit();
  }

  public float loadLastPlaybackSpeed() {
    return preferences.getFloat(KEY_PLAYBACK_SPEED, 1.0f);
  }

}
