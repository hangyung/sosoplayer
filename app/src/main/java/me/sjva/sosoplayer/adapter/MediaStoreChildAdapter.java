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

import com.google.android.exoplayer2.C;

import me.sjva.sosoplayer.R;
import java.io.IOException;
import java.util.ArrayList;

import me.sjva.sosoplayer.data.StorageInfo;
import me.sjva.sosoplayer.fragment.OnCommonEventListener;
import me.sjva.sosoplayer.util.SharedPreferencesUtil;
import me.sjva.sosoplayer.util.Util;
import me.sjva.sosoplayer.data.FileInfo;

public class MediaStoreChildAdapter extends RecyclerView.Adapter<MediaStoreChildAdapter.ViewHolder> {
  private final Context context;

  private final ArrayList<FileInfo> fileInfos;
  private OnCommonEventListener listener;
  private StorageInfo storageInfo;
  private SharedPreferencesUtil sharedPreferencesUtil;
  public MediaStoreChildAdapter(Context context, StorageInfo storageInfo,  ArrayList<FileInfo> fileInfos, OnCommonEventListener listener){
    this.context = context;
    this.fileInfos = fileInfos;
    this.listener = listener;
    this.storageInfo = storageInfo;
    sharedPreferencesUtil = SharedPreferencesUtil.getInstance(context);
  }


  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View convertView = LayoutInflater.from(context).inflate(R.layout.media_list_item, parent, false);
    return new ViewHolder(convertView);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    FileInfo fileInfo = fileInfos.get(position);
    if (fileInfo != null) {
      holder.top.setText(fileInfo.getName());

      long currentPostion = C.TIME_UNSET;
      String mediaKey = fileInfo.getCurrentMediaKey();
      if (!TextUtils.isEmpty(mediaKey) ) {
        Pair<Integer, Long> pair =  sharedPreferencesUtil.loadLastPosition(mediaKey);
        if (pair != null) {
          currentPostion = pair.second;
          int level = (int) (pair.second * 10000 / fileInfo.getDuration());
          holder.progressView.getBackground().setLevel(level);
        }
      }

      if (fileInfo.isDirectory()) {
        holder.bottom.setVisibility(View.GONE);
      } else {
        String info = fileInfo.getInfo(currentPostion);
        holder.bottom.setVisibility(View.VISIBLE);
        holder.bottom.setText(Html.fromHtml(info), TextView.BufferType.SPANNABLE);
      }
      holder.top.setTextColor(Color.BLACK);
      holder.bottom.setTextColor(Color.BLACK);

      if (fileInfo.getDuration() > 0) {
        holder.durationTextView.setText(Util.makeDurationMessage(fileInfo.getDuration()));
        holder.durationTextView.setVisibility(View.VISIBLE);
      } else {
        holder.durationTextView.setVisibility(View.GONE);
      }

      try {
        fileInfo.loadImage(context, holder.thumbnail);
      } catch (IOException e) {
        e.printStackTrace();
      }


    }
  }

  @Override
  public long getItemId(int i) {
    return i;
  }

  @Override
  public int getItemCount() {
    return fileInfos.size();
  }



  public class ViewHolder extends RecyclerView.ViewHolder {
    TextView top;
    TextView bottom;
    ImageView thumbnail;
    TextView durationTextView;

    FrameLayout thumbnailLayout;
    View progressView;

    public ViewHolder(@NonNull View itemView) {
      super(itemView);

      top = (TextView)itemView.findViewById(R.id.toptext);
      bottom = (TextView)itemView.findViewById(R.id.bottomtext);
      thumbnail = (ImageView)itemView.findViewById(R.id.icon);
      durationTextView = (TextView)itemView.findViewById(R.id.duration);
      thumbnailLayout = (FrameLayout)itemView
          .findViewById(R.id.media_list_item_thumbnail_fLayout);
      progressView = itemView.findViewById(R.id.progress);

      itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          int position = getAdapterPosition();
          if (position == RecyclerView.NO_POSITION)
            return;

          listener.onItemSelect(fileInfos, position);
        }
      });
      itemView.setOnLongClickListener(new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
          int position = getAdapterPosition();
          if (position == RecyclerView.NO_POSITION)
            return true;
          FileInfo fileInfo = fileInfos.get(position);
          listener.onItemLongSelect(fileInfos, position);
          return true;
        }
      });
    }
  }
}
