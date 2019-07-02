package com.wp.csmu.classschedule.io;

import com.wp.csmu.classschedule.application.MyApplication;
import com.wp.csmu.classschedule.view.scheduletable.Subjects;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;

public class IO {
    public static String scheduleFile = MyApplication.getContext().getExternalFilesDir("") + "/schedule.dat";

    public static HashSet<Subjects> readSchedule() throws IOException, ClassNotFoundException {
        ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(scheduleFile));
        HashSet<Subjects> subjects = (HashSet<Subjects>) objectInputStream.readObject();
        objectInputStream.close();
        return subjects;
    }

    public static void writeSchedule(HashSet<Subjects> set) throws IOException {
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(new PrintStream(scheduleFile));
        objectOutputStream.writeObject(set);
        objectOutputStream.close();
    }

    public static String readString(InputStream inputStream) {
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(inputStreamReader);
        StringBuffer sb = new StringBuffer("");
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
