package me.sjva.sosoplayer.fragment;


import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

import me.sjva.sosoplayer.R;
import me.sjva.sosoplayer.widget.LicenseInfoDialog;

public class SettingsFragment  extends PreferenceFragmentCompat {
  public static final String KEY_PLAYER_CONTINUATION = "player_continuation";
  public static final String KEY_PLAYER_NEXTFILE_AUTOPLAY = "player_nextfile_auto_playback";
  public static final String KEY_PLAYER_KEEP_LAST_PLAYBACKSPEED = "player_keep_last_playbackspeed";
  public static final String KEY_PLAYER_USE_FFMPEG_PROGRESS = "soso_settings_summary_use_ffmpeg_progress";
  public static final String KEY_SUBTITLE_TEXTCOLOR = "subtitle_foregroundcolor";
  public static final String KEY_SUBTITLE_BGCOLOR = "subtitle_backgroundcolor";
  public static final String KEY_SUBTITLE_WINDOWCOLOR = "subtitle_windowcolor";
  public static final String KEY_SUBTITLE_EDGECOLOR = "subtitle_edgecolor";
  public static final String KEY_SUBTITLE_EDGETYPE= "subtitle_edgetype";
  public static final String KEY_INFO_LICENSE= "info_license";
  private ColorPickerPreference subtitleTextColorPreference;
  private ColorPickerPreference subtitleBGColorPreference;
  private ColorPickerPreference subtitleWindowColorPreference;
  private ListPreference subtitleEdgetypePreference;
  private ColorPickerPreference subtitleEdgeColorPreference;
  private Preference licensePreference;
  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    addPreferencesFromResource(R.xml.settings);
    SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
    PreferenceScreen prefScreen = getPreferenceScreen();

    subtitleTextColorPreference = prefScreen.findPreference(KEY_SUBTITLE_TEXTCOLOR);
    subtitleBGColorPreference = prefScreen.findPreference(KEY_SUBTITLE_BGCOLOR);
    subtitleWindowColorPreference= prefScreen.findPreference(KEY_SUBTITLE_WINDOWCOLOR);
    subtitleEdgetypePreference = prefScreen.findPreference(KEY_SUBTITLE_EDGETYPE);
    subtitleEdgeColorPreference = prefScreen.findPreference(KEY_SUBTITLE_EDGECOLOR);
    licensePreference = prefScreen.findPreference(KEY_INFO_LICENSE);
    licensePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        new LicenseInfoDialog(getActivity()).show();
        return false;
      }
    });

    subtitleTextColorPreference.setDefaultValue(sharedPreferences.getInt(KEY_SUBTITLE_TEXTCOLOR, Color.BLACK));
    subtitleBGColorPreference.setDefaultValue(sharedPreferences.getInt(KEY_SUBTITLE_BGCOLOR, Color.WHITE));
    subtitleWindowColorPreference.setDefaultValue(sharedPreferences.getInt(KEY_SUBTITLE_WINDOWCOLOR, Color.TRANSPARENT));
    subtitleEdgeColorPreference.setDefaultValue(sharedPreferences.getInt(KEY_SUBTITLE_EDGECOLOR, Color.WHITE));

    String edgeType = sharedPreferences.getString(KEY_SUBTITLE_EDGETYPE, "None");

    int prefIndex =  subtitleEdgetypePreference.findIndexOfValue(edgeType);
    if (prefIndex >= 0) {
      subtitleEdgetypePreference.setDefaultValue(subtitleEdgetypePreference.getEntries()[prefIndex]);
      subtitleEdgetypePreference.setSummary(subtitleEdgetypePreference.getEntries()[prefIndex]);
    }
    subtitleEdgetypePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
      @Override
      public boolean onPreferenceChange(Preference preference, Object newValue) {
        subtitleEdgetypePreference.setSummary((String)newValue);
        return false;
      }
    });
  }
}
