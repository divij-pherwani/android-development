package com.example.healthanalytics;


import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import static androidx.constraintlayout.widget.Constraints.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class HistoryFragment extends Fragment {
    private View fragmentView;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private String userID;
    private StorageReference mStorageReference;
    File file;

    private Button week;
    private Button fortnight;
    private Button month;
    private Button downloadButton;
    List<String[]> lines = new ArrayList<>();
    private GraphView chart;

    public HistoryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_history, container, false);

        databaseReference = FirebaseDatabase.getInstance().getReference("USERS");
        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser current = mAuth.getCurrentUser();
        userID = current.getUid();

        file = new File(getActivity().getFilesDir(),userID + ".csv");
        mStorageReference = FirebaseStorage.getInstance().getReference("files");

        week = fragmentView.findViewById(R.id.Week);
        fortnight = fragmentView.findViewById(R.id.Fortnight);
        month = fragmentView.findViewById(R.id.Month);
        downloadButton = fragmentView.findViewById(R.id.downloadButton);



        chart = fragmentView.findViewById(R.id.lineChart);
        chart.getViewport().setYAxisBoundsManual(true);
        chart.getViewport().setMinY(0.0);
        chart.getViewport().setMaxY(210.0);
        chart.getViewport().setXAxisBoundsManual(true);
        chart.getViewport().setMinX(1.0);
        chart.getViewport().setMaxX(30.0);
        chart.getViewport().setScalableY(false);
        chart.getViewport().setScalable(false);
        chart.getViewport().setScrollable(false);
        chart.getViewport().setScrollableY(false);


        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                download();
            }
        });

        week.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                createWeekGraph(lines);
            }


        });

        fortnight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                createFortnightGraph(lines);
            }
        });

        month.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                createMonthGraph(lines);
            }
        });


        return fragmentView;
    }

    public void download()
    {
        //Uri f = Uri.fromFile(file);
        StorageReference storageReference = mStorageReference.child(userID + ".csv");

        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri)
            {
                String url = uri.toString();
               downloadFile(getActivity(),url, Environment.DIRECTORY_DOWNLOADS);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
       /* storageReference.getFile(f).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot)
            {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e)
            {

            }
        });*/
    }

    private void downloadFile(Context context, String url, String destination)
    {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(context,destination,  "HealthAnalytics.csv");
        downloadManager.enqueue(request);
    }

    private void createWeekGraph(List<String[]> result) throws NumberFormatException, NullPointerException
    {
            readAll();
            String[] rows;
            DataPoint[] dataPoints = new DataPoint[7];
            int counter = 0;

            if (result.size()>7)
            {
                for (int i = result.size()-7; i < result.size() ; i++)
                {
                    rows = result.get(i);
                    rows[2] = rows[2].replace("\"", "");
                    int temp = Integer.parseInt(rows[2]);
                    dataPoints[counter] = new DataPoint(counter+1, temp);
                    counter += 1;
                }
                if(dataPoints !=null)
                {
                    chart.removeAllSeries();
                    LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints);
                    series.setDrawDataPoints(true);
                    chart.getViewport().setXAxisBoundsManual(true);
                    chart.getViewport().setMaxX(7.0);
                    chart.addSeries(series);
                }
            }
            else
            {
                Toast.makeText(getActivity(), "Not enough data for plot", Toast.LENGTH_SHORT).show();
                dataPoints = new DataPoint[] {new DataPoint(0, 0)};
            }

    }
    private void createFortnightGraph(List<String[]> result) throws NumberFormatException, NullPointerException
    {
        readAll();
        String[] rows;
        DataPoint[] dataPoints = new DataPoint[15];
        int counter = 0;

        if (result.size()>15)
        {
            for (int i = result.size()-15; i < result.size() ; i++)
            {
                rows = result.get(i);
                rows[2] = rows[2].replace("\"", "");
                int temp = Integer.parseInt(rows[2]);
                dataPoints[counter] = new DataPoint(counter+1, temp);
                counter += 1;
            }
            if(dataPoints !=null)
            {
                chart.removeAllSeries();
                LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints);
                series.setDrawDataPoints(true);
                chart.getViewport().setXAxisBoundsManual(true);
                chart.getViewport().setMaxX(15.0);
                chart.addSeries(series);
            }
        }
        else
        {
            Toast.makeText(getActivity(), "Not enough data for plot", Toast.LENGTH_SHORT).show();
            dataPoints = new DataPoint[] {new DataPoint(0, 0)};
        }

    }
    private void createMonthGraph(List<String[]> result) throws NumberFormatException, NullPointerException
    {
        readAll();
        String[] rows;
        DataPoint[] dataPoints = new DataPoint[30];
        int counter = 0;

        if (result.size()>30)
        {
            for (int i = result.size()-30 ; i < result.size(); i++)
            {
                rows = result.get(i);
                rows[2] = rows[2].replace("\"", "");
                int temp = Integer.parseInt(rows[2]);
                dataPoints[counter] = new DataPoint(counter+1, temp);
                counter += 1;
            }
            if(dataPoints !=null)
            {
                chart.removeAllSeries();
                LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints);
                series.setDrawDataPoints(true);
                chart.getViewport().setXAxisBoundsManual(true);
                chart.getViewport().setMaxX(30.0);
                chart.addSeries(series);
            }
        }
        else
        {
            Toast.makeText(getActivity(), "Not enough data for plot", Toast.LENGTH_SHORT).show();
            dataPoints = new DataPoint[] {new DataPoint(0, 0)};
        }

    }

    public void readAll()
    {
            String line;
            String [] data;
            lines.clear();

            try
            {
                InputStream inputStream = new FileInputStream(file);
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                while((line = br.readLine()) != null)
                {
                        data = line.split(",");
                        lines.add(data);

                }
                br.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }


    }

}
