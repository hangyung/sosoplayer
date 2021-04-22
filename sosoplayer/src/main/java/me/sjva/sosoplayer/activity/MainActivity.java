package me.sjva.sosoplayer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ProgressBar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.exoplayer2.MediaItem;
import java.util.ArrayList;
import me.sjva.sosoplayer.R;
import me.sjva.sosoplayer.data.StorageInfo;
import me.sjva.sosoplayer.data.StorageType;
import me.sjva.sosoplayer.fragment.MediaListOnFragment;
import me.sjva.sosoplayer.fragment.StorageListFragment;
import me.sjva.sosoplayer.item.EntryItem;
import me.sjva.sosoplayer.util.IntentUtil;
import me.sjva.sosoplayer.util.SharedPreferencesUtil;
import me.sjva.sosoplayer.util.Util;
import me.sjva.sosoplayer.widget.ConfirmDialog;

public class MainActivity extends AppCompatActivity implements OnMainEventListener {
  private static final String TAG = "MainActivity";
  private static final int MSG_ERROR = 0;
  private static final int MSG_INFO = 1;


  private StorageListFragment storgeListFragment;
  private MediaListOnFragment mediaListFragment;
  private DrawerLayout drawerLayout;
  private ActionBarDrawerToggle drawerToggle;

  private EntryItem mSelectedDrawerItem;
  private EntryItem mModifyingDrawerItem;
  private boolean isFragmnetChanged;
  private ProgressBar progressBar;

  private StorageInfo storageInfo;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    storageInfo = new StorageInfo(StorageType.MediaStore);
  }

  @Override
  protected void onResume() {
    super.onResume();
    isFragmnetChanged = false;

    setupDrawerLayout();
    setupMediaListFragment(storageInfo, false);
    setUpStorgeListFragment(storageInfo, false);
  }

  private void setupMediaListFragment( StorageInfo storageInfo, boolean force) {
    if (force || mediaListFragment == null) {
      mediaListFragment = new MediaListOnFragment(storageInfo, this);
    }

    try {
      getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, mediaListFragment).commit();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void setUpStorgeListFragment(StorageInfo storageInfo, boolean force) {
    if (force || storgeListFragment == null) {
      storgeListFragment = new StorageListFragment(storageInfo, this);
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
  }

  public void selectItem(EntryItem selectedItem) {
    this.storageInfo = selectedItem.info;
    setupMediaListFragment(storageInfo, true);

    drawerLayout.closeDrawer(GravityCompat.START );
  }

  @Override
  public void onAddStorage(StorageInfo storageInfo) {
    SharedPreferencesUtil sharedPreferencesUtil = SharedPreferencesUtil.getInstance(this);
    if (sharedPreferencesUtil.haveSameStorageInfo(storageInfo)) {
      handler.sendMessage(handler.obtainMessage(MSG_INFO, "Error Same StreamInfo already Saved"));
    } else {
      sharedPreferencesUtil.addStorageInfo(storageInfo);
      updateStorage(storageInfo);
    }
  }

  @Override
  public void onModifyStorage(StorageInfo orgStorageInfo, StorageInfo newStorageInfo) {
    SharedPreferencesUtil sharedPreferencesUtil = SharedPreferencesUtil.getInstance(this);
    sharedPreferencesUtil.modifyStorageInfo(orgStorageInfo, newStorageInfo);
    updateStorage(newStorageInfo);
  }

  @Override
  public void onRemoveStorage(StorageInfo storageInfo) {
    SharedPreferencesUtil sharedPreferencesUtil = SharedPreferencesUtil.getInstance(this);
    sharedPreferencesUtil.removeStorageInfo(storageInfo);
    updateStorage(new StorageInfo(StorageType.MediaStore));
  }

  @Override
  public void onError(String message) {
    //Util.showToast(this, message);
    handler.sendMessage(handler.obtainMessage(MSG_INFO, message));
  }

  @Override
  public void onPlexError(Throwable t) {
    handler.sendMessage(handler.obtainMessage(MSG_INFO, "Error Plex list load fail"));
    updateStorage(new StorageInfo(StorageType.MediaStore));
  }

  public void startPlayer(ArrayList<MediaItem> mediaItems) {
    Intent intent = new Intent(this, PlayerActivity.class);
    IntentUtil.addToIntent(mediaItems, intent);
//    ExtraStreamInfo extraStreamInfo = storageInfo.getExtraStreamInfo();
//    if (extraStreamInfo != null) {
//      intent.putExtra(PlayerActivity.EXTRA_STREAM_INFO, extraStreamInfo);
//    }

    this.storageInfo = storageInfo;
    startActivity(intent);

  //  finish();
  }


}

