package com.wp.csmu.classschedule.activity;

import android.os.Bundle;
import android.widget.Toast;

import com.wp.csmu.classschedule.R;
import com.wp.csmu.classschedule.application.MyApplication;
import com.wp.csmu.classschedule.data.SubjectsParser;
import com.wp.csmu.classschedule.network.LoginHelper;
import com.wp.csmu.classschedule.view.scheduletable.Subjects;

import java.io.File;
import java.util.ArrayList;

import androidx.annotation.Nullable;

public class MainActivity extends BaseActivity {
    ArrayList<Subjects> subjects;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (checkSchedule()){

        }else {
            importSchedule();
        }

    }

    boolean checkSchedule() {
        File file = new File(MyApplication.getContext().getExternalFilesDir("") + "/schedule.json");
        return file.exists();
    }
    void importSchedule(){

    }
    void readSchedule(){
        try {
            subjects= LoginHelper.getSchedules();
            SubjectsParser.writeSubjects(subjects);
            Toast.makeText(getApplicationContext(), "导入成功", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
        }
    }
}