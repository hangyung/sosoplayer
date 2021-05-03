package me.sjva.sosoplayer.widget;

import android.content.Context;
import android.content.DialogInterface;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;

import me.sjva.sosoplayer.R;

public class SubtitleEdgeTypeDialog {
    private AlertDialog.Builder builder;
    private AlertDialog dialog;

    public interface OnSubtitleEdgeTypeSelectListener{
        void  onSubtitleEdgeTypeSelect(int type);
    }

    private OnSubtitleEdgeTypeSelectListener onSubtitleEdgeTypeSelectListener;
    private int findIndexOfValue(String[] values,  String value) {
        if (value != null && values != null) {
            for (int i = values.length - 1; i >= 0; i--) {
                if (values[i].equals(value)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public SubtitleEdgeTypeDialog(@NonNull Context context, int curType, OnSubtitleEdgeTypeSelectListener listener) {
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(context, R.style.Theme_AppCompat_Light_Dialog_Alert);
        this.onSubtitleEdgeTypeSelectListener = listener;
        builder = new AlertDialog.Builder(contextThemeWrapper);
        builder.setTitle(R.string.soso_settings_title_subtitle_edgetype);

        final String[] values = context.getResources().getStringArray(R.array.subtitle_edgetype);
        builder.setSingleChoiceItems(values, curType, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String type = values[i];
                onSubtitleEdgeTypeSelectListener.onSubtitleEdgeTypeSelect(i);
                dialogInterface.dismiss();
            }
        });
    }

    public void show() {
        dialog = builder.show();
    }
}
