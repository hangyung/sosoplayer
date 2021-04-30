package me.sjva.sosoplayer.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import me.sjva.sosoplayer.R;

import java.util.ArrayList;
import java.util.List;

import me.sjva.sosoplayer.data.StorageInfo;
import me.sjva.sosoplayer.util.PlexUtil;
import me.sjva.sosoplayer.util.SharedPreferencesUtil;
import me.sjva.sosoplayer.util.Util;
import me.sjva.sosoplayer.fragment.OnCommonEventListener;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ext.plex.PlexApi;
import com.google.android.exoplayer2.ext.plex.Video;

public class PlexEpisodeListAdapter extends RecyclerView.Adapter<PlexEpisodeListAdapter.ViewHolder> {

  private Context mContext;
  private ArrayList<Video> mVideo;
  private final String path;
  private final String token;
  private final StorageInfo storageInfo;
  private OnCommonEventListener mOnCommonEventListener;
  private SharedPreferencesUtil sharedPreferencesUtil;
  public PlexEpisodeListAdapter(Context context, StorageInfo storageInfo, String path , String token, ArrayList<Video> videos, OnCommonEventListener listener) {
    mContext = context;
    this.path = path;
    this.token = token;
    this.storageInfo = storageInfo;
    mVideo = videos;
    mOnCommonEventListener = listener;
    sharedPreferencesUtil = SharedPreferencesUtil.getInstance(context);
  }

  @NonNull
  @Override
  public PlexEpisodeListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View convertView = LayoutInflater.from(mContext).inflate(R.layout.plex_media_list_item, parent, false);
    int spanCount = mContext.getResources().getConfiguration().screenWidthDp / Util.SPAN_MIN_WIDTH;

    int width = parent.getMeasuredWidth() / spanCount;
    int height = width / 16 * 9;

    return new ViewHolder(convertView, height);
  }

  @Override
  public void onBindViewHolder(@NonNull PlexEpisodeListAdapter.ViewHolder holder, int position) {
    Video video = mVideo.get(position);
    String thumb =  video.getThumb();
    String imagePath = PlexApi.getContentPath(path, token, thumb);

    String year = video.getYear();
    if (!TextUtils.isEmpty(year)) {
      holder.bottom.setVisibility(View.VISIBLE);
      holder.bottom.setText("Year : " + year, TextView.BufferType.SPANNABLE);
    } else {
      holder.bottom.setVisibility(View.GONE);
    }

    Glide.with(mContext)
        .load(imagePath)
        .error(R.drawable.outline_music_video_black_24)
        .into(holder.thumbnail);

    holder.top.setText(video.getTitle());
    String durationString = video.getDuration();
    if (!TextUtils.isEmpty(durationString) ) {
      long duration = Long.parseLong(durationString);
      long currentPostion = C.TIME_UNSET;
      String mediaKey = PlexUtil.getCurrentMediaKey(storageInfo,  video);
      if (!TextUtils.isEmpty(mediaKey) ) {
        Pair<Integer, Long> pair =  sharedPreferencesUtil.loadLastPosition(mediaKey);
        if (pair != null &&  pair.second > 0) {
          currentPostion = pair.second;
          int level = (int) (pair.second * 10000 / duration);
          holder.progressView.getBackground().setLevel(level);
//
//          if (currentPostion > 0) {
//            String info = "<font color='#00FF00'>" + Util.makeDurationMessage(currentPostion) + "</font>";
//            holder.bottom.setText(Html.fromHtml(info));
//          }
        }
      }
      holder.durationTextView.setText(Util.makeDurationMessage(duration));
      holder.durationTextView.setVisibility(View.VISIBLE);
    } else {
      holder.durationTextView.setVisibility(View.GONE);
    }

    holder.top.setTextColor(Color.BLACK);
    holder.bottom.setTextColor(Color.BLACK);


  }

  @Override
  public int getItemCount() {
    return mVideo.size();
  }


  public class ViewHolder extends RecyclerView.ViewHolder {

//    private ImageView img_thumb;
//    private TextView txt_title;

    TextView top;
    TextView bottom;
    ImageView thumbnail;
    TextView durationTextView;

    FrameLayout thumbnailLayout;
    View progressView;

    public ViewHolder(@NonNull View itemView, int height) {
      super(itemView);
      top = (TextView)itemView.findViewById(R.id.toptext);
      bottom = (TextView)itemView.findViewById(R.id.bottomtext);
      thumbnail = (ImageView)itemView.findViewById(R.id.icon);
      durationTextView = (TextView)itemView.findViewById(R.id.duration);
      thumbnailLayout = (FrameLayout)itemView
          .findViewById(R.id.media_list_item_thumbnail_fLayout);
      progressView = itemView.findViewById(R.id.progress);
      ViewGroup.LayoutParams params = itemView.getLayoutParams();
      params.height = height;

      itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          int position = getAdapterPosition();
          if (position == RecyclerView.NO_POSITION)
            return;
       //   Video video = mVideo.get(position);
          mOnCommonEventListener.onPlexItemSelect(mVideo, position);
        }
      });
      itemView.setOnLongClickListener(new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
          int position = getAdapterPosition();
          if (position == RecyclerView.NO_POSITION)
            return true;

          mOnCommonEventListener.onPlexItemLongSelect(mVideo, position);
          return true;
        }
      });
    }
  }

}
