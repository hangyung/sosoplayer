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
import com.google.android.exoplayer2.ext.plex.Directory;
import com.google.android.exoplayer2.ext.plex.PlexApi;

public class PlexShowListAdapter extends RecyclerView.Adapter<PlexShowListAdapter.ViewHolder> {

  private Context mContext;
  private final List<Directory> mDirectories;
  private OnFragmentEventListener mOnFragmentEventListener;
  private final String path;
  private final String token;
  public PlexShowListAdapter(Context context, String path, String token,
      List<Directory> directories, OnFragmentEventListener listener) {
    mContext = context;
    this.path = path;
    this.token = token;
    mDirectories = directories;
    mOnFragmentEventListener = listener;
  }

  @NonNull
  @Override
  public PlexShowListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View convertView = LayoutInflater.from(mContext).inflate(R.layout.plex_show_list_item, parent, false);
//    int height = (parent.getMeasuredHeight()  / (Util.SPAN_COUNT  )) ;

    int spanCount = mContext.getResources().getConfiguration().screenWidthDp / Util.SPAN_MIN_WIDTH;

    int width = parent.getMeasuredWidth() / spanCount;
    int height = width / 2 * 3;

    return new ViewHolder(convertView, height);
  }

  @Override
  public void onBindViewHolder(@NonNull PlexShowListAdapter.ViewHolder holder, int position) {
    Directory directory = mDirectories.get(position);
    String thumb =  directory.getThumb();
    String imagePath = PlexApi.getContentPath(path, token, thumb);

    Glide.with(mContext)
        .load(imagePath)
        .error(R.drawable.outline_music_video_black_24)
        .into(holder.img_thumb);
    holder.txt_episode_count.setText(directory.getLeafCount());
    holder.txt_title.setText(directory.getTitle());


  }

  @Override
  public int getItemCount() {
    return mDirectories.size();
  }


  public class ViewHolder extends RecyclerView.ViewHolder {

    private ImageView img_thumb;
    private TextView txt_episode_count;
    private TextView txt_title;

    public ViewHolder(@NonNull View itemView, int height) {
      super(itemView);

      img_thumb = (ImageView)itemView.findViewById(R.id.show_poster_imageview);
      txt_episode_count = (TextView)itemView.findViewById(R.id.show_episode_count);
      txt_title = (TextView)itemView.findViewById(R.id.show_title_textview);

      ViewGroup.LayoutParams params = img_thumb.getLayoutParams();
      params.height = height;

      itemView.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          int position = getAdapterPosition();
          if (position == RecyclerView.NO_POSITION)
            return;
          Directory directory = mDirectories.get(position);
          mOnFragmentEventListener.onContentSelect(directory);
        }
      });

    }
  }

}
