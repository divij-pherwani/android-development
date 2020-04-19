package com.example.healthanalytics;


import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.channels.Channel;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class MonitorFragment extends Fragment {


    private View fragmentView;
    private TextView textMonitor;
    private TextView textMin;
    private TextView textAvg;
    private TextView textMax;
    private TextView textActivity;
    private TextView textStaus;

   private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private String userID;
    private LineChart chart;
    private Thread thread;
    private int val;
    private boolean plotData = true;

    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;

    private StorageReference mStorageReference;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private NotificationManagerCompat notificationManagerCompat;
   File file;

    public static final String CHANNEL_ID = "Channel";



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        Receiver messageReceiver = new Receiver();
        getActivity().registerReceiver(messageReceiver, messageFilter);
        createNotificationChannels();
        notificationManagerCompat = NotificationManagerCompat.from(getActivity());
    }
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notification Channel");

            NotificationManager manager = getActivity().getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_monitor, container, false);
        textMonitor = fragmentView.findViewById(R.id.textMonitor);
        textMin = fragmentView.findViewById(R.id.textMin);
        textAvg = fragmentView.findViewById(R.id.textAvg);
        textMax = fragmentView.findViewById(R.id.textMax);
        textActivity = fragmentView.findViewById(R.id.textActivity);
        textStaus = fragmentView.findViewById(R.id.textStatus);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        editor = sharedPreferences.edit();



        databaseReference = FirebaseDatabase.getInstance().getReference("USERS");
        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser current = mAuth.getCurrentUser();
        userID = current.getUid();
        file = new File(getActivity().getFilesDir(),userID + ".csv");

        mStorageReference = FirebaseStorage.getInstance().getReference("files");

        chart = fragmentView.findViewById(R.id.chart);

        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(false);
        chart.setPinchZoom(true);
        chart.setBackgroundColor(Color.WHITE);

        LineData data = new LineData();
        data.setValueTextColor(Color.RED);
        chart.setData(data);

        Legend l = chart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.RED);

        XAxis xl = chart.getXAxis();
        xl.setTextColor(Color.RED);
        xl.setDrawGridLines(true);
        xl.setAxisMinimum(1f);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(Color.RED);
        //leftAxis.setDrawGridLines(true);
        leftAxis.setLabelCount(8, true);
        leftAxis.setAxisMaximum(210f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);

        chart.getAxisLeft().setDrawGridLines(true);
        chart.getXAxis().setDrawGridLines(true);
        chart.setDrawBorders(true);

        feedMultiple();
        createFile();
        alarm();
        return fragmentView;
    }

    public void createFile()
    {
        Uri f = Uri.fromFile(file);
        StorageReference storageReference = mStorageReference.child(userID + ".csv");

        if(!file.exists())
            {
                storageReference.getFile(f).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot)
                    {

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (!file.exists()) {
                            try {
                                file.createNewFile();
                                CSVWriter writer = new CSVWriter(new FileWriter(file.getAbsoluteFile()));
                                String[] Header = "Date#Minimum#Average#Maximum".split("#");
                                writer.writeNext(Header);
                                writer.flush();
                                writer.close();
                                upload();
                            } catch (IOException x) {
                                x.printStackTrace();
                            }
                        }
                    }
                });


            }

    }
    public void save(String minimum, String average, String maximum)
    {


      /*  try
        {
            CSVReader reader = new CSVReader(new FileReader(file.getAbsoluteFile()));
            List<String[]> myEntries = reader.readAll();
            String[] data = { date, minimum, average,maximum };
            myEntries.add(data);
            String[] entries =new String[myEntries.size()];

            for (int i = 0; i < myEntries.size(); i++) {
                entries = myEntries.get(i);
            }

          CSVWriter writer = new CSVWriter(new FileWriter(file.getAbsoluteFile(),true));
            writer.writeNext(entries);
            writer.flush();
           writer.close();

        }


        catch (IOException e)
        {
            e.printStackTrace();
        }*/

        editor.putString(getString(R.string.minimum), minimum);
        editor.commit();

        editor.putString(getString(R.string.average), average);
        editor.commit();

        editor.putString(getString(R.string.maximum), maximum);
        editor.commit();

        editor.putString(getString(R.string.user), userID);
        editor.commit();



    }
    public void alarm()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 45);
        Intent intent = new Intent(getActivity(), AlarmTime.class);
        alarmIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, 0);
        alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null)
        {
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);
        }
    }

    public void read()
    {
      //  File file = new File(getActivity().getFilesDir(),"data.csv");
        String date = "";
        String min = "";
        String avg = "";
        String max = "";
        try {
            String line;
            String [] data;
            FileInputStream fis = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            while ((line = br.readLine()) != null) {

                data = line.split(",");
                date = data[0];
                min = data[1];
                avg = data[2];
                max = data[3];
             }
            br.close();
            Log.e("Date",date);
            Log.e("Min", min);

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    public void upload()
    {
            Uri f = Uri.fromFile(file);
             StorageReference storageReference = mStorageReference.child(userID + ".csv");

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
    private void addEntry() {

        LineData data = chart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(),  val), 0);
            data.notifyDataChanged();

            // let the chart know it's data has changed
            chart.notifyDataSetChanged();

            // limit the number of visible entries
            chart.setVisibleXRangeMaximum(15);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            chart.moveViewToX(data.getEntryCount());

        }
    }

    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "Live Heart Pulse Rate");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(3f);
        set.setColor(Color.BLUE);
        set.setHighlightEnabled(false);
        set.setDrawValues(true);
        set.setDrawCircles(true);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        return set;
    }

    private void feedMultiple() {

        if (thread != null){
            thread.interrupt();
        }

        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (true){
                    plotData = true;
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        thread.interrupt();

        super.onDestroy();

    }
    @Override
    public void onStop()
    {
        super.onStop();
    }
    @Override
    public void onPause() {
        if (thread != null) {
            thread.interrupt();
        }
        save(textMin.getText().toString(), textAvg.getText().toString(), textMax.getText().toString());
        super.onPause();

    }
/*
   public void register(){

        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yyyy");
        Date todayDate = new Date();
        String thisDate = currentDate.format(todayDate);
        String min = textMin.getText().toString();
        String max = textMax.getText().toString();
        String avg = textAvg.getText().toString();
        databaseReference.child(userID).child(thisDate);
        databaseReference.child(userID).child(thisDate + "/Minimum").setValue(min);
        databaseReference.child(userID).child(thisDate + "/Average").setValue(avg);
        databaseReference.child(userID).child(thisDate + "/Maximum").setValue(max);
       if (avg != "--")
       {
           save(min, avg, max);
           upload();
       }
    }*/


    public class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String msg = intent.getStringExtra("MY_KEY");
            if (msg.contains("Min:"))
            {
                String[] output = msg.split(":");
                String x = output[1];
                int y = Integer.valueOf(x);
                textMin.setText(String.valueOf(y));

            }
            else if (msg.contains("Max:"))
            {
                String[] output = msg.split(":");
                String x = output[1];
                int y = Integer.valueOf(x);
                textMax.setText(String.valueOf(y));
            }
            else if (msg.contains("Avg:"))
            {
                String[] output = msg.split(":");
                String x = output[1];
                int y = Integer.valueOf(x);
                textAvg.setText(String.valueOf(y));
            }
            else if (msg.contains("Activity:"))
            {
                String[] output = msg.split(":");
                String x = output[1];
                textActivity.setText(x);
                checkWarning(x);
            }
            else
            {
                textMonitor.setText(msg);
                val = Integer.valueOf(msg);
                if(plotData)
                {
                    addEntry();
                    plotData = false;
                }
                int x = val;
                checkDanger(x);
              //  register();
            }
        }
    }
    public void checkWarning(String x)
    {
        String y = textMonitor.getText().toString();
        int z = Integer.valueOf(y);
        if (x.matches("Driving Vehicle") || x.matches("On foot"))
        {
            if(z > 120 || z <60)
            {
                textStaus.setText("Not normal");
                textStaus.setTextColor(Color.RED);
            }
            else
            {
                textStaus.setText("Normal");
                textStaus.setTextColor(Color.GREEN);
            }
        }
        else if (x.matches("Riding Bicycle")||   x.matches("Running"))
    {
        if(z > 170 || z <60)
        {
            textStaus.setText("Not normal");
            textStaus.setTextColor(Color.RED);
        }
        else
        {
            textStaus.setText("Normal");
            textStaus.setTextColor(Color.GREEN);
        }
    }
          else if(x.matches("Still"))
        {
            if(z > 100 || z <60)
            {
                textStaus.setText("Not normal");
                textStaus.setTextColor(Color.RED);
            }
            else
            {
                textStaus.setText("Normal");
                textStaus.setTextColor(Color.GREEN);
            }
        }
          else
        {
            textStaus.setText("--");
        }
    }
    public void checkDanger (int x)
    {
        String temp = sharedPreferences.getString(getString(R.string.age),"");
        if(temp.matches ("")) {
        }
        else {
            int y = Integer.valueOf(temp);
            int z = 220 - y;
            if (x>z)
            {
                Notification notification = new NotificationCompat.Builder(getActivity(),CHANNEL_ID).setSmallIcon(R.drawable.ic_one)
                        .setContentTitle("High Heart Rate Alert").setContentText("Heart Rate is very high")
                        .setPriority(NotificationCompat.PRIORITY_LOW).setCategory(NotificationCompat.CATEGORY_MESSAGE).build();
                notificationManagerCompat.notify(1,notification);

            }
            if(x<40)
            {
                Notification notification = new NotificationCompat.Builder(getActivity(),CHANNEL_ID).setSmallIcon(R.drawable.ic_one)
                        .setContentTitle("Low Heart Rate Alert").setContentText("Heart Rate is very low")
                        .setPriority(NotificationCompat.PRIORITY_LOW).setCategory(NotificationCompat.CATEGORY_MESSAGE).build();
                notificationManagerCompat.notify(1,notification);

            }
        }
    }

}


