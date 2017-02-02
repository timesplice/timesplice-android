package com.example.m1032561.pomodoroprototype;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.example.m1032561.pomodoroprototype.adapter.LogViewerAdapter;
import com.example.m1032561.pomodoroprototype.model.LogModel;

import java.util.ArrayList;
import java.util.List;

public class LogViewer extends AppCompatActivity implements LogViewerAdapter.OnDataClickListener {

    private RecyclerView recyclerView;
    private LogViewerAdapter logViewerAdapter;
    List<LogModel> logList = new ArrayList<LogModel>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_viewer);

        setTitle("Log Viewer");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.log_viewer);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        logViewerAdapter = new LogViewerAdapter(logList, this);
        recyclerView.setAdapter(logViewerAdapter);
        //loadLogViewer();

    }

    private void loadLogViewer() {
        DBHelper dbHelper = new DBHelper(getApplicationContext());
        List<LogModel> tmpLogList = dbHelper.getLogModelList();
        for (LogModel model : tmpLogList) {
            System.out.println(model.getId() + ":" + model.getEllapsedDur()+":"+ model.getActualDur() + ":"  +model.getNote());
        }
        logList.clear();
        for (LogModel tmpModel : tmpLogList) {
            logList.add(tmpModel);
            //logViewerAdapter.notifyDataSetChanged();
        }
        System.out.println("entered into logviewer:" + logList.size());
        logViewerAdapter.notifyDataSetChanged();

    }


    @Override
    public void onDataClick(LogModel log) {
        System.out.println("clicked:" + log.getId());
        Intent i = new Intent(LogViewer.this, LogDetails.class);
        i.putExtra("LogDetails", log);
        startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_log_viewer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                break;
            case R.id.action_settings:
                startActivity(new Intent(LogViewer.this, Settings.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadLogViewer();
    }

}
