package me.sjva.sosoplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import me.sjva.sosoplayer.R;
import java.util.List;

import me.sjva.sosoplayer.data.StorageInfo;
import me.sjva.sosoplayer.util.Util;
import me.sjva.sosoplayer.fragment.OnCommonEventListener;
import me.sjva.sosoplayer.widget.RecyclerViewEmptySupport;
import com.google.android.exoplayer2.ext.plex.Directory;
import com.google.android.exoplayer2.ext.plex.MediaContainer;
import com.google.android.exoplayer2.ext.plex.PlexApi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlexShowDetailsListAdapter extends RecyclerView.Adapter<PlexShowDetailsListAdapter.ViewHolder>  {

  private Context mContext;
  private final List<Directory> mDirectories;
  private final String path;
  private final String token;
  private OnCommonEventListener mOnCommonEventListener;
  private final StorageInfo storageInfo;
  public PlexShowDetailsListAdapter(Context context, StorageInfo storageInfo, String path , String token, List<Directory> directories, OnCommonEventListener listener) {
    mContext = context;
    this.path = path;
    this.token = token;
    mDirectories = directories;
    mOnCommonEventListener = listener;
    this.storageInfo = storageInfo;
  }

  @NonNull
  @Override
  public PlexShowDetailsListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View convertView = LayoutInflater.from(mContext).inflate(R.layout.plex_showdetails_list_item, parent, false);
    int spanCount = mContext.getResources().getConfiguration().screenWidthDp / Util.SPAN_MIN_WIDTH;

    int width = parent.getMeasuredWidth() / spanCount;
    int height = width / 2 * 3;

    return new ViewHolder(convertView, height);
  }

  @Override
  public void onBindViewHolder(@NonNull final  PlexShowDetailsListAdapter.ViewHolder holder, int position) {
    Directory directory = mDirectories.get(position);

    holder.txt_title.setText(directory.getTitle());
    mOnCommonEventListener.onLoadingStart();
    PlexApi.getDetails(path,   token, directory.getKey(), new Callback<MediaContainer>() {

      @Override
      public void onResponse(Call<MediaContainer> call, Response<MediaContainer> response) {
        MediaContainer mediaContainer = response.body();

        PlexEpisodeListAdapter plexEpisodeListAdapter = new PlexEpisodeListAdapter(mContext,storageInfo,  path, token, mediaContainer.getVideos(),
                mOnCommonEventListener);
        holder.recyclerView.setAdapter(plexEpisodeListAdapter);
        mOnCommonEventListener.onLoadingEnd();
      }

      @Override
      public void onFailure(Call<MediaContainer> call, Throwable t) {
        mOnCommonEventListener.onLoadingEnd();
        mOnCommonEventListener.onError(t);
      }
    });
  }

  @Override
  public int getItemCount() {
    return mDirectories.size();
  }



  public class ViewHolder extends RecyclerView.ViewHolder {
    RecyclerView.LayoutManager recyclerViewLayoutManager;
    private TextView txt_title;
    private RecyclerViewEmptySupport recyclerView;
    public ViewHolder(@NonNull View itemView, int height) {
      super(itemView);

      txt_title = (TextView)itemView.findViewById(R.id.season_title_textview);
      int width = mContext.getResources().getConfiguration().screenWidthDp;
      int spanCount = width / Util.SPAN_MIN_WIDTH;

      recyclerView =
          (RecyclerViewEmptySupport)itemView.findViewById(R.id.recyclerview_season);
      recyclerViewLayoutManager = new LinearLayoutManager(mContext);

      recyclerView.setLayoutManager(recyclerViewLayoutManager);
      recyclerView.setEmptyView(itemView.findViewById(R.id.list_empty));
    }
  }

}
