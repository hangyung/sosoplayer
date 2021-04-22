package com.google.android.exoplayer2.ext.extrastream;


import android.text.TextUtils;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;

import java.util.ArrayList;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import android.util.Log;

public class FtpFile {

  private FTPClient mClient;

  private FTPClientConfig mConfig;

  private String mUserName;

  private String mPassWd;

  private String mEncoding;

  private int mPort;

  private String mHost;

  private String mWorkingDirectory;

  private String mOsType;

  private boolean mActiveMode;


  public FtpFile(
      String userName, String password, String encoding, String host, String port,
      boolean isActiveMode, String osType) {


    mUserName = userName;
    mPassWd = password;
    mEncoding = encoding;
    mPort = Integer.parseInt(port);
    mHost = host;
    mActiveMode = isActiveMode;
    mOsType = osType;

    mWorkingDirectory = null;

    if (mOsType != null && mOsType.length() > 0 && mOsType.equalsIgnoreCase("Default") == false) {
      mConfig = new FTPClientConfig(mOsType);

    } else {
      mConfig = new FTPClientConfig();
    }
  }

  public String getFileName(String path) {
    String tempdir = path.replace(mHost, "");
    if (tempdir == null || tempdir.length() <= 0)
      return null;

    int index = tempdir.lastIndexOf("/");
    if (index == -1) {
      return null;
    }

    return tempdir.substring(index);
  }

  public void connect() throws SocketException, IOException {
    mClient = new FTPClient();
    if (mEncoding != null && mEncoding.length() > 0
        && mEncoding.equalsIgnoreCase("Default") == false)
      mClient.setControlEncoding(mEncoding); // 인코딩

    mClient.configure(mConfig);

    String host = mHost.replace("ftp://", "");

    mClient.connect(host, mPort);

    if (mActiveMode == false) {
      mClient.enterLocalPassiveMode();
    }

    if (TextUtils.isEmpty(mUserName)) {
      mUserName = "anonymous";
      mPassWd = "";
    }

    mClient.login(mUserName, mPassWd);

    mClient.setFileType(FTP.BINARY_FILE_TYPE);
    mClient.setReceiveBufferSize(32768);
    mClient.setBufferSize(32768);

    mClient.setKeepAlive(true);

    int reply = mClient.getReplyCode();
    Log.d("FTPUtil", "reply : " + reply);
    if (!FTPReply.isPositiveCompletion(reply)) {

      disconnect();
      throw new IOException();
    }

    if (mWorkingDirectory != null)
      changeWorkingDirectory(mWorkingDirectory);
    // mWorkingDirectory = mClient.printWorkingDirectory();
  }

  public InputStream retrieveFileStream(String dir) throws IOException {
    return mClient.retrieveFileStream(dir);
  }

  public FTPFile[] listFiles() throws IOException {
    return mClient.listFiles();
  }

  public ArrayList<ExtraStreamFileInfo> list() throws IOException {
    ArrayList<ExtraStreamFileInfo> extraStreamFileInfos = new ArrayList<ExtraStreamFileInfo>();
    FTPFile[] ftpFiles = listFiles();
    int count = (ftpFiles == null) ? 0 : ftpFiles.length;
    for (int i = 0; i < count; i++) {
      ExtraStreamFileInfo extraStreamFileInfo = new ExtraStreamFileInfo(ftpFiles[i].getName(), ftpFiles[i].getName(),
          ftpFiles[i].isDirectory(), ftpFiles[i].getSize());
      extraStreamFileInfos.add(extraStreamFileInfo);
    }
    return extraStreamFileInfos;
  }

  /**
   * 서버와의 연결을 끊는다.
   *
   * @throws IOException
   */
  public void disconnect() {
    if (mClient != null) {
      try {
        mClient.logout();
      } catch (IOException e) {
        e.printStackTrace();
      }

      try {
        mClient.disconnect();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    mClient = null;
  }

  public boolean isConnected() {
    return mClient != null;
  }

  public boolean changeWorkingDirectory(String subDir) throws IOException {

//    boolean bRet =
//    // 일부 FTP 에서 "/" 를 붙여주지 않으면 디렉토리 목록을 가져오지 못하는 현상 발생함.
//    if (bRet = true) {
//      saveWorkingDirectory(subDir);
//    }
    mWorkingDirectory = subDir;
    return mClient.changeWorkingDirectory(subDir + "/");
  }

  public boolean changeToParentDirectory() throws IOException {
    // return mClient.changeToParentDirectory();
    if (mWorkingDirectory == null || mWorkingDirectory.length() <= 0) {
      mWorkingDirectory = "/";
      return false;
    }

    if (mWorkingDirectory.equals("/") == true || mWorkingDirectory.startsWith("/") == false) {
      return false;
    }

    int lastIndex = mWorkingDirectory.lastIndexOf("/");
    if (lastIndex == 0) {
      mWorkingDirectory = "/";
      return true;
    }
    mWorkingDirectory = mWorkingDirectory.substring(0, lastIndex);

    return true;

  }

  public boolean setWorkingDirectory(String path, String subDir) throws IOException {
    String workingDirectory = subDir.replace(path, "");
    if (workingDirectory == null || workingDirectory.length() <= 0)
      workingDirectory = "/";

    return changeWorkingDirectory(workingDirectory);
  }

  public String getWorkingDirectory() throws IOException {
    // return mClient.printWorkingDirectory();
    return mWorkingDirectory;
  }

  public FtpInputStream newFtpRandomAccessFile(String name, long offset) throws IOException {
    return new FtpInputStream(this, name, offset);
  }

  public FTPClient getFTPClient() throws SocketException, IOException {
    if (mClient == null) {
      try {
        connect(); // 연결 실패시 한번 더 시도
      } catch (Exception e) {
        disconnect();
        connect();
      }
    }

    return mClient;
  }
}