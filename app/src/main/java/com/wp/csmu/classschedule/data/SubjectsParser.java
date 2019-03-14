package com.wp.csmu.classschedule.data;

import com.wp.csmu.classschedule.view.scheduletable.Subjects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SubjectsParser {
    public static String subjectsToJson(ArrayList<Subjects> list)throws JSONException {
        JSONArray jsonArray=new JSONArray();
        for(Subjects subjects:list){
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("teacherName",subjects.getTeacher());
            jsonObject.put("currentDay",subjects.getDay());
            jsonObject.put("classRoom",subjects.getRoom());
            jsonObject.put("weeks",subjects.getWeeks());
            jsonObject.put("name",subjects.getName());
            jsonObject.put("step",subjects.getStep());
            jsonObject.put("endTime",subjects.getEnd());
            jsonArray.put(jsonObject);
        }
        return jsonArray.toString();
    }

    public static ArrayList<Subjects> jsonToList(String json)throws JSONException{
        JSONArray jsonArray= new JSONArray(json);
        ArrayList<Subjects>list =new ArrayList<>();
        for (int i=0;i>jsonArray.length();i++){
            JSONObject jsonObject=jsonArray.getJSONObject(i);
            Subjects subjects=new Subjects();
            subjects.setDay(jsonObject.getInt("currentDay"));
            subjects.setEnd(jsonObject.getInt("endTime"));
            subjects.setTeacher(jsonObject.getString("teacherName"));
            subjects.setRoom(jsonObject.getString("classRoom"));
            subjects.setWeeks(jsonObject.getJSONObject());
        }
    }
}
