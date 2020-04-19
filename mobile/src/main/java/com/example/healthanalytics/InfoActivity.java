package com.example.healthanalytics;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class InfoActivity extends AppCompatActivity {

    private TextView infoText;
    private String text;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        //  getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        infoText = findViewById(R.id.infoText);
        text = "Project name: Health Analytics." +"\n\n"+
                "Bachelor Thesis Title: Designing an android application to process and analyse health data." +"\n\n"+
                "This application is created by Divij Pherwani in collaboration with dr inż. Piotr Wąsiewicz and Warsaw University of Technology, Poland."
                + "\n\n"
                + "Recommended BMI Values\n\n" +
                "Underweight: Less than 18.5\n" +
                "Healthy weight: 18.5 to 24.9\n" +
                "Overweight: 25 to 29.9\n" +
                "Obese: 30 or higher\n" +
                "\n\n" +
                "Recommended Heart Values \n\n" +
                "Resting: 60-100 bpm\n" +
                "Walking: 60-120 bpm\n" +
                "Running: 60-170 bpm\n" +
                "Driving Vehicle: 60-120 bpm\n" +
                "Riding Bicycle: 60-170 bpm\n" +
                "\n\n"
                +"This project is subjected to copyright © " +
                "\n\nCreated in 2020 for Bachelor thesis work.";
        infoText.setText(text);
    }
}
