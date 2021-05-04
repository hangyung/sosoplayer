package me.sjva.sosoplayer.widget;

import android.content.Context;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.ext.plex.Video;

import java.util.ArrayList;

import me.sjva.sosoplayer.R;
import me.sjva.sosoplayer.activity.OnMainEventListener;
import me.sjva.sosoplayer.data.FileInfo;
import me.sjva.sosoplayer.data.StorageInfo;
import me.sjva.sosoplayer.fragment.OnCommonEventListener;

public class FileListLongClickDialog {
  private AlertDialog.Builder builder;
  private AlertDialog dialog;

//  private AppCompatButton editTextView;
//  private AppCompatButton removeTextView;
  private Context context;
  ArrayList<? extends Parcelable > fileInfos;
  private int position;
  private OnCommonEventListener onCommonEventListener;
  public FileListLongClickDialog(@NonNull Context context, ArrayList<? extends Parcelable > fileInfos, int position, OnCommonEventListener onCommonEventListener){
    this.context = context;

    this.onCommonEventListener = onCommonEventListener;
    ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(context, R.style.Theme_AppCompat_Light_Dialog_Alert);
    builder = new AlertDialog.Builder(contextThemeWrapper);
    this.fileInfos = fileInfos;
    this.position = position;

    Parcelable fileInfo = fileInfos.get(position);

    String title = null;
    if (fileInfo instanceof FileInfo) {
      title =  String.format("%s", ((FileInfo)fileInfo).getName());
    } else if (fileInfo instanceof Video) {
      title =  String.format("%s", ((Video)fileInfo).getTitle());
    }

    if (!TextUtils.isEmpty(title)) {
      builder.setTitle(title);
    }

    LayoutInflater layoutInflater = (LayoutInflater)LayoutInflater.from(contextThemeWrapper);
    Context themedContext  = new android.view.ContextThemeWrapper(context, android.R.style.Theme_Dialog);
    RecyclerView recyclerView = new RecyclerView(themedContext);
    recyclerView.setLayoutManager(new LinearLayoutManager(themedContext));

    recyclerView.setAdapter(new FileListLongClickMenuAdapter(themedContext));
    builder.setView(recyclerView);

  }
  public void show() {
    dialog = builder.show();
  }

  public class FileListLongClickMenuAdapter extends RecyclerView.Adapter<FileListLongClickMenuAdapter.ViewHolder>{
    private ArrayList<String> menuItems;
    public FileListLongClickMenuAdapter(Context context) {
      menuItems = new ArrayList<String>();
      menuItems.add(context.getString(R.string.play_from_scratch));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View convertView = LayoutInflater.from(context).inflate(R.layout.media_list_item, parent, false);
      return new ViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
      String menu = menuItems.get(position);
      holder.bottom.setVisibility(View.GONE);
      holder.top.setText(menu);
      switch (position){
        case 0:
          Glide.with(context)
                  .load(R.drawable.outline_play_circle_black_24)
                  .into(holder.thumbnail);
          break;
      }
    }

    @Override
    public int getItemCount() {
      return menuItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

      TextView top;
      TextView bottom;
      ImageView thumbnail;
      TextView durationTextView;

      FrameLayout thumbnailLayout;

      public ViewHolder(@NonNull View itemView) {
        super(itemView);
        top = (TextView)itemView.findViewById(R.id.toptext);
        bottom = (TextView)itemView.findViewById(R.id.bottomtext);
        thumbnail = (ImageView)itemView.findViewById(R.id.icon);
        durationTextView = (TextView)itemView.findViewById(R.id.duration);
        durationTextView.setVisibility(View.GONE);
        thumbnailLayout = (FrameLayout)itemView
                .findViewById(R.id.media_list_item_thumbnail_fLayout);

        itemView.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            int position = getAdapterPosition();
            if (position == RecyclerView.NO_POSITION)
              return;
            switch (position) {
              case 0:
                onCommonEventListener.onPlayFromScratch(fileInfos.get(position));
                break;
            }
            dialog.dismiss();
          }
        });
      }
    }
  }

}
