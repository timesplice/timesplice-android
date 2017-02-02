package com.example.m1032561.pomodoroprototype;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Settings extends AppCompatPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    SharedPreferences sharedPreference;
    DBHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_settings);
        sharedPreference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        databaseHelper = new DBHelper(getApplicationContext());
        final EditTextPreference editTextPref = (EditTextPreference) findPreference("work_interval");
        Preference clearLog = (Preference) findPreference("clear_log");
        Preference download = (Preference) findPreference("download_log");
        Preference alarmPreference = (Preference) findPreference("alarmtone_sound");
        CheckBoxPreference vibration = (CheckBoxPreference) findPreference("is_vibration_enabled");
        clearLog.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                //open browser or intent here
                databaseHelper.deleteRecords();
                Toast.makeText(getApplicationContext(), "Log cleared", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        download.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                checkFileCreationPermission();
                return true;
            }
        });

        /*alarm.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                selectAlarmTone();
                return true;
            }
        });*/

        /*vibration.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                System.out.println( "Pref " + preference.getKey() + " changed to " + (Boolean)newValue);
                return true;
            }
        });*/
        alarmPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String stringValue = newValue.toString();
                System.out.println("##########" + stringValue);
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary("Silent");
                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));
                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary("nothing");
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }
                return true;
            }
        });
        editTextPref.setSummary(sharedPreference.getString("work_interval", "25") + " minutes");
    }

    private void checkFileCreationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(Settings.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            } else {
                createAndShareLogFile();
            }
        } else {
            createAndShareLogFile();
        }
    }

    private void createAndShareLogFile() {
        File newFile = createLogFile();
        Intent intentShareFile = new Intent(Intent.ACTION_SEND);
        if (newFile.exists()) {
            intentShareFile.setType("application/pdf");
            intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + newFile.getAbsolutePath()));
            intentShareFile.putExtra(Intent.EXTRA_SUBJECT, "Pomodoro Log on: " + new SimpleDateFormat("dd-MMM-yyyy HH:mm").format(new Date()));
            intentShareFile.putExtra(Intent.EXTRA_TEXT, "Attached Pomodoro Log on:" + new SimpleDateFormat("dd-MMM-yyyy HH:mm").format(new Date()));
            startActivity(Intent.createChooser(intentShareFile, "Share File"));
        }
        Toast.makeText(getApplicationContext(), "Shared successfully", Toast.LENGTH_SHORT).show();
    }

    private void selectAlarmTone() {


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    createAndShareLogFile();
                } else {
                    Toast.makeText(getApplicationContext(), "You denied access to create file", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    private File createLogFile() {
        String filename = "PomodoroLog.csv";
        String filepath = "Pomodoro";
        File f = new File(getExternalFilesDir(filepath), filename);
        return writeDataIntoFile(f);
    }

    private File writeDataIntoFile(File f) {
        ArrayList<HashMap<String, String>> dbData = databaseHelper.getFullLog();
        if (dbData.size() > 0) {
            StringBuilder fullLog = new StringBuilder("");
            fullLog.append("sep=^\n");
            Set<String> columnNames = dbData.get(0).keySet();
            for (String colName : columnNames) {
                fullLog.append(colName + "^");
            }
            fullLog.append("\n");
            for (Map<String, String> row : dbData) {
                for (String colName : columnNames) {
                    if (colName.equals("created_at")) {
                        fullLog.append(new Date(Long.parseLong(row.get(colName))) + "^");
                    } else if (colName.endsWith("_dur")) {
                        fullLog.append(getDurationText(row.get(colName)) + "^");
                    } else {
                        fullLog.append(row.get(colName) + "^");
                    }
                }
                fullLog.append("\n");
            }
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(f);
                out.write(fullLog.toString().getBytes());
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Unable to create a new file", Toast.LENGTH_SHORT).show();
                return null;
            }
            System.out.println("file creation success");
            return f;
        } else {
            System.out.println("no records in database");
            Toast.makeText(getApplicationContext(), "Nothing to share", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private String getDurationText(String actualDur) {
        long sec = Long.parseLong(actualDur);
        long min = sec / 60;
        return String.format("%02d:%02d", min / 60, min % 60);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePrefSummary(findPreference(key));
    }

    private void updatePrefSummary(Preference preference) {
        System.out.println("Inside preference changed");
        if (preference.getKey().equals("work_interval")) {
            EditTextPreference editTextPref = (EditTextPreference) preference;
            preference.setSummary(editTextPref.getText() + " minutes");
        } else if (preference.getKey().equals("is_vibration_enabled")) {
            System.out.println("vibration status:" + ((CheckBoxPreference) preference).isChecked());
        }
    }
}
