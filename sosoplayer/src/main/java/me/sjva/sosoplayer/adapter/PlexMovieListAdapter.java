package me.sjva.sosoplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import me.sjva.sosoplayer.R;
import java.util.List;
import me.sjva.sosoplayer.util.Util;
import me.sjva.sosoplayer.fragment.OnFragmentEventListener;
import com.google.android.exoplayer2.ext.plex.PlexApi;
import com.google.android.exoplayer2.ext.plex.Video;

public class PlexMovieListAdapter extends RecyclerView.Adapter<PlexMovieListAdapter.ViewHolder> {

  private Context mContext;
  private final String path;
  private final String token;
  private List<Video> mVideo;
  private OnFragmentEventListener mOnFragmentEventListener;
  public PlexMovieListAdapter(Context context,  String path , String token, List<Video> videos, OnFragmentEventListener listener) {
    mContext = context;
    this.path = path;
    this.token = token;
    mVideo = videos;
    mOnFragmentEventListener = listener;
  }

  @NonNull
  @Override
  public PlexMovieListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View convertView = LayoutInflater.from(mContext).inflate(R.layout.plex_movie_list_item, parent, false);
//    int height = (parent.getMeasuredHeight()  / (Util.SPAN_COUNT  )) ;

    int spanCount = mContext.getResources().getConfiguration().screenWidthDp / Util.SPAN_MIN_WIDTH;

    int width = parent.getMeasuredWidth() / spanCount;
    int height = width / 2 * 3;

    return new ViewHolder(convertView, height);
  }

  @Override
  public void onBindViewHolder(@NonNull PlexMovieListAdapter.ViewHolder holder, int position) {
    Video video = mVideo.get(position);
    String thumb =  video.getThumb();
    String imagePath = PlexApi.getContentPath(path, token, thumb);

    Glide.with(mContext)
        .load(imagePath)
        .error(R.drawable.outline_music_video_black_24)
        .into(holder.img_thumb);

    holder.txt_title.setText(video.getTitle());

  }

  @Override
  public int getItemCount() {
    return mVideo.size();
  }


  public class ViewHolder extends RecyclerView.ViewHolder {

    private ImageView img_thumb;
    private TextView txt_title;

    public ViewHolder(@NonNull View itemView, int height) {
      super(itemView);

      img_thumb = (ImageView)itemView.findViewById(R.id.movie_poster_imageview);

      txt_title = (TextView)itemView.findViewById(R.id.movie_title_textview);

      ViewGroup.LayoutParams params = img_thumb.getLayoutParams();
      params.height = height;

      itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          int position = getAdapterPosition();
          if (position == RecyclerView.NO_POSITION)
            return;
          Video video = mVideo.get(position);
          mOnFragmentEventListener.onItemSelect(video);
        }
      });
      itemView.setOnLongClickListener(new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
          int position = getAdapterPosition();
          if (position == RecyclerView.NO_POSITION)
            return true;
          Video video = mVideo.get(position);
          mOnFragmentEventListener.onItemLongSelect(video);
          return true;
        }
      });
    }
  }

}
