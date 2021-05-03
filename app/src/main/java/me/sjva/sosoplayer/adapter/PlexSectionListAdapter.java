package me.sjva.sosoplayer.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import me.sjva.sosoplayer.R;
import me.sjva.sosoplayer.fragment.OnCommonEventListener;
import com.google.android.exoplayer2.ext.plex.Directory;

public class PlexSectionListAdapter extends RecyclerView.Adapter<PlexSectionListAdapter.ViewHolder> {

  private Context mContext;
  private final List<Directory> mDirectories;
  private OnCommonEventListener mOnCommonEventListener;
  public PlexSectionListAdapter(Context context, List<Directory> directories, OnCommonEventListener listener) {
    mContext = context;
    mDirectories = directories;
    mOnCommonEventListener = listener;
  }

  @NonNull
  @Override
  public PlexSectionListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View convertView = LayoutInflater.from(mContext).inflate(R.layout.plex_section_list_item, parent, false);
//    int height = (parent.getMeasuredHeight()  / (Util.SPAN_COUNT  )) ;

    return new ViewHolder(convertView);
  }

  @Override
  public void onBindViewHolder(@NonNull PlexSectionListAdapter.ViewHolder holder, int position) {
    Directory directory = mDirectories.get(position);

    holder.top.setText(directory.getTitle());



    String  type = directory.getType();
    holder.bottom.setText(type);
    if (type.equals("movie")) {
      Glide.with(mContext)
          .load(R.drawable.outline_local_movies_black_48)
          .into(holder.thumbnail);
      holder.parentView.setEnabled(true);
    } else if (type.equals("show")) {
      Glide.with(mContext)
          .load(R.drawable.outline_monitor_black_48)
          .into(holder.thumbnail);
      holder.parentView.setEnabled(true);
    } else {
      Glide.with(mContext)
          .load(R.drawable.outline_close_black_48)
          .into(holder.thumbnail);
      holder.parentView.setEnabled(false);
    }


    holder.top.setTextColor(Color.BLACK);
    holder.bottom.setTextColor(Color.BLACK);

  }

  @Override
  public int getItemCount() {
    return mDirectories.size();
  }


  public class ViewHolder extends RecyclerView.ViewHolder {

    View parentView;
    TextView top;
    TextView bottom;
    ImageView thumbnail;
    LinearLayout thumbnailLayout;

    public ViewHolder(@NonNull View itemView) {
      super(itemView);
      parentView = itemView;
      top = (TextView)itemView.findViewById(R.id.toptext);
      bottom = (TextView)itemView.findViewById(R.id.bottomtext);
      thumbnail = (ImageView)itemView.findViewById(R.id.icon);

      thumbnailLayout = (LinearLayout)itemView
          .findViewById(R.id.media_list_item_thumbnail_fLayout);

      itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          int pos = getAdapterPosition();
          if (pos == RecyclerView.NO_POSITION) {
            return;
          }
          Directory directory = mDirectories.get(pos);
          mOnCommonEventListener.onSectionSelect(directory);
        }
      });

    }
  }

}
