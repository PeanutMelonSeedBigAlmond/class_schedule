package com.wp.csmu.classschedule.io;

import com.wp.csmu.classschedule.application.MyApplicationLike;
import com.wp.csmu.classschedule.view.scheduletable.Subjects;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;

public class IO {
    public static String scheduleFile = MyApplicationLike.getContext().getExternalFilesDir("") + "/schedule.dat";
    public static String backgroundImg = MyApplicationLike.getContext().getExternalFilesDir("") + "/background.bg";
    public static String verifyCodeImg = MyApplicationLike.getContext().getExternalFilesDir("") + "/verifyCode";

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
        inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(inputStreamReader);
        StringBuffer sb = new StringBuffer();
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

    public static void writeVerifyCodeImage(InputStream inputStream) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(new File(verifyCodeImg));
        byte[] buffer = new byte[1024];
        int len = -1;
        while ((len = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
        }
        outputStream.flush();
        outputStream.close();
    }
}
