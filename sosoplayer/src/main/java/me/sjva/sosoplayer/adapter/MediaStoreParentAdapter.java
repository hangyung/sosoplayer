package me.sjva.sosoplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import me.sjva.sosoplayer.R;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import me.sjva.sosoplayer.data.FileInfo;
import me.sjva.sosoplayer.fragment.OnItemSelectListener;


public class MediaStoreParentAdapter extends RecyclerView.Adapter<MediaStoreParentAdapter.ViewHolder> {
  private final Context context;
  private final OnItemSelectListener listener;
  public interface MediaStoreParentEventLister{
    void onItemSelect(String name, ArrayList<FileInfo> fileInfos);
  }
  

  private final  TreeMap<String, ArrayList<FileInfo>> bucketMap;
  public MediaStoreParentAdapter(Context context,   TreeMap<String, ArrayList<FileInfo>> bucketMap , OnItemSelectListener listener){
    this.listener = listener;
    this.context = context;
    this.bucketMap = bucketMap;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View convertView = LayoutInflater.from(context).inflate(R.layout.mediastore_parent_list_item, parent, false);
    return new MediaStoreParentAdapter.ViewHolder(convertView);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    int index =0;
    for (Map.Entry<String, ArrayList<FileInfo>> entry : bucketMap.entrySet()) {
      if (index == position) {
        String name = entry.getKey();
        ArrayList<FileInfo> fileInfos = entry.getValue();
        holder.media_count_textview.setText(Integer.toString(fileInfos.size()));
        holder.bucket_name_textview.setText(name);
        break;
      }
      index++;
    }
  }

  @Override
  public int getItemCount() {
    return bucketMap.size();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {
    LinearLayout mediastore_parent_list_item_linearlayout;
    TextView media_count_textview;
    TextView bucket_name_textview;
    ImageView bucket_folder_imageview;
    public ViewHolder(@NonNull View itemView) {
      super(itemView);
      mediastore_parent_list_item_linearlayout = itemView.findViewById(R.id.mediastore_parent_list_item_linearlayout);
      bucket_folder_imageview = itemView.findViewById(R.id.bucket_folder_imageview);
      media_count_textview = itemView.findViewById(R.id.content_count_textview);
      bucket_name_textview = itemView.findViewById(R.id.bucket_name_textview);
      mediastore_parent_list_item_linearlayout.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          int position = getAdapterPosition();
          if (position == RecyclerView.NO_POSITION)
            return;
          int index =0;
          for (Map.Entry<String, ArrayList<FileInfo>> entry : bucketMap.entrySet()) {
            if (index == position) {
              String name = entry.getKey();
              listener.onItemSelect(entry.getKey(), entry.getValue());
              break;
            }
            index++;
          }
        }
      });
    }
  }
}
