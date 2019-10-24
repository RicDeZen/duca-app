package com.gov.ducadegliabruzzitreviso.ducaapp.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.gov.ducadegliabruzzitreviso.ducaapp.R;
import com.gov.ducadegliabruzzitreviso.ducaapp.classes.NotifWorker;

import java.util.concurrent.TimeUnit;

public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private String url_sito;
    private String url_circolari;
    private String file_circolari;
    private Context context;
    private SharedPreferences sharedPreferences;
    private SettingsFragment settingsFragment = new SettingsFragment();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        context = this.getApplicationContext();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getFragmentManager().beginTransaction()
                .replace(R.id.Settings_content, settingsFragment)
                .commit();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        ImageButton imgb1 = (ImageButton) findViewById(R.id.imgButtonInfo);
        imgb1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment mFrag = new InfoDialogFragment();
                mFrag.show(getSupportFragmentManager(), "Info");
            }
        });
        url_sito = sharedPreferences.getString("pref_url_site","https://www.liceoduca.edu.it");
        url_circolari = sharedPreferences.getString("pref_url_circolari", "https://www.liceoduca.edu.it/studenti/circolari-studenti-anno-scolastico-2018-2019/");
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals("pref_notif")) {
            if (sharedPreferences.getBoolean(key, false)) {
                scheduleWork();
            } else {
                dismissWork();
            }
        }else{
            settingsFragment.fix();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(context).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(this);
    }

    private void scheduleWork(){
        Constraints.Builder cBuilder = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_ROAMING);
        PeriodicWorkRequest.Builder pBuilder = new PeriodicWorkRequest.Builder(NotifWorker.class, 3, TimeUnit.HOURS)
                .addTag("DucaNotif")
                .setInputData(createWorkData())
                .setConstraints(cBuilder.build());
        PeriodicWorkRequest pRequest = pBuilder.build();
        WorkManager.getInstance(context).enqueue(pRequest);
    }

    private void dismissWork(){
        WorkManager.getInstance(context).cancelAllWorkByTag("DucaNotif");
    }

    private Data createWorkData(){
        Data.Builder builder = new Data.Builder();
        builder.putString("url_feed", url_sito+"/feed/");
        builder.putString("url_circolari", url_circolari);
        builder.putString("data_path", getApplicationContext().getFilesDir() + "/DucaApp");
        return builder.build();
    }

    public static class SettingsFragment extends PreferenceFragment{
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            fix();
        }
        public void fix(){
            EditTextPreference pref;
            Preference p = findPreference("pref_url_site");
            if(p instanceof EditTextPreference) {
                pref = (EditTextPreference) p;
                pref.setSummary(pref.getText());
            }
            p = findPreference("pref_url_circolari");
            if(p instanceof EditTextPreference) {
                pref = (EditTextPreference) p;
                pref.setSummary(pref.getText());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_item:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.URL_sito)));
                startActivity(intent);
                return true;
            case android.R.id.home:
                onBackPressed();
            default: return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public static class InfoDialogFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            builder.setView(inflater.inflate(R.layout.custom_dialog_info, null));
            return builder.create();
        }
    }

    public void sendMail(View v){
        int id = v.getId();
        Intent mailIntent = new Intent(Intent.ACTION_VIEW);
        Uri data;
        switch(id){
            case R.id.textView_myMail:
                data = Uri.parse("mailto:riccardodezen98@gmail.com?");
                mailIntent.setData(data);
                break;
            case R.id.textView_bugreport:
                data = Uri.parse("mailto:riccardodezen98@gmail.com?subject=DucaApp bug report");
                mailIntent.setData(data);
                break;
        }
        startActivity(mailIntent);
    }
}
