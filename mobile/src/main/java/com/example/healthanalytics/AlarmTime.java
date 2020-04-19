package com.example.healthanalytics;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.IBinder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.preference.PreferenceManager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;

public class AlarmTime extends BroadcastReceiver
{
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    File file;
    private StorageReference mStorageReference;
    private String user;

    public AlarmTime()
    {
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = sharedPreferences.edit();
        mStorageReference = FirebaseStorage.getInstance().getReference("files");


        SimpleDateFormat currentDate = new SimpleDateFormat("dd/MM/yyyy");
        Date todayDate = new Date();
        String date = currentDate.format(todayDate);
        user = sharedPreferences.getString(context.getString(R.string.user),"");
        String minimum = sharedPreferences.getString(context.getString(R.string.minimum),"");
        String average  = sharedPreferences.getString(context.getString(R.string.average),"");
        String maximum = sharedPreferences.getString(context.getString(R.string.maximum),"");
        file = new File(context.getFilesDir(), user + ".csv");

        if(minimum.matches("") || maximum.matches("") || average.matches("") || minimum.matches("--") || maximum.matches("--") || average.matches("--"))
        {
            try {


             //   CSVReader reader = new CSVReader(new FileReader(file.getAbsoluteFile()));
              //  List<String[]> myEntries = reader.readAll();
                String[] data = { date, "0", "0","0" };
               /* myEntries.add(data);
                String[] entries =new String[myEntries.size()];

                for (int i = 0; i < myEntries.size(); i++) {
                    entries = myEntries.get(i);
                }*/

                CSVWriter writer = new CSVWriter(new FileWriter(file.getAbsoluteFile(),true));
                writer.writeNext(data);
                writer.flush();
                writer.close();

            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            upload();

        }
        else
            {

                try
                {
                  //  CSVReader reader = new CSVReader(new FileReader(file.getAbsoluteFile()));
                  //  List<String[]> myEntries = reader.readAll();
                    String[] data = { date, minimum, average,maximum };
                  /*  myEntries.add(data);
                    String[] entries =new String[myEntries.size()];

                    for (int i = 0; i < myEntries.size(); i++) {
                        entries = myEntries.get(i);
                    }*/

                    CSVWriter writer = new CSVWriter(new FileWriter(file.getAbsoluteFile(),true));
                    writer.writeNext(data);
                    writer.flush();
                    writer.close();

                    editor.putString(context.getString(R.string.minimum), "");
                    editor.commit();

                    editor.putString(context.getString(R.string.average), "");
                    editor.commit();

                    editor.putString(context.getString(R.string.maximum), "");
                    editor.commit();

                }


                catch (IOException e)
                {
                    e.printStackTrace();
                }
                upload();
        }


    }
    private void upload()
    {
        Uri f = Uri.fromFile(file);
        StorageReference storageReference = mStorageReference.child(user + ".csv");

        storageReference.putFile(f).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

            }
        });
    }
}
