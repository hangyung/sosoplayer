package me.sjva.sosoplayer.widget;

import android.content.Context;
import android.content.DialogInterface;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import com.google.android.exoplayer2.C;
import me.sjva.sosoplayer.R;

public class ConfirmDialog {
  private AlertDialog.Builder builder;
  private AlertDialog dialog;
  public interface OnComfirmDialogEventListener{
    void onComfirm(boolean comfirm);
  }

  public ConfirmDialog(@NonNull Context context, String title, String message, OnComfirmDialogEventListener listener) {
    ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(context, R.style.Theme_AppCompat_Light_Dialog_Alert);
    builder = new AlertDialog.Builder(contextThemeWrapper);
    builder.setTitle(title);
    builder.setMessage(message);
    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int i) {
        listener.onComfirm(true);
        dialogInterface.dismiss();
      }
    });
    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int i) {
        listener.onComfirm(false);
        dialogInterface.dismiss();
      }
    });
  }

  public void show() {
    dialog = builder.show();
  }
}
