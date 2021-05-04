package me.sjva.sosoplayer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.exoplayer2.ext.plex.Video;

import java.util.ArrayList;
import java.util.Collections;

import me.sjva.sosoplayer.R;
import me.sjva.sosoplayer.data.FileInfo;
import me.sjva.sosoplayer.data.StorageInfo;
import me.sjva.sosoplayer.data.StorageType;
import me.sjva.sosoplayer.fragment.MediaListCommonFragment;
import me.sjva.sosoplayer.fragment.StorageListFragment;
import me.sjva.sosoplayer.item.EntryItem;
import me.sjva.sosoplayer.util.SharedPreferencesUtil;
import me.sjva.sosoplayer.util.Util;
import me.sjva.sosoplayer.widget.ConfirmDialog;

public class MainActivity extends AppCompatActivity implements OnMainEventListener {
  private static final String TAG = "MainActivity";
  public static final String KEY_STORAGE_INFO = "storageInfo";

  private static final int PLAYER_ACTIVITY_REQUSET_CODE = 0;
  private static final int MSG_ERROR = 0;
  private static final int MSG_INFO = 1;


  private StorageListFragment storgeListFragment;
  private MediaListCommonFragment mediaListFragment;
  private DrawerLayout drawerLayout;
  private ActionBarDrawerToggle drawerToggle;

  private EntryItem mSelectedDrawerItem;
  private EntryItem mModifyingDrawerItem;
  private boolean isFragmnetChanged;

  private StorageInfo storageInfo;
  private SharedPreferencesUtil sharedPreferencesUtil;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    sharedPreferencesUtil = SharedPreferencesUtil.getInstance(this);
    if (savedInstanceState != null) {
      storageInfo  = savedInstanceState.getParcelable(KEY_STORAGE_INFO);
      if (mediaListFragment != null) {
        mediaListFragment.set(storageInfo, this);
      }
      if (storgeListFragment != null) {
        storgeListFragment.set(storageInfo, this);
      }
    } else {
      storageInfo = sharedPreferencesUtil.getLastStorage();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    isFragmnetChanged = false;

    setupDrawerLayout();
    setupMediaListFragment(storageInfo, false);
    setUpStorgeListFragment(storageInfo, false);
  }


  @Override
  public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putParcelable(KEY_STORAGE_INFO, storageInfo);
  }


  private void setupMediaListFragment( StorageInfo storageInfo, boolean force) {
    if (force || mediaListFragment == null) {
      mediaListFragment = new MediaListCommonFragment();
      mediaListFragment.set(storageInfo, this);
    }

    try {
      getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, mediaListFragment).commit();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void setUpStorgeListFragment(StorageInfo storageInfo, boolean force) {
    if (force || storgeListFragment == null) {
      storgeListFragment = new StorageListFragment();
      storgeListFragment.set(storageInfo, this);
    }

    try {
      getSupportFragmentManager().beginTransaction().replace(R.id.storagelist_frame, storgeListFragment).commit();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void setupDrawerLayout() {
    drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
    if (drawerLayout != null) {
      drawerLayout.setStatusBarBackground(R.color.media_item_drm);
      drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
      drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
      drawerLayout.setDrawerListener(drawerToggle);
    }
  }

  private Handler handler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      switch (msg.what){
        case MSG_ERROR:
          break;
        case MSG_INFO:
          showToast((String)msg.obj);
          break;
      }
    }
  };


  private void showToast(String message) {
    Util.showToast(this, message);
  }

  public void setToolbar(Toolbar toolbar, String title, boolean homeUpEnable) {
    setSupportActionBar(toolbar);
    ActionBar actionBar = getSupportActionBar();
    actionBar.setTitle(title);
    actionBar.setDisplayShowCustomEnabled(true);
    actionBar.setDisplayHomeAsUpEnabled(homeUpEnable);
    supportInvalidateOptionsMenu();
  }


  private void showExitPopup() {
    new ConfirmDialog(this, getString(R.string.confirm_exit),
        getString(R.string.confirm_exit_message),
        new ConfirmDialog.OnComfirmDialogEventListener(){
          @Override
          public void onComfirm(boolean comfirm) {
            if (comfirm) {
              finish();
            }
          }
        }).show();
  }

  @Override
  public void onBackPressed() {
    try {
      if (mediaListFragment != null) {
        if(mediaListFragment.onBackPressed()) {
        //  super.onBackPressed();
          showExitPopup();
        }
      } else {
        super.onBackPressed();
      }
    }catch (Exception e) {
      Log.e(TAG, e.toString());
      super.onBackPressed();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    if (mediaListFragment != null) {
      MenuInflater inflater=getMenuInflater();
      mediaListFragment.createOptionsMenu(menu, inflater);
    }
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (mediaListFragment != null) {
      return mediaListFragment.optionsItemSelected(item);
    }
    return true;
  }

  public void updateStorage(StorageInfo storageInfo) {
    this.storageInfo = storageInfo;
    setupMediaListFragment(storageInfo, true);
    setUpStorgeListFragment(storageInfo, true);

    sharedPreferencesUtil.setLastStorage(storageInfo);
  }

  public void selectItem(EntryItem selectedItem) {
    this.storageInfo = selectedItem.info;
    setupMediaListFragment(storageInfo, true);
    drawerLayout.closeDrawer(GravityCompat.START );

    sharedPreferencesUtil.setLastStorage(storageInfo);
  }

  @Override
  public void onAddStorage(StorageInfo storageInfo) {
    if (sharedPreferencesUtil.haveSameStorageInfo(storageInfo)) {
      handler.sendMessage(handler.obtainMessage(MSG_INFO, "Error Same StreamInfo already Saved"));
    } else {
      sharedPreferencesUtil.addStorageInfo(storageInfo);
      updateStorage(storageInfo);
    }
  }

  @Override
  public void onModifyStorage(StorageInfo orgStorageInfo, StorageInfo newStorageInfo) {
    sharedPreferencesUtil.modifyStorageInfo(orgStorageInfo, newStorageInfo);
    updateStorage(newStorageInfo);
  }

  @Override
  public void onRemoveStorage(StorageInfo storageInfo) {
    sharedPreferencesUtil.removeStorageInfo(storageInfo);
    updateStorage(new StorageInfo(StorageType.MediaStore));
  }

  @Override
  public void onError(String message) {
    handler.sendMessage(handler.obtainMessage(MSG_INFO, message));
  }

  @Override
  public void onPlexError(Throwable t) {
    handler.sendMessage(handler.obtainMessage(MSG_INFO, "Error Plex list load fail"));
    updateStorage(new StorageInfo(StorageType.MediaStore));
  }

//  public void startPlayer(ArrayList<MediaItem> mediaItems) {
//    Intent intent = new Intent(this, PlayerActivity.class);
//    IntentUtil.addToIntent(mediaItems, intent);
//
//    startActivity(intent);
//
//  }


  public void startSettingActivity() {
    Intent intent = new Intent(this, SettingsActivity.class);
    startActivity(intent);
  }

  public void startPlayer(ArrayList<FileInfo> fileInfos, int postion, boolean playFromScratch) {
    Intent intent = new Intent(this, PlayerActivity.class);

    if (sharedPreferencesUtil.isNextFileAutoPlay()) {
      intent.putParcelableArrayListExtra(PlayerActivity.KEY_FILE_INFO_LIST, fileInfos);
      intent.putExtra(PlayerActivity.KEY_SELECTED_POSITION, postion);
    } else {
      ArrayList<FileInfo> newfileInfos = new ArrayList<FileInfo>();
      newfileInfos.add(fileInfos.get(0));
      intent.putParcelableArrayListExtra(PlayerActivity.KEY_FILE_INFO_LIST, newfileInfos);
      intent.putExtra(PlayerActivity.KEY_SELECTED_POSITION, 0);
    }

    intent.putExtra(PlayerActivity.KEY_STORAGE_INFO, storageInfo);
    intent.putExtra(PlayerActivity.KEY_FILE_INFO_TYPE, PlayerActivity.KEY_FILE_INFO_TYPE_COMMON);
    intent.putExtra(PlayerActivity.KEY_PLAY_FROM_SCRATCH, playFromScratch);
    startActivityForResult(intent, PLAYER_ACTIVITY_REQUSET_CODE);

  }

  public void startPlexPlayer(ArrayList<Video> fileInfos, int postion, boolean playFromScratch) {
    Intent intent = new Intent(this, PlayerActivity.class);

    if (sharedPreferencesUtil.isNextFileAutoPlay()) {
      intent.putParcelableArrayListExtra(PlayerActivity.KEY_PLEXFILE_INFO_LIST, fileInfos);
      intent.putExtra(PlayerActivity.KEY_SELECTED_POSITION, postion);
    } else {
      ArrayList<Video>  newfileInfos = new ArrayList<Video> ();
      newfileInfos.add(fileInfos.get(0));
      intent.putParcelableArrayListExtra(PlayerActivity.KEY_PLEXFILE_INFO_LIST, newfileInfos);
      intent.putExtra(PlayerActivity.KEY_SELECTED_POSITION, 0);
    }

    intent.putExtra(PlayerActivity.KEY_STORAGE_INFO, storageInfo);
    intent.putExtra(PlayerActivity.KEY_FILE_INFO_TYPE, PlayerActivity.KEY_FILE_INFO_TYPE_PLEX);
    intent.putExtra(PlayerActivity.KEY_PLAY_FROM_SCRATCH, playFromScratch);
    startActivityForResult(intent, PLAYER_ACTIVITY_REQUSET_CODE);
  }

  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == PLAYER_ACTIVITY_REQUSET_CODE) {
      if (mediaListFragment != null) {
        mediaListFragment.updateView();
      }
    }
  }

  public void playFromScratch(Parcelable parcelable) {
    if (parcelable instanceof FileInfo) {
      FileInfo fileInfo = (FileInfo)parcelable;
      ArrayList<FileInfo> fileInfos = new ArrayList<FileInfo>();
      fileInfos.add(fileInfo);
      startPlayer(fileInfos, 0, true);
    } else if (parcelable instanceof Video) {
      Video video = (Video)parcelable;
      ArrayList<Video> fileInfos = new ArrayList<Video>();
      fileInfos.add(video);
      startPlexPlayer(fileInfos, 0, true);
    }
  }
}

