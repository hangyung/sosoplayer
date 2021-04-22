package me.sjva.sosoplayer.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import me.sjva.sosoplayer.R;
import me.sjva.sosoplayer.activity.OnMainEventListener;
import me.sjva.sosoplayer.adapter.MediaStoreChildAdapter;
import me.sjva.sosoplayer.data.FileInfo;
import me.sjva.sosoplayer.data.StorageInfo;

public class StorageListLongClickDialog {
  private AlertDialog.Builder builder;
  private AlertDialog dialog;

//  private AppCompatButton editTextView;
//  private AppCompatButton removeTextView;
  private Context context;
  private StorageInfo storageInfo;
  private OnMainEventListener onMainEventListener;
  public StorageListLongClickDialog(@NonNull Context context, StorageInfo storageInfo, OnMainEventListener onMainEventListener){
    this.context = context;
    this.storageInfo = storageInfo;
    this.onMainEventListener = onMainEventListener;
    ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(context, R.style.Theme_AppCompat_Light_Dialog_Alert);
    builder = new AlertDialog.Builder(contextThemeWrapper);
    String title = String.format("%s change", storageInfo.getName());
    builder.setTitle(title);
    LayoutInflater layoutInflater = (LayoutInflater)LayoutInflater.from(contextThemeWrapper);
    Context themedContext  = new android.view.ContextThemeWrapper(context, android.R.style.Theme_Dialog);
    RecyclerView recyclerView = new RecyclerView(themedContext);
    recyclerView.setLayoutManager(new LinearLayoutManager(themedContext));

    recyclerView.setAdapter(new StorageListLongClickMenuAdapter(themedContext));
    builder.setView(recyclerView);

  }
  public void show() {
    dialog = builder.show();
  }

  public class StorageListLongClickMenuAdapter extends RecyclerView.Adapter<StorageListLongClickMenuAdapter.ViewHolder>{
    private ArrayList<String> menuItems;
    public StorageListLongClickMenuAdapter(Context context) {
      menuItems = new ArrayList<String>();
      menuItems.add(context.getString(R.string.storage_edit));
      menuItems.add(context.getString(R.string.storage_remove));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View convertView = LayoutInflater.from(context).inflate(R.layout.media_list_item, parent, false);
      return new StorageListLongClickMenuAdapter.ViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
      String menu = menuItems.get(position);
      holder.bottom.setVisibility(View.GONE);
      holder.top.setText(menu);
      switch (position){
        case 0:
          Glide.with(context)
                  .load(R.drawable.outline_edit_black_24)
                  .into(holder.thumbnail);
          break;
        case 1:
          Glide.with(context)
                  .load(R.drawable.outline_delete_forever_black_24)
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
                new StorageManagerDialog(context, storageInfo, onMainEventListener).show();
                break;
              case 1:
                new ConfirmDialog(context, context.getString(R.string.confirm_remove),
                  context.getString(R.string.confirm_remove_message),
                  new ConfirmDialog.OnComfirmDialogEventListener() {
                    @Override
                    public void onComfirm(boolean comfirm) {
                      if (comfirm) {
                        onMainEventListener.onRemoveStorage(storageInfo);
                      }
                    }
                  }).show();
                break;
            }
            dialog.dismiss();
          }
        });
      }
    }
  }

}
