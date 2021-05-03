package me.sjva.sosoplayer.widget;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import me.sjva.sosoplayer.R;
import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import me.sjva.sosoplayer.util.Util;


public class FileExplorer
{
  private Context mContext = null;
  private String mRootPath = null;
  private String mTitle = null;
  private AlertDialog mDialog = null;
  private AlertDialog.Builder mEditDialog = null;
  private FileListAdapter mFileListAdapter = null;
  private boolean mIsEditMode =false;
  private static FileExtensionType mFileExtensionType;

  private RecyclerView mListView;
  private EditText mPathEditText;

  private FileExplorer(Context context)
  {
    mContext = context;
  }


  private void show(String rootPath, String title)
  {
    mRootPath = rootPath;
    mTitle = title;

    AlertDialog.Builder listDialog = new AlertDialog.Builder(mContext);

    listDialog.setTitle(title + rootPath);

    ArrayList<String> list = updateFileList(rootPath);

    mFileListAdapter = new FileListAdapter(mContext, R.layout.layout_fileexplorer_row , title, list);

    LayoutInflater vi = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    //View view = vi.inflate(R.layout.layout_fileexplorer, null);

    Context themedContext  = new ContextThemeWrapper(mContext, android.R.style.Theme_Dialog);

    mListView = new RecyclerView(themedContext);

    mListView.setLayoutManager(new LinearLayoutManager(mContext));
    listDialog.setView(mListView);

    mListView.setAdapter(mFileListAdapter);


    listDialog.setPositiveButton(R.string.typing_text, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        if(mEditDialog != null)
          mEditDialog.show();
      }
    });

    if(mFileExtensionType == FileExtensionType.Directory)
    {
      listDialog.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          if(mOnFileSelectListener != null)
          {
            mOnFileSelectListener.onFileSelect(false, mRootPath);
          }
        }
      });
    }


    listDialog.setOnCancelListener(new DialogInterface.OnCancelListener ()
    {
      @Override
      public void onCancel(DialogInterface dialog) {
        if(mOnFileSelectListener != null)
        {
          mOnFileSelectListener.onCancel();
        }
      }
    } );

    mDialog = listDialog.create();
    mDialog.show();

   // newEditDialog();
  }

  private void newEditDialog()
  {
    mEditDialog = new AlertDialog.Builder(mContext);

    mEditDialog.setTitle(R.string.typing_text);

    LayoutInflater vi = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View view = vi.inflate(R.layout.layout_fileexplorer_edittext, null);


    mPathEditText = new EditText(mContext);//(EditText)mEditDialog.findViewById(R.id.pathedittext);
    mEditDialog.setView(mPathEditText);

    mEditDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
    {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        String path = mPathEditText.getText().toString();
        File fp = new File(path);
        if (fp.exists() == false)
        {
          Toast.makeText(mContext, R.string.error_cannot_access, Toast.LENGTH_LONG).show();
          return;
        }

        if(mOnFileSelectListener != null)
        {
          mOnFileSelectListener.onFileSelect(false, path);
        }
      }
    });

    mEditDialog.setOnCancelListener(new DialogInterface.OnCancelListener ()
    {
      @Override
      public void onCancel(DialogInterface dialog)
      {
        show(mRootPath, mTitle);
      }
    });
  }

  public interface OnFileSelectListener
  {

    void onFileSelect(boolean needConfirm, String filePath);
    void onCancel();
  }

  private void setOnFileSelectListener(OnFileSelectListener listener)
  {
    mOnFileSelectListener = listener;
  }

  private OnFileSelectListener mOnFileSelectListener = null;

  static public void show(Context context, String rootPath, String title, FileExtensionType fileExtensionType, OnFileSelectListener onFileSelectListener)
  {
    mFileExtensionType = fileExtensionType;
    FileExplorer fe = new FileExplorer(context);
    fe.setOnFileSelectListener(onFileSelectListener);
    fe.show(rootPath, title);
  }

  public enum FileExtensionType
  {
    Subtitle,
    Font,
    Directory,
  }

  private static boolean checkExtension( String fleName)
  {
    boolean bRet = false;
    switch(mFileExtensionType)
    {
      case Subtitle:
        bRet = Util.isSubtitleFile(fleName);
        break;
      case Font:
        bRet = Util.isFontFile(fleName);
        break;
    }
    return bRet;
  }


  private ArrayList<String> updateFileList(String rootPath)
  {
    File dir = new File(rootPath);
    File[] fileList = dir.listFiles();
    ArrayList<String> list = new ArrayList<String>();
    int count = (fileList == null) ? 0 : fileList.length;
    if(mRootPath.equals("/") == false)
    {
      list.add("..");
    }

    for (int i = 0; i < count; i++)
    {
      String name = fileList[i].getName();
      String lowName = name.toLowerCase();
      if(checkExtension(lowName) == true)
      {
        list.add(name);
      }
      else if (fileList[i].isDirectory() )//&& fileList[i].isHidden() == false)
      {
        list.add(name);
      }
    }
    Collections.sort(list ,fileInfoComparator);

    return list;
  }

  private final static Comparator<String> fileInfoComparator = new Comparator<String>()
  {
    private final Collator collator = Collator.getInstance();

    public int compare(String info1, String info2)
    {
      if(info1.equals("..") == true)
      {
        return 1;
      }
      else if(info2.equals("..") == true)
      {
        return 1;
      }


      boolean isSubtitle1 = checkExtension(info1);
      boolean isSubtitle2 = checkExtension(info2);
      if ((isSubtitle1 && isSubtitle2) || (!isSubtitle1 && !isSubtitle2))
      {
        return collator.compare(info1, info2);
      }
      else if (isSubtitle1 || !isSubtitle2)
      {
        return 1;
      }
      else if (!isSubtitle1 || isSubtitle2)
      {
        return -1;
      }

      return 0;
    }
  };



  private class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.ViewHolder>
  {
    ArrayList<String> mFileList;
    String mTitle;
    public FileListAdapter(Context context, int textViewResourceId, String title,
        ArrayList<String> objects)
    {
      mFileList = objects;
      mTitle = title;
    }



    public void updateList(String rootPath)
    {
      mDialog.setTitle(mTitle + rootPath);
      mFileList = updateFileList(rootPath);
      notifyDataSetChanged();
    }


    public String getName(int index)
    {
      if(mFileList != null && mFileList.size() > index )
        return mFileList.get(index);
      return "";
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      Context context = parent.getContext() ;
      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;

      View view = inflater.inflate(R.layout.layout_fileexplorer_row, parent, false);
      return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
      String fleName = mFileList.get(position);
      if (fleName != null)
      {
        holder.fileNameTextView.setText(fleName);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
        holder.fileNameTextView.setLayoutParams(params);
        holder.fileNameTextView.setPadding(10, 10, 0, 10);


        if(checkExtension(fleName) == true)
        {
          holder.icon.setVisibility(View.GONE);
        }
        else
        {
          holder.icon.setImageResource(R.drawable.ic_folder_holo_dark);
          holder.icon.setVisibility(View.VISIBLE);
        }

      }
    }

    @Override
    public int getItemCount() {
      return mFileList.size();
    }

    public class ViewHolder  extends RecyclerView.ViewHolder{
      TextView fileNameTextView;
      ImageView icon;
      public ViewHolder(@NonNull View itemView) {
        super(itemView);
        fileNameTextView = (TextView) itemView.findViewById(R.id.filename);
        icon = (ImageView) itemView.findViewById(R.id.icon);
        itemView.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            int pos = getAdapterPosition() ;
            if (pos == RecyclerView.NO_POSITION) {
              return;
            }
            String name = mFileListAdapter.getName(pos);
            if(name.equals("..") == true)
            {
              int index = mRootPath.lastIndexOf("/");
              if(index == 0)
                mRootPath = "/";
              else
                mRootPath = mRootPath.substring(0, index);
              mFileListAdapter.updateList(mRootPath);
            }
            else
            {
              if(mRootPath.endsWith("/") == true)
              {
                mRootPath = "/" + name;
              }
              else
              {
                mRootPath += "/" + name;
              }

              File dir = new File(mRootPath);

              if(dir.isDirectory() == true && dir.isHidden() == false)
              {
                if(mFileExtensionType == FileExtensionType.Directory)
                {
                  File file = new File(mRootPath);
                  File[] fileList = file.listFiles();

                  int count = (fileList == null) ? 0 : fileList.length;
                  boolean havaSubDir = false;
                  for (int i = 0; i < count; i++ ){
                    if (fileList[i].isDirectory()) {
                      havaSubDir = true;
                      break;
                    }
                  }

                  if (!havaSubDir) {
                    if(mOnFileSelectListener != null)
                    {
                      mOnFileSelectListener.onFileSelect(true, mRootPath);
                    }
                    mDialog.dismiss();
                    return;
                  }
                }
                mFileListAdapter.updateList(mRootPath);
              }
              else
              {
                if(mOnFileSelectListener != null)
                {
                  mOnFileSelectListener.onFileSelect(false, mRootPath);
                  mDialog.dismiss();
                }else {
                  mDialog.dismiss();
                }

              }
            }
          }
        });
      }
    }
  }



}