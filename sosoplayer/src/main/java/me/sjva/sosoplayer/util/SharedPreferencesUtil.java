package me.sjva.sosoplayer.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import com.google.android.exoplayer2.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import me.sjva.sosoplayer.data.StorageInfo;
import com.google.gson.Gson;
public class SharedPreferencesUtil {
  private static final String SETTING_FILE_NAME = "soso";
  private static final String KEY_STORAGE_LIST = "KEY_STORAGE_LIST";

  private static SharedPreferencesUtil instance;
  public static SharedPreferencesUtil getInstance(Context context){
    if (instance == null) {
      instance = new SharedPreferencesUtil(context);
    }
    return instance;
  }

  private SharedPreferences preferences;
  private SharedPreferences.Editor editor;
  private SharedPreferencesUtil(Context context){
    preferences = context.getSharedPreferences(SETTING_FILE_NAME, Activity.MODE_PRIVATE);
    editor = preferences.edit();
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


}
