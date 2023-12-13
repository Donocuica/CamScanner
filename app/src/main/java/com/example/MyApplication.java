package com.example;

import android.app.Application;

import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.Locale;

public class MyApplication extends Application {
    @Override
    public void onCreate(){
        super.onCreate();
    }

    public  static  String formatTimestamp(long timestamp){
        Calendar calendar = Calendar.getInstance(new Locale("es", "PE"));
        calendar.setTimeInMillis(timestamp);


        String date = DateFormat.format("dd/MM/yyyy",calendar).toString();

        return  date;

    }

}
