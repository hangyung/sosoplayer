package com.google.android.exoplayer2.ext.soso.text.smi;

import android.text.TextUtils;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.SimpleSubtitleDecoder;
import com.google.android.exoplayer2.text.Subtitle;
import com.google.android.exoplayer2.text.SubtitleDecoderException;


import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.LongArray;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Pattern;

public final class SmiDecoder extends SimpleSubtitleDecoder {
  private static int CLEARTIME = 7000;
  private static final String TAG = "SmiDecoder";
  private static final String DEFAULT_ENCODING = "Default";
  private String encoding;
  public SmiDecoder() {
    this(DEFAULT_ENCODING);
  }
  public SmiDecoder(String encoding) {
    super(TAG);
    this.encoding = encoding;
  }

  private String replaceFontTag(String s) // 폰트 색상이 겹쳐서 적용 안되니. 겹쳐서 되게 수정
  {

    if(s.contains("<font")== false)
      return s;

    String message[] =  s.split("<font");
    if(message == null)
      return s;


    String outMessage = "";
    for(int i = 0 ; i < message.length; i++) {
      if(message[i] == null)
        continue;

      boolean bStartFont = false;

      if(i != 0)
      {
        outMessage += "<font" + message[i];
        bStartFont = true;
      }
      else
      {
        outMessage += message[i];
      }


      if(bStartFont && message[i].contains("</font>") == false)
      {
        outMessage +=  "</font>";
      }
    }

    return outMessage;
  }


  private String removeMessage(String message, String startMessage, String endMessage)
  {
    String outMessage = "";
    String[] lines =  message.split(startMessage);

    for(int i = 0 ; i < lines.length; i++)
    {
      int lastIndex = lines[i].indexOf(endMessage);
      if(lastIndex > 0)
        outMessage += lines[i].substring(lastIndex + endMessage.length());
      else
        outMessage += lines[i];
    }

    return outMessage;
  }

  private String addEndTag(String message)
  {

    if(message.length() <= 1){
      return "";
    }

    String outMessage =  "";
    int lastIndex = message.indexOf(" ");
    if(lastIndex < 0) {
      if(message.length() > 1) {
        return "</" + message.substring(1, message.length());
      }
      return "";
    }
    outMessage =  "</" + message.substring(1, lastIndex) + ">";

    // Log.e("test", outMessage);
    return outMessage;
  }

  boolean isEuckrEncoding(byte[] buffer)
  {
    boolean isEuckrEncoding = false;


    ByteArrayInputStream bais1 = null;
    ByteArrayInputStream bais2 = null;
    InputStreamReader isReaderUTF8 = null;
    InputStreamReader isReaderEUCKR = null;
    BufferedReader bufReaderUTF8  = null;
    BufferedReader bufReaderEUCKR = null;
    try
    {
      bais1 = new ByteArrayInputStream(buffer);
      bais2 = new ByteArrayInputStream(buffer);

      //InputStreamReader isReaderUTF8 = new InputStreamReader(fis1, "utf-8");
      isReaderUTF8 = new InputStreamReader(bais1, "8859-1");
      isReaderEUCKR = new InputStreamReader(bais2, "euc-kr");

      bufReaderUTF8 = new BufferedReader(isReaderUTF8);
      bufReaderEUCKR = new BufferedReader(isReaderEUCKR);

      String utfLine = "";
      String euckrLine = "";

      for (int i = 0; i <10000; i++)
      {
        utfLine = bufReaderUTF8.readLine();
        euckrLine = bufReaderEUCKR.readLine();

        if (euckrLine != null) {
	            	/*
		            if (utfLine.getBytes("utf-8").length == getEncodedLength(utfLine) &&
		                utfLine.getBytes("utf-8").length == euckrLine.getBytes("euc-kr").length)
		            {
		            	// UTF-8
		            }
		            else
		            {
		            	// EUC-KR
		            	isEuckrEncoding = true;
		            }
		            */
          byte[] b = utfLine.getBytes("8859-1");
          CharsetDecoder decoder
              = Charset.forName("UTF-8").newDecoder();
          try {
            CharBuffer r = decoder.decode(ByteBuffer.wrap(b));
            isEuckrEncoding = false;
          } catch (CharacterCodingException e) {
            isEuckrEncoding = true;
            break;
          }
        }
        else
        {
          if (utfLine == null) break;
        }
      }

    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    finally
    {
      try { if(bufReaderEUCKR != null)    bufReaderEUCKR.close();}    catch (IOException e) {}
      try { if(bufReaderUTF8 != null)     bufReaderUTF8.close();}     catch (IOException e) {}
      try { if(isReaderUTF8 != null)      isReaderUTF8.close();}      catch (IOException e) {}
      try { if(isReaderEUCKR != null)     isReaderEUCKR.close();}     catch (IOException e) {}
      try { if(bais1 != null)              bais1.close();}              catch (IOException e) {}
      try { if(bais2 != null)              bais2.close();}              catch (IOException e) {}
    }
    return isEuckrEncoding;
  }

  private String detectEncoding(byte[] bytes, int length) {
    String curEncoding = encoding;
    Charset guessedCharset = null;
    try {
      guessedCharset = CharsetToolkit.guessEncoding(bytes, length);
      if (!guessedCharset.name().toLowerCase().equals("utf-8")) {
        curEncoding = guessedCharset.name();
      } else {
        Locale locale = Locale.getDefault();
        if (locale.toString().equals("ko_KR")) {
          boolean euckrEncoding = isEuckrEncoding(bytes);
          if (euckrEncoding) {
            curEncoding = "euc-kr";
          } else {
            curEncoding = "utf-8";
          }
        } else {
          curEncoding = "utf-8";
        }
      }

    } catch (IOException e) {
      e.printStackTrace();
      curEncoding = "utf-8";
    }
    return curEncoding;
  }

  public class SubtitleDataSet{
    long timeUs;
    String message;

    public SubtitleDataSet(long timeMs, String message) {
      this.timeUs = timeMs * 1000;
      this.message = message;
    }
  }
  private void setFirstClassTag(Hashtable<String, Vector<SubtitleDataSet>> titleTable , String message)
  {
    int startIndex = message.indexOf("<p class");
    if(startIndex == -1)
    {
      titleTable.clear();
      Vector<SubtitleDataSet> tempVector = new Vector<SubtitleDataSet>();
      titleTable.put("default", tempVector);
      return;
    }
    startIndex = message.indexOf("=", startIndex);
    int endIndex = message.indexOf(">", startIndex);

    String classTag = message.substring(startIndex + 1, endIndex);
    if(classTag != null && classTag.length() > 0)
    {
      titleTable.clear();
      Vector<SubtitleDataSet> tempVector = new Vector<SubtitleDataSet>();
      titleTable.put(classTag, tempVector);
    }
  }

  @Override
  protected Subtitle decode(byte[] bytes, int length, boolean reset) throws SubtitleDecoderException {

    int offset = 0; // bitmask 제거
    if((bytes[0] == 0xff && bytes[1] == 0xfe)
        ||(bytes[0] == 0xfe && bytes[1] == 0xff))  {
      offset = 2;
    }
    if (encoding.equals(DEFAULT_ENCODING)) {
      encoding = detectEncoding(bytes, length);
    }

    String smi = null;
    try {
      smi= new String(bytes, offset, bytes.length, encoding);
    } catch (UnsupportedEncodingException e) {
      throw new SubtitleDecoderException(e);
    }

    Hashtable<String, Vector<SubtitleDataSet>> titleTable = new Hashtable<String, Vector<SubtitleDataSet>>();
    String classTag = "default";
    String outMessage= "";
    int pretime =0;
    int time = 0;
    try {
      String ss = removeMessage(smi, "<!--", "-->" );
      ss = ss.replace("\r", "");
      StringBuffer logMessage = new StringBuffer();
      String[] lines = ss.split("\n");
      String endTag ="";
      setFirstClassTag(titleTable, ss.toLowerCase());
      for(int i = 0; i < lines.length; i++)  {
        String[] tags = splitTag(lines[i]);

        if(tags == null)
          continue;

        for(int j =0 ; j < tags.length ; j++) {
          if(tags[j].trim().startsWith("<") == true)  {
            String tag = tags[j].toLowerCase().trim();
            tag = tag.replace("\"", "");
            tag = tag.replace("\'", "");
            if(tag.contains("<sync start") == true)  {
              if(logMessage.length() > 0)
              {
                Vector<SubtitleDataSet> tempVector = titleTable.get(classTag);
                if(tempVector != null)
                {
                  if(pretime != 0 && pretime + CLEARTIME < time)
                  {
                    tempVector.add(new SubtitleDataSet(pretime + CLEARTIME, null));
                  }
                  logMessage.append(endTag);
                  endTag ="";

                  try
                  {
                    String fontTag = replaceFontTag(logMessage.toString());
                    tempVector.add(new SubtitleDataSet(time, fontTag));
                  }catch(Exception e)
                  {
                    tempVector.add(new SubtitleDataSet(time, logMessage.toString()));
                  }

                  pretime = time;
                }
              }


              // 시간 가져오기
              try
              {
                String timeString = tag.toLowerCase().substring(tag.indexOf("=") + 1, tag.length() -1).trim();

                int endIndex = timeString.indexOf("end");
                if(endIndex > 0)
                  timeString = timeString.substring(0, endIndex).trim();

                if(timeString.endsWith("ms")) {
                  timeString = timeString.substring(0, timeString.length() - 2);
                }
                int curtime = Integer.parseInt(timeString);
                time = curtime;
              }catch(Exception e) {
                time += 500;
              }


              logMessage = new StringBuffer();    // reset buffer
            }
            else if(tag.contains("<p class") == true)  {
              // 클래스 가져오기
              try
              {
                int index = tag.indexOf("=");
                if(index > 0)
                {
                  classTag = tag.toLowerCase().substring(index + 1, tag.length() -1).trim();
                }
                else
                {
                  classTag = "default";
                }

                Vector<SubtitleDataSet> tempVector = (Vector<SubtitleDataSet>)titleTable.get(classTag);
                if(tempVector == null)
                {
                  tempVector = new Vector<SubtitleDataSet>();
                  titleTable.put(classTag, tempVector);
                }
              }catch(Exception e){}
            }
            else if(tag.contains("<smi>") == true
                || tag.contains("<sami>") == true
                || tag.contains("</sami>") == true
                || tag.contains("<head>") == true
                || tag.contains("</head>") == true
                || tag.contains("<title>") == true
                || tag.contains("</title>") == true
                || tag.contains("<body>") == true
                || tag.contains("</body>") == true
                || tag.contains("<style") == true
                || tag.contains("</style>") == true
                || tag.contains("<p>") == true )
            {
              break; // next tag
            }
            else if(tag.contains("<font") == true)
            {

              try
              {
                tag = tag.replace("\"", "");
                int index = tag.indexOf("color=");
                if(index > 0) {
                  int colorLentgth = "color=".length();
                  String colorTag = "";
                  int index2 = tag.lastIndexOf(" ", index + colorLentgth);
                  if(index2 > 0 && index2 > index + colorLentgth)
                  {
                    colorTag = tag.substring(index + colorLentgth, index2 -1).trim();
                  }
                  else
                  {
                    colorTag = tag.substring(index + colorLentgth, tag.length() -1).trim();
                  }

                  HtmlColor color = HtmlColor.fromString(colorTag);
                  if(color != null)  {
                    tag = tag.replace(colorTag, "#" +  Integer.toHexString(color.getInt()));
                  } else {
                    boolean bInt = Pattern.matches("^[0-9]*", colorTag);
                    if(bInt == false) {
                      if(colorTag.startsWith("#") == false){
                        tag = tag.replace(colorTag, "#" +  colorTag);
                      }
                    }
                  }
                  logMessage.append(tag);
                  // endTag += "</font>";//addEndTag(tag);
                }
              }catch(Exception e){}
            }
            else if(tag.contains("<br>") == true ||
                tag.contains("</p>") == true ) {
              logMessage.append("<br>");
            }
            else {
              logMessage.append(tag);
              endTag += addEndTag(tag);
            }
          }
          else {
            logMessage.append(tags[j]);
            //   logMessage += endTag;
            //  allEndTag += endTag;
            //     Log.e("test", tags[j] + endTag);
          }
        }
      }

      logMessage.append(endTag);

      Vector<SubtitleDataSet> tempVector = titleTable.get(classTag);
      if(tempVector != null)
      {
        if(pretime != 0 && pretime + CLEARTIME < time)
        {
          tempVector.add(new SubtitleDataSet(pretime + CLEARTIME, ""));
        }

        try
        {
          String fontTag = replaceFontTag(logMessage.toString());
          tempVector.add(new SubtitleDataSet(time, fontTag));
        }catch(Exception e)
        {
          tempVector.add(new SubtitleDataSet(time, logMessage.toString()));
        }

      }
      //      Log.e("test", "classTag : "+ classTag + " Time : "  +  time  + ", " + logMessage);
      titleTable.put(classTag, tempVector);
    } catch (Exception e) {
//            e.printStackTrace();
//            Log.e("test", e.toString());
    }

    Set<String> keys = titleTable.keySet();
    Iterator<String> itr = keys.iterator();
    String defaultTag = itr.next();

    Vector<SubtitleDataSet> subtitleDataSets = titleTable.get(defaultTag);
    ArrayList<Cue> cues = new ArrayList<>();
    LongArray cueTimesUs = new LongArray();

    for (int i = 0; i < subtitleDataSets.size(); i++) {
      String message = null;
      try {
        SubtitleDataSet subtitleDataSet = subtitleDataSets.get(i);
        if(TextUtils.isEmpty(subtitleDataSet.message)){
          message = "";
        } else {
          message = subtitleDataSet.message;
        }
        cues.add(new Cue(message));
        cueTimesUs.add(subtitleDataSet.timeUs);
      }catch (Exception e) {
        e.printStackTrace();
      }

    }

    Cue[] cuesArray = cues.toArray(new Cue[0]);
    long[] cueTimesUsArray = cueTimesUs.toArray();
    return new SmiSubtitle(cuesArray, cueTimesUsArray);
  }

  private String[] splitTag(String message)
  {
    Vector<String> tempVector = new Vector<String> ();
    StringBuffer temp = new StringBuffer();

    int index = 0;
    boolean bFirst = false;

    for(int i = 0 ; i < message.length(); i++)
    {
      if(message.charAt(i) == '<') {
        if(bFirst == true) {
          tempVector.add(temp.toString());
          temp = new StringBuffer();  // reset
          index++;
        }
        else  {
          bFirst = true;
        }
        temp.append(message.charAt(i));
      }
      else if(message.charAt(i) == '>') {
        temp.append(message.charAt(i));
        index++;
        tempVector.add(temp.toString());
        temp = new StringBuffer();  // reset
      }
      else  {
        temp.append(message.charAt(i));
      }

    }
    if(temp.length() > 0) {
      tempVector.add(temp.toString());
      temp = new StringBuffer();  // reset
    }

    String[] outMessage = new String[tempVector.size()];
    for(int i = 0; i< tempVector.size(); i++) {
      outMessage[i] = tempVector.get(i);
    }

    return outMessage;
  }
}
