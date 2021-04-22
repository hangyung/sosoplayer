package com.google.android.exoplayer2.ext.extrastream;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPFile;

import android.util.Log;

public class FtpInputStream implements ExtraInputStream
{
  private static final int SOCKET_TIMEOUT = 10000;

  private final String mFileName;

  private long length;
  private FtpFile mFTPFile;
  private FTPClient mFtpClient;
  private long mPosition = 0;
  private InputStream mIs;
  private boolean mInterrupt;

  public FtpInputStream(FtpFile fileUtil, String fileName, long offset)
  {
    mFTPFile = fileUtil;
    mFileName = fileName;
    mPosition = offset;
    long length = length();
    if(mPosition > length)
      mPosition = length;
    mInterrupt = false;
  }

  public void setInterrupt()
  {
    mInterrupt = true;
  }

  private void checkInterrupt() throws IOException
  {
    if(mInterrupt == true)
      throw new IOException();
  }

  public long length() {
    if (length == 0) {
      FTPFile[] files;
      try{
        files = mFTPFile.listFiles();
      }
      catch (IOException e) {
        try {
          disconnect();
          files = mFTPFile.listFiles();
        } catch (IOException e1) {
          return 0;
        }

      }
      for (int i=0; i<files.length; i++) {
        FTPFile file = files[i];
        if (file != null) {
          if (file.getName().equals(mFileName)) {
            length = file.getSize();
            break;
          }
        }
      }
    }
    return length;

  }


  public long getFilePointer()
  {
    return mPosition;
  }

  public synchronized int read(byte[] bytes, int offset, int len) throws IOException {
    checkInterrupt();
    try {
      return readFromStream(bytes, offset, len);
    } catch (IOException e) {
      e.printStackTrace();
      disconnect(); //retry
      return readFromStream(bytes, offset, len);
    }
  }

  public boolean newInputStream() throws IOException
  {
    FTPClient client = getFTPClient();
    if(mPosition != 0)
      client.setRestartOffset(mPosition);
    mIs = client.retrieveFileStream(mFileName);
    if(mIs == null)
    {
      disconnect();
      client = getFTPClient();
      if(mPosition != 0)
        client.setRestartOffset(mPosition);
      mIs = client.retrieveFileStream(mFileName);
    }
    if(mIs == null)
      return false;
    return true;

  }

  private int readFromStream(byte[] bytes, int offset, int len) throws IOException {

    if(mIs == null)
    {
      newInputStream();
    }
    FTPClient client = getFTPClient();
    if (mIs != null) {
      int n = 0;
      while (n < len) {
        int bytesRead = mIs.read(bytes, offset+n, len-n);
        if (bytesRead < 0) {
          if (n == 0) return -1;
          else break;
        }
        n += bytesRead;
      }

      mPosition += n;
      return n;
    } else {
      String msg = String.format("Unable to retrieve input stream for file (reply code %d).", client.getReplyCode());
      Log.e("TAG", msg);
      throw new IOException(msg);
    }
  }


  public void close() throws IOException
  {
    if(mIs != null)
    {
      mIs.close();
      mIs = null;
    }
  }



  public FTPFile[] listFiles(String relPath) throws IOException {
    try {
      return getFTPClient().listFiles(relPath);
    } catch (FTPConnectionClosedException e) {
      disconnect();
      return getFTPClient().listFiles(relPath);
    }
  }

  public void disconnect() throws IOException {

    close();

    if (mFtpClient != null)
    {
      mFTPFile.disconnect();
      mFtpClient = null;
    }
  }

  private FTPClient getFTPClient() throws IOException {
    if (mFtpClient == null) {
      mFtpClient = mFTPFile.getFTPClient();
    }

    return mFtpClient;
  }

}