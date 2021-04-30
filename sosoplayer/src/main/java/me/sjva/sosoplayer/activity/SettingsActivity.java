package me.sjva.sosoplayer.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import me.sjva.sosoplayer.R;
import me.sjva.sosoplayer.fragment.SettingsFragment;

public class SettingsActivity  extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        try {
            getSupportFragmentManager().beginTransaction().replace(R.id.settings_framelayout, new SettingsFragment()).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
