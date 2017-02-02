package com.example.m1032561.pomodoroprototype;


import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    TextView textviewTime, textviewHeading;
    EditText editText;
    RatingBar seekBar;
    Button button;
    SharedPreferences sharedPreference;
    ProgressBar progressBar;
    CardView cardView;
    Ringtone ringtone;
    Uri ringtoneUri;
    boolean isVibrationEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("Pomodoro");
        textviewHeading = (TextView) findViewById(R.id.textview_heading);
        textviewTime = (TextView) findViewById(R.id.textview_time);
        editText = (EditText) findViewById(R.id.edittext);
        seekBar = (RatingBar) findViewById(R.id.seekbar);
        button = (Button) findViewById(R.id.button);
        cardView = (CardView) findViewById(R.id.card_view);
        sharedPreference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        setActualCountDownFromPreference(sharedPreference.getString("work_interval", ""));
        setVibration(sharedPreference.getBoolean("is_vibration_enabled", false));

        setRingtone(sharedPreference.getString("alarmtone_sound", String.valueOf(android.provider.Settings.System.DEFAULT_RINGTONE_URI)));

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleButtonClick();
            }
        });
        mHandler = new Handler();
    }

    private void setRingtone(String uri) {
        ringtoneUri = Uri.parse(uri);
        System.out.println("new ringtone:" + uri);
    }

    private void setVibration(boolean enabled) {
        isVibrationEnabled = enabled;
    }

    public static void setActualCountDownFromPreference(String work_interval) {
        System.out.println("actual duartion:" + work_interval);
        if (work_interval != null && work_interval.trim().length() > 0) {
            try {
                actualCountDownSec = (long) (Double.parseDouble(work_interval.trim()) * 60);
            } catch (Exception e) {
                //nothing to handle
                actualCountDownSec = 25 * 60;
            }
        } else {
            actualCountDownSec = 25 * 60;
        }
        System.out.println("actual count down:" + actualCountDownSec);
    }

    private void handleButtonClick() {
        switch (button.getText().toString().toLowerCase()) {
            case "start":
                start_timer();
                textviewHeading.setText("Count down:" + actualCountDownSec / 60 + " min");
                presentState = "done";
                break;
            case "done":
                textviewHeading.setText("Enter note");
                show_note_rate();
                presentState = "complete";
                break;
            case "complete":
                textviewHeading.setText("Break time");
                start_break();
                presentState = "finish";
                break;
            case "finish":
                textviewHeading.setText("Get going");
                stop_timer();
                presentState = "start";
                break;
        }
        button.setText(presentState.toUpperCase());
    }

    private Handler mHandler;
    private Runnable mRunnable;
    private long mSeconds;
    private String presentState;

    private static long actualCountDownSec = 0;
    private long ellapsedTime = 0, rateTime = 0, rate = 0, breakTime = 0;
    private String note = "";
    private long id = 0;

    private void start_timer() {

        if (mHandler != null && mRunnable != null) {
            mHandler.removeCallbacks(mRunnable);
        }
        mSeconds = System.currentTimeMillis();
        //actualCountDownSec = 1 * 30;//need to make it generic

        progressBar.setVisibility(View.VISIBLE);
        textviewTime.setVisibility(View.VISIBLE);
        cardView.setVisibility(View.INVISIBLE);

        //converting long to int, error may come if seconds heigher than integer range
        progressBar.setMax((int) actualCountDownSec);
        mRunnable = new Runnable() {
            @Override
            public void run() {
                long seconds = 0;

                if (presentState.equals("done")) {
                    seconds = actualCountDownSec - (System.currentTimeMillis() - mSeconds) / 1000;
                } else {
                    seconds = (System.currentTimeMillis() - mSeconds) / 1000;
                }
                progressBar.setProgress((int) seconds);
                if (seconds < 0) {
                    if (ringtone == null) {
                        play_alarm_sound();
                    }
                    textviewTime.setTextColor(Color.RED);
                    seconds = -seconds;
                }else{
                    if (seconds == (int)(actualCountDownSec * (1.0 / 4))) {
                        //handleButtonClick();
                        playNotoficationSound();
                    }
                }
                System.out.println("Seconds " + seconds);
                textviewTime.setText(getDurationText(seconds));
                mHandler.postDelayed(mRunnable, 1000L);
            }
        };
        mHandler.postDelayed(mRunnable, 0L);
    }

    private void playNotoficationSound() {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone notificationSound;
        notificationSound = RingtoneManager.getRingtone(getApplicationContext(), notification);
        notificationSound.play();
        System.out.println("NOW PLAYING NOTIFICATION" + notification.getPath());
    }

    private String getDurationText(long sec) {
        long min = sec / 60;
        return String.format("%02d:%02d:%02d", min / 60, min % 60, sec % 60);
    }

    private void show_note_rate() {
        if (ringtone != null) {
            ringtone.stop();
            ringtone = null;
        }
        ellapsedTime = (System.currentTimeMillis() - mSeconds) / 1000;
        _insert_log();
        mSeconds = System.currentTimeMillis();
        progressBar.setVisibility(View.INVISIBLE);
        textviewTime.setVisibility(View.INVISIBLE);
        cardView.setVisibility(View.VISIBLE);
    }

    private void start_break() {
        rateTime = (System.currentTimeMillis() - mSeconds) / 1000;
        rate = seekBar.getProgress();
        note = editText.getText().toString().replaceAll("^", " ");
        _update_log();

        mSeconds = System.currentTimeMillis();
        mHandler.postDelayed(mRunnable, 0L);

        textviewTime.setTextColor(Color.GREEN);

        seekBar.setProgress(seekBar.getMax() / 2);
        textviewTime.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        cardView.setVisibility(View.INVISIBLE);

    }

    private void stop_timer() {
        breakTime = (System.currentTimeMillis() - mSeconds) / 1000;
        _update_log();
        _show_log();
        mHandler.removeCallbacks(mRunnable);
//        enableTimer.setEnabled(false);
        textviewTime.setTextColor(Color.BLACK);
        textviewTime.setText("00:00:00");
        progressBar.setProgress(0);
        progressBar.setVisibility(View.VISIBLE);
    }


    private void _insert_log() {
        DBHelper dbHelper = new DBHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("actual_dur", actualCountDownSec + "");
        cv.put("ellapsed_dur", ellapsedTime + "");
        cv.put("rate_dur", rateTime + "");
        cv.put("rate", rate + "");
        cv.put("note", note + "");
        cv.put("breakTime", breakTime + "");
        cv.put("created_at", System.currentTimeMillis() + "");

        id = db.insert(DBHelper.tablename, null, cv);


        /*Toast.makeText(MainActivity.this,
                "Inserted successfully", Toast.LENGTH_SHORT)
                .show();*/

        db.close();
    }

    private void _update_log() {
        DBHelper dbHelper = new DBHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("actual_dur", actualCountDownSec + "");
        cv.put("ellapsed_dur", ellapsedTime + "");
        cv.put("rate_dur", rateTime + "");
        cv.put("rate", rate + "");
        cv.put("note", note + "");
        cv.put("breakTime", breakTime + "");
        cv.put("created_at", System.currentTimeMillis() + "");

        db.update(DBHelper.tablename, cv, "id=" + id, null);
        db.close();

        /*Toast.makeText(MainActivity.this,
                "Updated successfully", Toast.LENGTH_SHORT)
                .show();*/
    }

    private void _show_log() {
        List<HashMap<String, String>> data = new DBHelper(getApplicationContext()).getFullLog();
        if (data.size() != 0) {   // if data is present do this
            for (HashMap<String, String> map : data) {
                Log.d("RECORD:", map.toString());
            }
            /*Toast.makeText(MainActivity.this,
                    "Records found:" + data.size(), Toast.LENGTH_SHORT)
                    .show();*/
        } else {  // if no data is present give the message no data in database.
            Toast.makeText(MainActivity.this,
                    "No data in database", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    public void play_alarm_sound() {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        System.out.println("rintone is:" + ringtoneUri);
        if (ringtone != null && ringtone.isPlaying())
            ringtone.stop();
        ringtone = RingtoneManager.getRingtone(getApplicationContext(), ringtoneUri);
        ringtone.play();
        if(isVibrationEnabled) {
            try{
                Vibrator v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                MediaPlayer mp = new MediaPlayer();
                mp.setDataSource(getApplicationContext(), notification);
                mp.prepare();
                v.vibrate(mp.getDuration());
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        System.out.println("NOW PLAYING" + ringtoneUri.getPath());
    }

    private void playSound(Context context, Uri alert) {
        MediaPlayer mMediaPlayer;
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(context, alert);
            final AudioManager audioManager = (AudioManager) context
                    .getSystemService(Context.AUDIO_SERVICE);
            if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            }
        } catch (IOException e) {
            System.out.println("OOPS");
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this, Settings.class));
        } else if (id == R.id.action_logs) {
            startActivity(new Intent(MainActivity.this, LogViewer.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        setActualCountDownFromPreference(sharedPreference.getString("work_interval", ""));
        setVibration(sharedPreference.getBoolean("is_vibration_enabled", false));
        setRingtone(sharedPreference.getString("alarmtone_sound", String.valueOf(android.provider.Settings.System.DEFAULT_RINGTONE_URI)));
        /*System.out.println("in onresume:" + ringtoneUri);
        play_alarm_sound();
        //playSound(this, ringtoneUri);
        //textviewHeading.setText("Count down:" + actualCountDownSec / 60 + " min");

        switch (button.getText().toString().toLowerCase()) {
            case "start":
                //textviewHeading.setText("Count down:" + actualCountDownSec / 60 + " min");
                break;
            case "done":
                start_timer();
                textviewHeading.setText("Count down:" + actualCountDownSec / 60 + " min");
                presentState = "done";
                break;
            case "complete":
                break;
            case "finish":
                break;
        }*/
    }

    @Override
    protected void onPause() {
        super.onPause();

        System.out.println("On Pause !!!!!!!!!!!!!!!!!");
    }

    @Override
    protected void onResume() {
        super.onResume();

        System.out.println("On Restart !!!!!!!!!!!!!!!!!");
    }
}
