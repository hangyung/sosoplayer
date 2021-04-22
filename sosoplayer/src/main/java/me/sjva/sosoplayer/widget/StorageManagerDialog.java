package me.sjva.sosoplayer.widget;

import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatSpinner;
import java.io.File;
import me.sjva.sosoplayer.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.ArrayList;
import me.sjva.sosoplayer.activity.OnMainEventListener;
import me.sjva.sosoplayer.data.StorageInfo;
import me.sjva.sosoplayer.util.Util;
import me.sjva.sosoplayer.data.StorageType;

public class StorageManagerDialog implements View.OnClickListener {
  private LinearLayout storageTypeLinearLayout;
  private AppCompatSpinner storageTypeSpinner;
  private TextInputLayout nameTextInputLayout;
  private TextInputEditText nameTextInputEditText;
  private LinearLayout ftpOptionLayout;

  private CheckBox isActiveModeCheckBox;
  private OnMainEventListener listener;
  private TextInputLayout storageUriTextInputLayout;
  private TextInputEditText  storageUriTextInputEditText;

  private TextInputLayout storagePlexTokenTextInputLayout;
  private TextInputEditText  storagePlexTokenTextInputEditText;

  private TextInputLayout portTextInputLayout;
  private TextInputEditText  portTextInputEditText;
  private AppCompatButton storageSearchButton;

  private LinearLayout userOptionLayout;
  private CheckBox anonymousCheckBox;
  private CheckBox hidepassCheckBox;

  private TextInputLayout userIdTextInputLayout;
  private TextInputEditText  userIdTextInputEditText;
  private TextInputLayout passwdTextInputLayout;
  private TextInputEditText  passwdTextInputEditText;

  private LinearLayout encodinglayout;
  private AppCompatSpinner encodingSpinner;

  private AppCompatButton okButton;
  private AppCompatButton cancelButton;

  private AlertDialog.Builder builder;
  private AlertDialog dialog;

  private StorageInfo orgStorageInfo;

  private  String[] encodingEntries;
  public StorageManagerDialog(@NonNull Context context, OnMainEventListener listener) {
    this(context, null, listener);
  }

  public StorageManagerDialog(@NonNull Context context, StorageInfo storageInfo, OnMainEventListener listener) {
    this.listener = listener;
    encodingEntries = Util.encodingEntries(context);


    orgStorageInfo = storageInfo;
    ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(context, R.style.Theme_AppCompat_Light_Dialog_Alert);
    builder = new AlertDialog.Builder(contextThemeWrapper);
    if (storageInfo == null) {
      builder.setTitle("Add Drive");
    } else {
      builder.setTitle("Modify Drive");
    }


    LayoutInflater layoutInflater = (LayoutInflater)LayoutInflater.from(contextThemeWrapper);

    View convertView = layoutInflater.inflate(R.layout.dialog_addstorage, null);
    builder.setView(convertView);
    storageTypeLinearLayout = convertView.findViewById(R.id.storagetype_linearLayout);
    if (orgStorageInfo != null) {
      storageTypeLinearLayout.setVisibility(View.GONE);
    }
    storageTypeSpinner = convertView.findViewById(R.id.storagetype_spinner);
    nameTextInputLayout = convertView.findViewById(R.id.edittext_storage_name_inputLayout);
    nameTextInputEditText = convertView.findViewById(R.id.edittext_storage_name);
    ftpOptionLayout = convertView.findViewById(R.id.ftp_option_layout);
    isActiveModeCheckBox = convertView.findViewById(R.id.isActiveMode);

    storageUriTextInputLayout = convertView.findViewById(R.id.edittext_storage_uri_inputLayout);
    storageUriTextInputEditText = convertView.findViewById(R.id.edittext_storage_uri);

    storagePlexTokenTextInputLayout= convertView.findViewById(R.id.edittext_storage_plextoken_inputLayout);
    storagePlexTokenTextInputEditText= convertView.findViewById(R.id.edittext_storage_plextoken);

    portTextInputLayout = convertView.findViewById(R.id.edittext_storage_port_inputLayout);
    portTextInputEditText = convertView.findViewById(R.id.edittext_storage_port);
    storageSearchButton = convertView.findViewById(R.id.storage_search_button);

    userOptionLayout = convertView.findViewById(R.id.user_option_layout);
    anonymousCheckBox = convertView.findViewById(R.id.anonymouscheckbox);
    hidepassCheckBox = convertView.findViewById(R.id.hidepasswdcheckbox);
    userIdTextInputLayout = convertView.findViewById(R.id.edittext_storage_userid_inputLayout);
    userIdTextInputEditText = convertView.findViewById(R.id.edittext_storage_userid);
    passwdTextInputLayout = convertView.findViewById(R.id.edittext_storage_passwd_inputLayout);
    passwdTextInputEditText = convertView.findViewById(R.id.edittext_storage_passwd);

    anonymousCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
        updateUserLayout(checked);
      }
    });

    hidepassCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
        changePasswdInputType(checked);
      }
    });

    encodinglayout = convertView.findViewById(R.id.encoding_layout);
    encodingSpinner = convertView.findViewById(R.id.encoding_spinner);

    okButton = convertView.findViewById(R.id.storage_ok_button);
    cancelButton = convertView.findViewById(R.id.storage_cancel_button);
    cancelButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        dialog.dismiss();
      }
    });
    okButton.setOnClickListener(this);

    storageSearchButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        FileExplorer.show(context, "/sdcard", "Select Folder : ",
            FileExplorer.FileExtensionType.Directory, new FileExplorer.OnFileSelectListener() {

              @Override
              public void onFileSelect(boolean needConfirm,String filePath) {
                if (!needConfirm) {
                  File file = new File(filePath);
                  StorageInfo storageInfo = new StorageInfo(StorageType.Mount, file.getName(), filePath);
                  if (orgStorageInfo == null) {
                    listener.onAddStorage(storageInfo);
                  } else {
                    listener.onModifyStorage(orgStorageInfo, storageInfo);
                  }
                  dialog.dismiss();
                  return;
                }

                String message = null;
                if (orgStorageInfo == null) {
                  message = String.format("Add %s ?", filePath);
                } else {
                  message = String.format("Change to  %s ?", filePath);
                }

                new AlertDialog.Builder(context)
                    .setMessage(message)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialogInterface, int i) {

                        File file = new File(filePath);
                        StorageInfo storageInfo = new StorageInfo(StorageType.Mount, file.getName(), filePath);
                        if (orgStorageInfo == null) {
                          listener.onAddStorage(storageInfo);
                        } else {
                          listener.onModifyStorage(orgStorageInfo, storageInfo);
                        }

                        dialogInterface.dismiss();
                        dialog.dismiss();
                      }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                      }
                    }).show();
              }

              @Override
              public void onCancel() {
                if (dialog != null) {
                  dialog.dismiss();
                }
              }
            });
      }
    });

    encodingSpinner.setAdapter(new ArrayAdapter(context, android.R.layout.simple_list_item_1, encodingEntries));

    if (orgStorageInfo == null) {
      ArrayList<String> driveList = new ArrayList<String>();
      for (StorageType method : StorageType.values()){
        switch (method){
          case MediaStore:
            continue;
          default:
            driveList.add(method.toString());
        }
      }
      storageTypeSpinner.setAdapter(new ArrayAdapter(context, android.R.layout.simple_list_item_1, driveList));

      storageTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
          StorageType storageType = StorageType.fromInt(i + 1);
          updateDialog(false ,storageType);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
      });
      storageTypeSpinner.setSelection(0);
    } else {
      updateDialog(true, orgStorageInfo.getStorageType());
    }
  }

  private void changePasswdInputType(boolean checked) {
    if (checked) {
      passwdTextInputEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
    } else {
      passwdTextInputEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    }
  }

  private void updateUserLayout(boolean checked){
    if (checked) {
      userIdTextInputLayout.setEnabled(false);
      userIdTextInputEditText.setEnabled(false);
      userIdTextInputEditText.setText("anonymous");
      passwdTextInputLayout.setVisibility(View.GONE);
    } else {
      userIdTextInputLayout.setEnabled(true);
      userIdTextInputEditText.setEnabled(true);
      userIdTextInputEditText.setText("");
      passwdTextInputLayout.setVisibility(View.VISIBLE);
    }
  }

  private void updateDialog(boolean isModify, StorageType storageType) {
    nameTextInputLayout.setVisibility(View.GONE);
    ftpOptionLayout.setVisibility(View.GONE);
    portTextInputLayout.setVisibility(View.GONE);
    userOptionLayout.setVisibility(View.GONE);
    encodinglayout.setVisibility(View.GONE);
    storageSearchButton.setVisibility(View.GONE);
    storageUriTextInputLayout.setVisibility(View.VISIBLE);
    storagePlexTokenTextInputLayout.setVisibility(View.GONE);
    cancelButton.setVisibility(View.VISIBLE);
    okButton.setVisibility(View.VISIBLE);

    anonymousCheckBox.setVisibility(View.GONE);
    changePasswdInputType(hidepassCheckBox.isChecked());
    updateUserLayout(anonymousCheckBox.isChecked());

    storageUriTextInputLayout.setHint("Uri");
    switch (storageType){
      case Mount:
        storageSearchButton.setVisibility(View.VISIBLE);
        storageUriTextInputLayout.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);
        okButton.setVisibility(View.GONE);
        break;
      case Samba: {
          nameTextInputLayout.setVisibility(View.VISIBLE);
          userOptionLayout.setVisibility(View.VISIBLE);
          if (isModify) {
            nameTextInputEditText.setText(orgStorageInfo.getName());
            storageUriTextInputEditText.setText(orgStorageInfo.getPath());
            userIdTextInputEditText.setText(orgStorageInfo.getId());
            passwdTextInputEditText.setText(orgStorageInfo.getPasswd());
          }
        }
        break;
      case Ftp: {
          nameTextInputLayout.setVisibility(View.VISIBLE);
          ftpOptionLayout.setVisibility(View.VISIBLE);
          userOptionLayout.setVisibility(View.VISIBLE);
          encodinglayout.setVisibility(View.VISIBLE);
          anonymousCheckBox.setVisibility(View.VISIBLE);
          portTextInputLayout.setVisibility(View.VISIBLE);
          portTextInputEditText.setText("21");
          storageUriTextInputLayout.setHint("Host");
          if (isModify) {
            nameTextInputEditText.setText(orgStorageInfo.getName());
            storageUriTextInputEditText.setText(orgStorageInfo.getPath());
            userIdTextInputEditText.setText(orgStorageInfo.getId());
            passwdTextInputEditText.setText(orgStorageInfo.getPasswd());
            portTextInputEditText.setText(orgStorageInfo.getPort());
            for(int i = 0 ; i < encodingEntries.length; i++) {
              if (orgStorageInfo.getEncoding().equals(encodingEntries[i])){
                encodingSpinner.setSelection(i);
                break;
              }
            }
          }
        }
        break;
      case WebDav: {
          nameTextInputLayout.setVisibility(View.VISIBLE);
          userOptionLayout.setVisibility(View.VISIBLE);
          if (isModify) {
            nameTextInputEditText.setText(orgStorageInfo.getName());
            storageUriTextInputEditText.setText(orgStorageInfo.getPath());
            userIdTextInputEditText.setText(orgStorageInfo.getId());
            passwdTextInputEditText.setText(orgStorageInfo.getPasswd());
          }
        }

        break;
      case Plex: {
          nameTextInputLayout.setVisibility(View.VISIBLE);
          storagePlexTokenTextInputLayout.setVisibility(View.VISIBLE);
          if (isModify) {
            nameTextInputEditText.setText(orgStorageInfo.getName());
            storagePlexTokenTextInputEditText.setText(orgStorageInfo.getToken());
          }
        }
        break;
    }

  }


  public void show() {
    dialog = builder.show();
  }

  private String getTextInputEditTextString(TextInputEditText textInputEditText) {
    try {
      return textInputEditText.getText().toString();
    }catch (Exception e) {
      return null;
    }
  }


  @Override
  public void onClick(View view) {
    int pos = storageTypeSpinner.getSelectedItemPosition();
    StorageType storageType = StorageType.fromInt(pos + 1);
    if (orgStorageInfo != null) {
      storageType = orgStorageInfo.getStorageType();
    }
    String name = getTextInputEditTextString(nameTextInputEditText);
    String url = getTextInputEditTextString(storageUriTextInputEditText);
    String port = getTextInputEditTextString(portTextInputEditText);
    String userId = getTextInputEditTextString(userIdTextInputEditText);
    String passwd = getTextInputEditTextString(passwdTextInputEditText);
    String encoding = encodingEntries[encodingSpinner.getSelectedItemPosition()];
    boolean isActivieMode = isActiveModeCheckBox.isChecked();
    String token = getTextInputEditTextString(storagePlexTokenTextInputEditText);
    switch (storageType) {
      case Mount:
        break;
      case Samba: {
          if(TextUtils.isEmpty(name)){
            nameTextInputEditText.requestFocus();
            listener.onError("Error Name is Empty");
            return;
          }
          if(TextUtils.isEmpty(url)){
            storageUriTextInputEditText.requestFocus();
            listener.onError("Error Url is Empty");
            return;
          }
          if(TextUtils.isEmpty(userId)){
            listener.onError("Error userId is Empty");
            userIdTextInputEditText.requestFocus();
            return;
          }
          if(TextUtils.isEmpty(passwd)){
            listener.onError("Error passwd is Empty");
            userIdTextInputEditText.requestFocus();
            return;
          }
        }
        if (orgStorageInfo == null) {
          listener.onAddStorage(new StorageInfo(StorageType.Samba, name, url, userId, passwd));
        } else {
          listener.onModifyStorage(orgStorageInfo, new StorageInfo(StorageType.Samba, name, url, userId, passwd));
        }
        break;
      case Ftp: {
          if(TextUtils.isEmpty(name)){
            nameTextInputEditText.requestFocus();
            listener.onError("Error Name is Empty");
            return;
          }
          if(TextUtils.isEmpty(url)){
            storageUriTextInputEditText.requestFocus();
            listener.onError("Error Url is Empty");
            return;
          }
          if(TextUtils.isEmpty(userId)){
            listener.onError("Error userId is Empty");
            userIdTextInputEditText.requestFocus();
            return;
          }

          if(TextUtils.isEmpty(port)){
            port = "21";
            portTextInputEditText.setText(port);
          }

          if (anonymousCheckBox.isChecked()) {
            if(TextUtils.isEmpty(passwd)){
              listener.onError("Error passwd is Empty");
              passwdTextInputEditText.requestFocus();
              return;
            }
          }
          if (orgStorageInfo == null) {
            listener.onAddStorage(new StorageInfo(StorageType.Ftp, name, url, userId, passwd, port, null, encoding, isActivieMode));
          } else {
            listener.onModifyStorage(orgStorageInfo, new StorageInfo(StorageType.Ftp, name, url, userId, passwd, port, null, encoding, isActivieMode));
          }
        }
        break;
      case WebDav: {
          if(TextUtils.isEmpty(name)){
            nameTextInputEditText.requestFocus();
            listener.onError("Error Name is Empty");
            return;
          }
          if(TextUtils.isEmpty(url)){
            storageUriTextInputEditText.requestFocus();
            listener.onError("Error Url is Empty");
            return;
          }
          if(TextUtils.isEmpty(userId)){
            listener.onError("Error userId is Empty");
            userIdTextInputEditText.requestFocus();
            return;
          }

          if (anonymousCheckBox.isChecked()) {
            if(TextUtils.isEmpty(passwd)){
              listener.onError("Error passwd is Empty");
              passwdTextInputEditText.requestFocus();
              return;
            }
          }

          if (orgStorageInfo == null) {
            listener.onAddStorage(new StorageInfo(StorageType.WebDav, name, url, userId, passwd));
          } else {
            listener.onModifyStorage(orgStorageInfo, new StorageInfo(StorageType.WebDav, name, url, userId, passwd));
          }
        }
        break;
      case Plex: {
          if(TextUtils.isEmpty(name)){
            nameTextInputEditText.requestFocus();
            listener.onError("Error Name is Empty");
            return;
          }
          if(TextUtils.isEmpty(url)){
            storageUriTextInputEditText.requestFocus();
            listener.onError("Error Url is Empty");
            return;
          }
          if(TextUtils.isEmpty(token)){
            listener.onError("Error Plex Token is Empty");
            storagePlexTokenTextInputEditText.requestFocus();
            return;
          }

          if (orgStorageInfo == null) {
            listener.onAddStorage(new StorageInfo(StorageType.Plex, name, url, token));
          } else {
            listener.onModifyStorage(orgStorageInfo, new StorageInfo(StorageType.Plex, name, url, token));
          }
        }
        break;
    }
    dialog.dismiss();
  }
}
