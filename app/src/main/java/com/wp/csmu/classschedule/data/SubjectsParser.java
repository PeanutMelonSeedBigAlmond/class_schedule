package com.wp.csmu.classschedule.data;

import com.wp.csmu.classschedule.application.MyApplication;
import com.wp.csmu.classschedule.view.scheduletable.Subjects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class SubjectsParser {

    public static void writeSubjects(ArrayList<Subjects> list) throws JSONException, IOException {
        BufferedWriter bufferedWriter = null;
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(MyApplication.getContext().getExternalFilesDir("") + "/schedule.json")));
        for (Subjects subjects : list) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("teacherName", subjects.getTeacher());
            jsonObject.put("currentDay", subjects.getDay());
            jsonObject.put("classRoom", subjects.getRoom());
            JSONArray jsonArray1 = new JSONArray();
            for (Integer integer : subjects.getWeeks()) {
                jsonArray1.put(integer.intValue());
            }
            jsonObject.put("weeks", jsonArray1.toString());
            jsonObject.put("name", subjects.getName());
            jsonObject.put("step", subjects.getStep());
            jsonObject.put("endTime", subjects.getEnd());
            bufferedWriter.write(jsonObject.toString() + "\n");
        }
        bufferedWriter.close();
    }

    public static ArrayList<Subjects> readSubjects(String json) throws JSONException, IOException {
        BufferedReader bufferedReader = null;
        ArrayList<Subjects> list = new ArrayList<>();
        String str = "";
        bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(MyApplication.getContext().getExternalFilesDir("") + "/schedule.json")));
        while ((str = bufferedReader.readLine()) != null) {
            JSONObject jsonObject = new JSONObject(str);
            Subjects subjects = new Subjects();
            subjects.setDay(jsonObject.getInt("currentDay"));
            subjects.setEnd(jsonObject.getInt("endTime"));
            subjects.setTeacher(jsonObject.getString("teacherName"));
            subjects.setRoom(jsonObject.getString("classRoom"));
            JSONArray jsonArray1 = new JSONArray(jsonObject.getJSONArray("weeks"));
            ArrayList<Integer> list1 = new ArrayList<>();
            for (int j = 0; j < jsonArray1.length(); j++) {
                list1.add(jsonArray1.getInt(j));
            }
            subjects.setWeeks(list1);
            subjects.setName(jsonObject.getString("name"));
            subjects.setStep(jsonObject.getInt("step"));
            list.add(subjects);
        }
        return list;
    }
}
