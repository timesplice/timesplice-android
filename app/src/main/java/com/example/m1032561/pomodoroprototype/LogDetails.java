package com.example.m1032561.pomodoroprototype;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.m1032561.pomodoroprototype.model.LogModel;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LogDetails extends AppCompatActivity {

    private TextView createdAtDateValue;
    private TextView createdAtTimeValue;
    private TextView actualDurationValue, elapsedDurationValue, rateDurationValue, breakDurationValue;
    private EditText noteValue;
    private RatingBar rateValue;
    private int mYear, mMonth, mDay, mHour, mMinute;
    private LogModel model;
    private Button updateButton, deleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_details);

        setTitle("Log Details");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setElevation(0);

        model = (LogModel) getIntent().getSerializableExtra("LogDetails");
        System.out.println("cameinto:"+model.getId());

        createdAtDateValue = (TextView) findViewById(R.id.created_at_date_value);
        createdAtTimeValue = (TextView) findViewById(R.id.created_at_time_value);
        actualDurationValue = (TextView) findViewById(R.id.actual_duration_value);
        elapsedDurationValue = (TextView) findViewById(R.id.elapsed_duration_value);
        rateDurationValue = (TextView) findViewById(R.id.rate_duration_value);
        breakDurationValue = (TextView) findViewById(R.id.break_duration_value);
        noteValue = (EditText) findViewById(R.id.note_value);
        rateValue = (RatingBar) findViewById(R.id.rate_value);
        updateButton = (Button) findViewById(R.id.update_button);
        deleteButton = (Button) findViewById(R.id.delete_button);

        populateData();

        createdAtDateValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popDatePicker();
            }
        });
        createdAtTimeValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popTimePicker();
            }
        });

        actualDurationValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popDurationPicker(actualDurationValue,"actual");
            }
        });
        elapsedDurationValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popDurationPicker(elapsedDurationValue,"elapsed");
            }
        });
        rateDurationValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popDurationPicker(rateDurationValue,"rate");
            }
        });
        breakDurationValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popDurationPicker(breakDurationValue,"break");
            }
        });
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateRecord();
            }
        });
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteRecord();
            }
        });
    }

    private void updateRecord() {
        model.setRate(rateValue.getProgress()+"");
        model.setNote(noteValue.getText().toString());

        DBHelper dbHelper = new DBHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();

        cv.put("actual_dur", model.getActualDur());
        cv.put("ellapsed_dur", model.getEllapsedDur());
        cv.put("rate_dur", model.getRateDur());
        cv.put("rate",model.getRate() );
        cv.put("note", model.getNote());
        cv.put("breakTime", model.getBreakTime());
        //cv.put("created_at", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(model.getCreated_at()));
        cv.put("created_at",System.currentTimeMillis() + "");
        if (db.update(dbHelper.tablename, cv, "id=" + model.getId(), null) > 0) {
            Toast.makeText(getApplicationContext(),
                    "Successfully Updated", Toast.LENGTH_SHORT)
                    .show();
            finish();
        } else {
            Toast.makeText(getApplicationContext(),
                    "No Records to Update", Toast.LENGTH_SHORT)
                    .show();
        }
        db.close();
    }

    private void deleteRecord() {
        DBHelper dbHelper = new DBHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (db.delete(dbHelper.tablename, "id=" + model.getId(), null) > 0) {
            Toast.makeText(getApplicationContext(),
                    "Successfully Deleted", Toast.LENGTH_SHORT)
                    .show();
            finish();
        } else {
            Toast.makeText(getApplicationContext(),
                    "No Records to delete", Toast.LENGTH_SHORT)
                    .show();
        }
        db.close();
    }

    private void populateData() {
        try {
            createdAtDateValue.setText(new SimpleDateFormat("dd-MM-yyyy").format(model.getCreated_at()).toString());
            createdAtTimeValue.setText(new SimpleDateFormat("HH:mm").format(model.getCreated_at()).toString());

            actualDurationValue.setText(getDurationText(model.getActualDur()));
            elapsedDurationValue.setText(getDurationText(model.getEllapsedDur()));
            rateDurationValue.setText(getDurationText(model.getRateDur()));
            breakDurationValue.setText(getDurationText(model.getBreakTime()));
            noteValue.setText(model.getNote());
            rateValue.setProgress(Integer.parseInt(model.getRate()));

            System.out.println("inside:" + model.getNote());
        } catch (NumberFormatException e) {
            Log.d("number format error:", e.toString());
        }

    }

    private String getDurationText(String actualDur) {
        long sec = Long.parseLong(actualDur);
        long min = sec / 60;
        return String.format("%02d:%02d", min / 60, min % 60);
    }

    private void popTimePicker() {
        final Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute) {

                        createdAtTimeValue.setText(hourOfDay + ":" + minute);
                        model.getCreated_at().setMinutes(minute);
                        model.getCreated_at().setHours(hourOfDay);
                    }
                }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    private void popDatePicker() {
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        createdAtDateValue.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                        model.getCreated_at().setDate(dayOfMonth);
                        model.getCreated_at().setMonth(monthOfYear);
                        model.getCreated_at().setYear(year);
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    private void popDurationPicker(final TextView presentView, final String viewType) {

        DurationPickerDialog datePickerDialog = new DurationPickerDialog(this,
                new DurationPickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        presentView.setText(hourOfDay + ":" + minute);
                        String toSet=hourOfDay*60*60+minute*60+"";
                        switch(viewType){
                            case "actual":
                                model.setActualDur(toSet);
                                break;
                            case "elapsed":
                                model.setEllapsedDur(toSet);
                                break;
                            case "rate":
                                model.setRateDur(toSet);
                                break;
                            case "break":
                                model.setBreakTime(toSet);
                                break;
                        }
                    }
                }, mHour, mMinute);
        datePickerDialog.show();
    }

    private class DurationPickerDialog extends TimePickerDialog {

        public DurationPickerDialog(Context context, int theme,
                                    OnTimeSetListener callBack, int hour, int minute) {
            super(context, theme, callBack, hour, minute, true);
            updateTitle(hour, minute);
        }

        public DurationPickerDialog(Context context, OnTimeSetListener callBack,
                                    int hour, int minute) {
            super(context, callBack, hour, minute, true);
            updateTitle(hour, minute);
        }

        @Override
        public void onTimeChanged(TimePicker view, int hour, int minute) {
            super.onTimeChanged(view, hour, minute);
            updateTitle(hour, minute);
        }

        public void updateTitle(int hour, int minute) {
            setTitle("Duration: " + hour + ":" + formatNumber(minute));
        }

        private String formatNumber(int number) {
            String result = "";
            if (number < 10) {
                result += "0";
            }
            result += number;

            return result;
        }
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
}
