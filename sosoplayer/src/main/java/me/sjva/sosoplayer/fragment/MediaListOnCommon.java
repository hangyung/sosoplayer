package me.sjva.sosoplayer.fragment;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.exoplayer2.ext.extrastream.FtpFile;
import com.google.android.exoplayer2.ext.extrastream.WebdavFile;
import me.sjva.sosoplayer.R;
import com.google.android.exoplayer2.ext.extrastream.ExtraStreamFileInfo;
import com.google.android.exoplayer2.ext.extrastream.SambaFile;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import me.sjva.sosoplayer.activity.MainActivity;
import me.sjva.sosoplayer.adapter.PlexMovieListAdapter;
import me.sjva.sosoplayer.adapter.PlexSectionListAdapter;
import me.sjva.sosoplayer.adapter.PlexShowDetailsListAdapter;
import me.sjva.sosoplayer.adapter.PlexShowListAdapter;
import me.sjva.sosoplayer.data.ExtraPlexStorageInfo;
import com.google.android.exoplayer2.ext.plex.MediaContainer;
import com.google.android.exoplayer2.ext.plex.Video;
import me.sjva.sosoplayer.util.PlexUtil;
import me.sjva.sosoplayer.util.Util;
import me.sjva.sosoplayer.adapter.MediaDefaultAdapter;
import me.sjva.sosoplayer.adapter.MediaStoreChildAdapter;
import me.sjva.sosoplayer.adapter.MediaStoreParentAdapter;
import me.sjva.sosoplayer.data.FileInfo;
import me.sjva.sosoplayer.data.StorageInfo;

import me.sjva.sosoplayer.data.StorageType;
import me.sjva.sosoplayer.ui.RecyclerViewEmptySupport;
import me.sjva.sosoplayer.widget.StorageManagerDialog;
import com.google.android.exoplayer2.ext.plex.Directory;
import me.sjva.sosoplayer.activity.OnMainEventListener;

    public class MediaListOnCommon extends Fragment implements OnCommonEventListener,
     Toolbar.OnMenuItemClickListener,
            ExtraPlexStorageInfo.OnPlexLoadEventListener {
  private static final String TAG = "MediaListFragment";
  private static final  int MSG_LOADING_START = 0;
  private static final  int MSG_LOADING_END = 1;
  private TextView noMediaTextView;
  private RecyclerViewEmptySupport recyclerView;
  private Toolbar toolbar;
  private StorageInfo storageInfo;

  private ProgressBar progressBar;
  private String rootDir;
  private String subDir;

  private OnMainEventListener onMainEventListener;

  public MediaListOnCommon(StorageInfo storageInfo, OnMainEventListener onMainEventListener) {
    rootDir = storageInfo.getPath();
    subDir = null;
    this.storageInfo = storageInfo;
    this.onMainEventListener = onMainEventListener;
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_media_list,
        null);

    if (root != null) {
      toolbar = (Toolbar)root.findViewById(R.id.main_toolbar);
      noMediaTextView = (TextView)root.findViewById(R.id.list_empty);
      progressBar = (ProgressBar) root.findViewById(R.id.main_progressbar);
      progressBar.setVisibility(View.GONE);
      recyclerView = (RecyclerViewEmptySupport)root.findViewById(R.id.recyclerview_medialist);
      recyclerView.setEmptyView(noMediaTextView);

      if (getActivity() != null && getActivity() instanceof MainActivity) {
        MainActivity mainActivity = (MainActivity)getActivity();
        mainActivity.setToolbar(toolbar, getString(R.string.application_name), true);
        toolbar.setTitle(getString(R.string.application_name));
      }
    }

    return root;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    if (getActivity() != null && getActivity() instanceof MainActivity) {
      final MainActivity activity = (MainActivity)getActivity();
      getActivity().runOnUiThread(new Runnable() {
        @Override
        public void run() {
          activity.supportInvalidateOptionsMenu();
        }
      });
    }
    updateView();

    super.onActivityCreated(savedInstanceState);
  }

  private void loadMediaList() {
    if (getActivity() == null)
      return;
    rootDir = storageInfo.getPath();

    if (subDir == null) {
      subDir = rootDir;
    }

    if (subDir != null) {
      switch (storageInfo.getStorageType()){
        case MediaStore:
          LoadMediaStoreFileInfo();
          break;
        case Mount:
          loadLocalFileInfo();
          break;
        case Samba:
          loadSambaFileInfo();
          break;
        case Ftp:
          loadFtpFileInfo();
          break;
        case WebDav:
          loadWebDavFileInfo();
          break;
        case Plex:
          loadPlexFileInfo();
          break;
      }
    }
  }

  @Override
  public void onResume() {
    super.onResume();
  }


  @Override
  public void onSectionSelect(Directory directory) {
    onLoadingStart();
    storageInfo.selectSection(directory.getTitle(), directory.getKey());
    storageInfo.load(this);
  }

  @Override
  public void onContentSelect(Directory directory) {
    onLoadingStart();
    storageInfo.setContent(directory.getTitle(), directory.getKey());
    storageInfo.load(this);
  }


  private void startPlayer(ArrayList<FileInfo> fileInfos, int postion) {
    if (getActivity() != null && getActivity() instanceof MainActivity) {
      final MainActivity activity = (MainActivity) getActivity();
      activity.startPlayer(fileInfos, postion);
//      activity.startPlayer(fileInfo.getMediaItems(storageInfo));
    }
  }

  @Override
  public void onPlexItemSelect(ArrayList<Video> videos,  int postion) {
    if (getActivity() != null && getActivity() instanceof MainActivity) {
      final MainActivity activity = (MainActivity) getActivity();
      activity.startPlexPlayer(videos, postion);
    //  PlexUtil.startPlayer((MainActivity)getActivity(),storageInfo, video, this);
    }
  }


  @Override
  public void onPlexItemLongSelect(ArrayList<Video> videos,  int postion) {
    // TODO:
  }


    private Handler handler = new Handler() {
      @Override
      public void handleMessage(Message msg) {
        switch (msg.what){
          case MSG_LOADING_START:
            progressBar.setVisibility(View.VISIBLE);
            break;
          case MSG_LOADING_END:
            progressBar.setVisibility(View.GONE);
            break;
        }
      }
    };

  @Override
  public void onLoadingStart() {
    handler.sendEmptyMessage(MSG_LOADING_START);
  }

  @Override
  public void onLoadingEnd() {
    handler.sendEmptyMessage(MSG_LOADING_END);
  }


  private boolean LoadMediaStoreFileInfo() {
    if(getActivity() == null) {
      return false;
    }

    String[] projection = new String[] {
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.DISPLAY_NAME,
        MediaStore.Video.Media.DURATION,
        MediaStore.Video.Media.SIZE,
        MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
    };
    String selection = MediaStore.Video.Media.DURATION +
        " >= ?";
    String[] selectionArgs = new String[] {
        String.valueOf(TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES))
    };
    String sortOrder = MediaStore.Video.Media.DISPLAY_NAME + " ASC";

    storageInfo.clear();

    TreeMap<String, ArrayList<FileInfo>> bucketMap = storageInfo.getBucketMap();
    try (Cursor cursor = getActivity().getContentResolver().query(
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        sortOrder
    )) {
      // Cache column indices.
      int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
      int nameColumn =
          cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
      int durationColumn =
          cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
      int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
      int bucketDisplayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);
      while (cursor.moveToNext()) {
        // Get values of columns for a given video.
        long id = cursor.getLong(idColumn);
        String name = cursor.getString(nameColumn);
        int duration = cursor.getInt(durationColumn);
        int size = cursor.getInt(sizeColumn);
        String bucketDisplayName = cursor.getString(bucketDisplayNameColumn);
        Uri contentUri = ContentUris.withAppendedId(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);

        FileInfo.Builder builder = new FileInfo.Builder(StorageType.MediaStore);
        builder.setId(Long.toString(id));
        builder.setName(name);
        builder.setDuration(duration);
        builder.setSize(size);
        builder.setPath(contentUri.toString());
        builder.setBucket(bucketDisplayName);

        FileInfo fileInfo = builder.build();
        if(bucketMap.containsKey(bucketDisplayName)) {
          ArrayList<FileInfo> fileInfos = bucketMap.get(bucketDisplayName);
          fileInfos.add(fileInfo);
        } else {
          ArrayList<FileInfo> fileInfos = new ArrayList<FileInfo>();
          fileInfos.add(fileInfo);
          bucketMap.put(bucketDisplayName, fileInfos);
        }

      }
    }
    return true;
  }

  private void addSubtitle(FileInfo.Builder builder, File[] files, String orgName) {
    int pos = orgName.lastIndexOf( "." );
    String name = orgName;
    if (pos > 0) {
      name = orgName.substring(0, pos);
    }
    for (File curFile : files) {
      String curName = curFile.getName();
      if (curName.equals(orgName)) // same file
        continue;
      if(curName.startsWith(name)) {
        builder.addSubtitle(curFile.getPath());
      }
    }
  }

  private  boolean loadLocalFileInfo() {
    if(getActivity() == null) {
      return false;
    }
    storageInfo.clear();
    File file = new File(subDir);
    File[] files = file.listFiles();
    for (File curFile : files) {
      String name = curFile.getName();
      if (curFile.isDirectory() ||
          Util.isMovie(getActivity(), name)) {
        FileInfo.Builder builder = new FileInfo.Builder(StorageType.Mount);
//      builder.setId(Long.toString(id));
        builder.setName(curFile.getName());
//      builder.setDuration(duration);
        builder.setSize(curFile.length());
        builder.setPath(curFile.getAbsolutePath());
        builder.setDirectory(curFile.isDirectory());

        if (!curFile.isDirectory() ) {
          addSubtitle(builder, files, name);
        }

        FileInfo fileInfo = builder.build();
        storageInfo.addFileInfos(fileInfo);
      }
    }
    storageInfo.sort();
    return true;
  }


  private void addExtraFileInfo(ArrayList<ExtraStreamFileInfo> list , StorageType storageType) {

    for (ExtraStreamFileInfo extraStreamFileInfo : list) {
      String name = extraStreamFileInfo.getName();
      if (extraStreamFileInfo.isDirectory() ||
          Util.isMovie(getActivity(), name)) {
        FileInfo.Builder builder = new FileInfo.Builder(storageType);
        builder.setName(name);
        builder.setSize(extraStreamFileInfo.getLength());
        if (storageType == StorageType.Ftp) {
          File curDir = new File(subDir);
          File childDir = new File(curDir,extraStreamFileInfo.getPath());

          builder.setPath(childDir.getPath());
        } else {
          builder.setPath(extraStreamFileInfo.getPath());
        }

        builder.setDirectory(extraStreamFileInfo.isDirectory());

        if (!extraStreamFileInfo.isDirectory() ) {
          addSubtitle(storageType, builder, list, name);
        }

        FileInfo fileInfo = builder.build();
        storageInfo.addFileInfos(fileInfo);
      }
    }
  }

  private void addSubtitle(StorageType storageType, FileInfo.Builder builder, ArrayList<ExtraStreamFileInfo> list, String orgName) {
    int pos = orgName.lastIndexOf( "." );
    String name = orgName;
    if (pos > 0) {
      name = orgName.substring(0, pos);
    }
    for (ExtraStreamFileInfo extraStreamFileInfo : list) {
      String curName = extraStreamFileInfo.getName();
      if (curName.equals(orgName)) // same file
        continue;

      if (curName.length() <= 4) {
        continue;
      }

      String ext = curName.substring(curName.length() - 4);
      if(curName.startsWith(name) && ext.startsWith(".") && Util.isSubtitleFile(curName)) {
        if (storageType != StorageType.Ftp) {
          builder.addSubtitle(extraStreamFileInfo.getPath());
        } else {
          File curDir = new File(subDir);
          File childDir = new File(curDir,curName);
          builder.addSubtitle(childDir.getPath());
        }
      }
    }
  }

  private boolean  loadSambaFileInfo() {
    if(getActivity() == null) {
      return false;
    }
    storageInfo.clear();
    try {
      SambaFile sambaFile = null;

      if (TextUtils.isEmpty(storageInfo.getPasswd())){
        sambaFile = new SambaFile(subDir);
      } else {
        sambaFile = new SambaFile(subDir, storageInfo.getId(), storageInfo.getPasswd());
      }

      ArrayList<ExtraStreamFileInfo> list = sambaFile.list();
      addExtraFileInfo(list, StorageType.Samba);
    } catch (IOException e) {
      e.printStackTrace();
      onMainEventListener.onError(e.toString());
    }
    storageInfo.sort();
    return true;
  }

  private boolean loadFtpFileInfo() {
    if(getActivity() == null) {
      return false;
    }
    storageInfo.clear();

    FtpFile ftpFile = null;
    try {
      ftpFile = new FtpFile(storageInfo.getId(), storageInfo.getPasswd(), storageInfo.getEncoding(),
          storageInfo.getPath(), storageInfo.getPort(), storageInfo.isActivieMode(), null);
      ftpFile.connect();
      ftpFile.setWorkingDirectory(rootDir, subDir);

      ArrayList<ExtraStreamFileInfo> list = ftpFile.list();
      addExtraFileInfo(list, StorageType.Ftp);
    } catch (IOException e) {
      e.printStackTrace();
      onMainEventListener.onError(e.toString());
    } finally {
      if (ftpFile != null) {
        ftpFile.disconnect();
      }
      ftpFile = null;
    }
    storageInfo.sort();
    return true;
  }

  private boolean loadWebDavFileInfo() {
    if(getActivity() == null) {
      return false;
    }
    storageInfo.clear();

    try {
      WebdavFile webdavFile = new WebdavFile(storageInfo.getId(), storageInfo.getPasswd());
      ArrayList<ExtraStreamFileInfo> list = webdavFile.list(rootDir, subDir);
      addExtraFileInfo(list, StorageType.WebDav);
      storageInfo.sort();
    } catch (Exception e) {
      e.printStackTrace();
      onMainEventListener.onError(e.toString());
    }

    return true;
  }


  private boolean loadPlexFileInfo() {
    if(getActivity() == null) {
      return false;
    }
    storageInfo.load(this);

    return true;
  }

  public void updateView() {
    ViewUpdateAsyncTask asyncTask = new ViewUpdateAsyncTask();
    asyncTask.execute();
  }

  @Override
  public boolean onMenuItemClick(MenuItem item) {
    switch(item.getItemId()) {
      case android.R.id.home :
        return onBackPressed();
      case R.id.main_menu_add_storage:
        break;
      case R.id.main_menu_settings:
        break;
    }
    return true;
  }

  @Override
  public void onItemSelect(String name) {
    storageInfo.setBucketFilter(name);
    setAdapter();
  }

  @Override
  public void onItemSelect(ArrayList<FileInfo> fileInfos, int postion) {
    FileInfo fileInfo = fileInfos.get(postion);
    switch (storageInfo.getStorageType()){
      case MediaStore:
        startPlayer(fileInfos, postion);
        break;
      case Mount: {
          if (fileInfo.isDirectory()) {
            File curDir = new File(subDir);
            File childDir = new File(curDir, fileInfo.getName());
            subDir = childDir.getPath();
            updateView();
          } else {
            startPlayer(fileInfos, postion);
          }
        }
        break;
      case Samba:{
          if (fileInfo.isDirectory()) {
            subDir = fileInfo.getPath();
            updateView();
          } else {
            startPlayer(fileInfos, postion);
          }
        }
        break;
      case Ftp: {
          if (fileInfo.isDirectory()) {
            File curDir = new File(subDir);
            File childDir = new File(curDir, fileInfo.getName());
            subDir = childDir.getPath();
            updateView();
          } else {
            startPlayer(fileInfos, postion);
          }
        }
        break;
      case WebDav: {
          if (fileInfo.isDirectory()) {
            subDir = fileInfo.getPath();
            updateView();
          } else {
            startPlayer(fileInfos, postion);
          }
        }
        break;
      case Plex:
        break;
    }
  }

  @Override
  public void onItemLongSelect(ArrayList<FileInfo> fileInfos, int postion) {
    // TODO:
  }


  @Override
  public void onSectionLoaded(MediaContainer sectionMediaContainer) {
    if (sectionMediaContainer == null)
      return;

    PlexSectionListAdapter adapter = new PlexSectionListAdapter(getActivity(), sectionMediaContainer.getDirectory(), this);
    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
    recyclerView.setLayoutManager(linearLayoutManager);
    recyclerView.setAdapter(adapter);

    updateToolbar();
  }

  @Override
  public void onContentsLoaded(MediaContainer contentMediaContainer) {
    String viewGroup = contentMediaContainer.getViewGroup();
    RecyclerView.Adapter adapter = null;
    if (viewGroup.equals("movie")) {
      adapter = new PlexMovieListAdapter(getActivity(), storageInfo.getPath(), storageInfo.getToken(),
          contentMediaContainer.getVideos(), this);

    } else if (viewGroup.equals("show")) {
      adapter = new PlexShowListAdapter(getActivity(), storageInfo.getPath(), storageInfo.getToken(),
          contentMediaContainer.getDirectory(), this);
    }

    if (adapter == null) {
      return;// Error
    }
    int width = getResources().getConfiguration().screenWidthDp;
    int spanCount = width / Util.SPAN_MIN_WIDTH;
    GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), spanCount);
    recyclerView.setLayoutManager(gridLayoutManager);
    recyclerView.setAdapter(adapter);

    updateToolbar();
  }

  @Override
  public void onContentsDetailsLoaded(MediaContainer detailsMediaContainer) {
    RecyclerView.Adapter  adapter = new PlexShowDetailsListAdapter(getActivity(), storageInfo,  storageInfo.getPath(), storageInfo.getToken(),
        detailsMediaContainer.getDirectory(), this);
    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
    recyclerView.setLayoutManager(linearLayoutManager);
    recyclerView.setAdapter(adapter);

    updateToolbar();
  }


  @Override
  public void onError(Throwable t) {
    onMainEventListener.onPlexError(t);
  }

  private class ViewUpdateAsyncTask extends AsyncTask<Void, Void, Void>  {
    @Override
    protected Void doInBackground(Void... params) {
      onLoadingStart();
      loadMediaList();
      return null;
    }

    @Override
    protected void onPostExecute(Void result) {
      if (storageInfo.getStorageType() != StorageType.Plex) {
        setAdapter();
      }

      super.onPostExecute(result);
    }

  }


  private void setAdapter() {
    switch (storageInfo.getStorageType()) {
      case MediaStore:
        setMediaStoreAdapter();
        updateToolbar();
        break;
      case Mount:
        setDefaultAdater();
        updateToolbar();
        break;
      case Samba:
        setDefaultAdater();
        updateToolbar();
        break;
      case Ftp:
        setDefaultAdater();
        updateToolbar();
        break;
      case WebDav:
        setDefaultAdater();
        updateToolbar();
        break;
    }
  }

  private void updateToolbar() {
    if (getActivity() != null && getActivity() instanceof MainActivity) {
      final MainActivity activity = (MainActivity) getActivity();
      switch (storageInfo.getStorageType()) {
        case MediaStore: {
          if (TextUtils.isEmpty(storageInfo.getBucketFilter()) ){
            activity.setToolbar(toolbar, storageInfo.getStorageType().toString(), false);
            toolbar.setTitle(storageInfo.getStorageType().toString());
          } else {
            activity.setToolbar(toolbar, storageInfo.getBucketFilter(), true);
            toolbar.setTitle(storageInfo.getBucketFilter());
          }
        }
        break;
      case Mount: {
          activity.setToolbar(toolbar, subDir, false);
          toolbar.setTitle(subDir);
        }
        break;
      case Samba: {
            String title = storageInfo.getName();
            if (!subDir.equals(rootDir)){
              int pos = subDir.lastIndexOf("/");
              String curSubDir = subDir.substring(0, pos);

              if (!curSubDir.equals(rootDir)) {
                title = curSubDir.replace(rootDir, "");
              }
            }

            activity.setToolbar(toolbar, title, false);
            toolbar.setTitle(title);
          }
          break;
      case Ftp: {
            String title = storageInfo.getName();
            if (!subDir.equals(rootDir)){
              title = subDir.replace(rootDir, "");
            }
            activity.setToolbar(toolbar, title, false);
            toolbar.setTitle(title);
          }
          break;
      case WebDav: {
            String title = storageInfo.getName();
            if (!subDir.equals(rootDir)){
              title = subDir.replace(rootDir, "");
            }
            activity.setToolbar(toolbar, title, false);
            toolbar.setTitle(title);
          }
        break;
      case Plex: {
          activity.setToolbar(toolbar, storageInfo.getTitle(), false);
          toolbar.setTitle(storageInfo.getTitle());
        }
        break;
      }
      onLoadingEnd();
    }
  }

  private void  setMediaStoreAdapter() {
    if (TextUtils.isEmpty(storageInfo.getBucketFilter())) {
      //     adapter = new MediaStoreParentAdapter(getActivity(), storageInfoMediaStore);
      MediaStoreParentAdapter adapter = new MediaStoreParentAdapter(getActivity(), storageInfo.getBucketMap(), this);
      int width = getResources().getConfiguration().screenWidthDp;
      int spanCount = width / Util.SPAN_MIN_WIDTH;
      GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), spanCount);
      recyclerView.setLayoutManager(gridLayoutManager);
      recyclerView.setAdapter(adapter);
    } else {
      MediaStoreChildAdapter adapter = new MediaStoreChildAdapter(getActivity(), storageInfo, storageInfo.getBucketList(), this);
      LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
      linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
      recyclerView.setLayoutManager(linearLayoutManager);
      recyclerView.setAdapter(adapter);
    }
  }

  private void setDefaultAdater() {
    MediaDefaultAdapter adapter = new MediaDefaultAdapter(getActivity(), storageInfo,  storageInfo.getFileInfos(), this);
    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
    linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
    recyclerView.setLayoutManager(linearLayoutManager);
    recyclerView.setAdapter(adapter);
  }
  
  public boolean onBackPressed() {
    switch (storageInfo.getStorageType()) {
      case MediaStore: {
          if(TextUtils.isEmpty(storageInfo.getBucketFilter())) {
            return true;
          } else {
            storageInfo.setBucketFilter(null);
            setAdapter();
            return false;
          }
        }
      case Mount: {
          if (subDir.equals(rootDir)){
            return true;
          } else {
            int pos = subDir.lastIndexOf("/");
            subDir = subDir.substring(0, pos);
            updateView();
            return false;
          }
        }
      case Samba: {
        int pos = subDir.lastIndexOf("/");
        String curSubDir = subDir.substring(0, pos);
        if (curSubDir.equals(rootDir)){
          return true;
        } else {
          pos = curSubDir.lastIndexOf("/");
          curSubDir = curSubDir.substring(0, pos);
          subDir = curSubDir + "/";
          updateView();
          return false;
        }
      }
      case Ftp: {
          if (subDir.equals(rootDir)){
            return true;
          } else {
            int pos = subDir.lastIndexOf("/");
            subDir = subDir.substring(0, pos);
            updateView();
            return false;
          }
        }
      case WebDav: {
          if (subDir.equals(rootDir)){
            return true;
          } else {
            int pos = subDir.lastIndexOf("/");
            subDir = subDir.substring(0, pos);
            updateView();
            return false;
          }
        }
      case Plex: {
          if(storageInfo.onBackPressed(this)) {
            return true;
          }
          return false;
        }
    }
    return false;
  }


  public void createOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    switch (storageInfo.getStorageType()) {
      default:
        inflater.inflate(R.menu.main_menu, menu);
    }
  }

  public boolean optionsItemSelected(MenuItem item) {
    switch(item.getItemId()) {
      case android.R.id.home :
        return onBackPressed();
      case R.id.main_menu_add_storage:
        new StorageManagerDialog(getActivity(), onMainEventListener).show();
        break;
      case R.id.main_menu_settings:
        startSettingActivity();
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  private void startSettingActivity() {
    if (getActivity() != null && getActivity() instanceof MainActivity) {
      MainActivity mainActivity = (MainActivity)getActivity();
      mainActivity.startSettingActivity();
    }
  }

}
