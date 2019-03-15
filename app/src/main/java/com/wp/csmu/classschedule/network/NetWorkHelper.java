package com.wp.csmu.classschedule.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.wp.csmu.classschedule.application.MyApplication;
import com.wp.csmu.classschedule.view.bean.Score;
import com.wp.csmu.classschedule.view.scheduletable.Subjects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NetWorkHelper {
    public static String token = "";
    public static int weekIndex = 0;
    public static String endTime = "";
    public static String startTime = "";
    public static String termName = "";
    public static List<Subjects> subjects = new ArrayList<>();
    private static OkHttpClient client = new OkHttpClient();

    public static void login(String account, String password) throws Exception {
        String url = "http://jiaowu.csmu.edu.cn:8099/app.do?method=authUser&xh=" + account + "&pwd=" + password;
        Request request = new Request.Builder().url(url).get().build();
        Response response = client.newCall(request).execute();
        JSONObject jsonObject = new JSONObject(response.body().string());
        if (jsonObject.getBoolean("success")) {
            token = jsonObject.getString("token");
            SharedPreferences sharedPreferences = MyApplication.getContext().getSharedPreferences("user", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("account", account);
            editor.putString("password", password);
            editor.putString("token", token);
            editor.putString("userName", jsonObject.getString("userrealname"));
            editor.putString("userCollege", jsonObject.getString("userdwmc"));
            editor.apply();
        } else {
            throw new IllegalStateException("用户名或密码错误");
        }
    }

    private static int getCurrentWeek() throws Exception {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String url = "http://jiaowu.csmu.edu.cn:8099/app.do?method=getCurrentTime&currDate=" + simpleDateFormat.format(date);
        Request request = new Request.Builder().url(url).addHeader("token", token).get().build();
        Response response = client.newCall(request).execute();
        JSONObject jsonObject = new JSONObject(response.body().string());
        try {
            weekIndex = jsonObject.getInt("zc");
            endTime = jsonObject.getString("e_time");
            startTime = jsonObject.getString("s_time");
            termName = jsonObject.getString("xnxqh");
        } catch (JSONException e) {
            Log.i("getCurrentWeek", "getCurrentWeek: " + e.getMessage());
            if ("-1".equals(jsonObject.getString("token"))) {
                SharedPreferences sharedPreferences = MyApplication.getContext().getSharedPreferences("user", Context.MODE_PRIVATE);
                String account = sharedPreferences.getString("account", null);
                String password = sharedPreferences.getString("password", null);
                if (account != null && password != null) {
                    login(account, password);
                    getCurrentWeek();
                }
            }
        }
        return weekIndex;
    }

    public synchronized static void getSchedule(String account, int weekIndex) throws Exception {
        subjects.clear();
        NetWorkHelper.weekIndex = ((weekIndex == -1) ? getCurrentWeek() : weekIndex);
        String url = "http://jiaowu.csmu.edu.cn:8099/app.do?method=getKbcxAzc&xh=" + account + "&xnxqid=" + termName + "&zc=" + NetWorkHelper.weekIndex;
        Request request = new Request.Builder().url(url).addHeader("token", token).get().build();
        Response response = client.newCall(request).execute();
        JSONArray jsonArray = new JSONArray(response.body().string());
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String teacher = jsonObject.getString("jsxm");
            String room = jsonObject.getString("jsmc");
            String time = jsonObject.getString("kcsj");
            String name = jsonObject.getString("kcmc");
            int day = Integer.valueOf(time.substring(0, 1));
            int step = time.substring(1).length() / 2;
            int start = Integer.valueOf(time.substring(1, 3));
            Subjects subjects = new Subjects();
            subjects.setDay(day);
            subjects.setName(name);
            subjects.setRoom(room);
            subjects.setStart(start);
            subjects.setStep(step);
            subjects.setTeacher(teacher);

            NetWorkHelper.subjects.add(subjects);
        }
    }

    public synchronized static String[] getHomepage(String account) throws Exception {
        if (token.equals("")){
            SharedPreferences sharedPreferences=MyApplication.getContext().getSharedPreferences("user",Context.MODE_PRIVATE);
            login(account,sharedPreferences.getString("password",""));
        }
        String[] info = new String[12];
        FormBody formBody = new FormBody.Builder().add("userAccountType", "2").build();
        Request request = new Request.Builder().url("http://jiaowu.csmu.edu.cn:8099/app.do?method=getUserInfo&xh=" + account)
                .addHeader("token", token).post(formBody).build();
        Response response = client.newCall(request).execute();
        try {
            JSONObject jsonObject = new JSONObject(response.body().string());
            info[0] = jsonObject.getString("xm");
            info[1] = jsonObject.getString("xh");
            info[2] = jsonObject.getString("xb");
            info[3] = jsonObject.getString("dh");
            info[4] = jsonObject.getString("qq");
            info[5] = jsonObject.getString("email");
            info[6] = jsonObject.getString("yxmc");
            info[7] = jsonObject.getString("zymc");
            info[8] = jsonObject.getString("bj");
            info[9] = jsonObject.getString("rxnf");
            info[10] = jsonObject.getString("nj");
            info[11] = jsonObject.getString("fxzy");
        }catch (JSONException e){
            if (e.toString().contains("no value for")){
                SharedPreferences sharedPreferences=MyApplication.getContext().getSharedPreferences("user",Context.MODE_PRIVATE);
                login(account,sharedPreferences.getString("password",""));
                info=getHomepage(account);
            }
        }
        return info;
    }

    public synchronized static List<Score> getScore(String account,String selectedTerm) throws Exception {
        if (token.equals("")){
            SharedPreferences sharedPreferences=MyApplication.getContext().getSharedPreferences("user",Context.MODE_PRIVATE);
            login(account,sharedPreferences.getString("password",""));
        }
        List<Score>list=new ArrayList<>();
        Request request=new Request.Builder().url("http://jiaowu.csmu.edu.cn:8099/app.do?method=getCjcx&xh="+account+"&xnxqid="+selectedTerm)
                .addHeader("token",token).get().build();
        Response response=client.newCall(request).execute();
        JSONObject jsonObject=new JSONObject(response.body().string());
        JSONArray jsonArray=jsonObject.getJSONArray("result");
        try {
            for(int i=0;i<jsonArray.length();i++){
                JSONObject object=jsonArray.getJSONObject(i);
                Score score=new Score();
                score.setScore(object.getString("zcj"));
                score.setCredit(object.getInt("xf"));
                score.setSubjectEnglish(object.getString("kcywmc"));
                score.setSubjectAttribute(object.getString("kclbmc"));
                score.setTerm(object.getString("xqmc"));
                score.setSubject(object.getString("kcmc"));
                score.setExamAttribute(object.getString("ksxzmc"));
                score.setSubjectNature(object.getString("kcxzmc"));
                score.setNote(object.getString("bz"));
                list.add(score);
            }
        }catch (JSONException e){
            if (e.toString().contains("no value for")){
                SharedPreferences sharedPreferences=MyApplication.getContext().getSharedPreferences("user",Context.MODE_PRIVATE);
                login(account,sharedPreferences.getString("password",""));
               list=getScore(account,selectedTerm);
            }
        }
        return list;
    }

    public static JSONArray getTerms(String account) throws Exception {
        Request request=new Request.Builder().url("http://jiaowu.csmu.edu.cn:8099/app.do?method=getXnxq&xh="+account)
                .addHeader("token",token).get().build();
        Response response=client.newCall(request).execute();
        return new JSONArray(response.body().string());
    }
}
