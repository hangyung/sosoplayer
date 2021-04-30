package me.sjva.sosoplayer.widget;


import android.content.Context;
import android.content.DialogInterface;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;

import me.sjva.sosoplayer.R;

public class LicenseInfoDialog {
    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    public interface OnComfirmDialogEventListener{
        void onComfirm(boolean comfirm);
    }

    public LicenseInfoDialog(@NonNull Context context) {
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(context, R.style.Theme_AppCompat_Light_Dialog_Alert);
        builder = new AlertDialog.Builder(contextThemeWrapper);
        builder.setTitle(context.getString(R.string.soso_settings_category_info_license));

        WebView webView = new WebView(context);
        webView.loadUrl("file:///android_asset/OpenSourceLicense.html");
        builder.setView(webView);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();
            }
        });
    }

    public void show() {
        dialog = builder.show();
    }
}
