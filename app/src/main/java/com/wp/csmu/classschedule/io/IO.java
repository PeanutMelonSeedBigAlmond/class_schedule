package com.wp.csmu.classschedule.io;

import com.wp.csmu.classschedule.application.MyApplication;
import com.wp.csmu.classschedule.view.scheduletable.Subjects;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

public class IO {
    public static String scheduleFile= MyApplication.getContext().getExternalFilesDir("")+"/schedule.dat";
    public static ArrayList<Subjects> readSchedule() throws IOException, ClassNotFoundException {
        ObjectInputStream objectInputStream=new ObjectInputStream(new FileInputStream(scheduleFile));
        ArrayList<Subjects>subjects=(ArrayList<Subjects>) objectInputStream.readObject();
        objectInputStream.close();
        return subjects;
    }

    public static void writeSchedule(ArrayList<Subjects> list)throws IOException{
        ObjectOutputStream objectOutputStream=new ObjectOutputStream(new PrintStream(scheduleFile));
        objectOutputStream.writeObject(list);
        objectOutputStream.close();
    }
}
