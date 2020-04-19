package com.example.healthanalytics;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private static final String TAG = "Read Database";
    private TextView textName;
    private TextView textGender;
    private TextView textAge;
    private TextView textHeight;
    private TextView textWeight;
    private TextView textBMI;
    private View fragmentView;
    private UserInfo userInfo;
    private Button button;
    private TextView textDel;

    public ProfileFragment() {
        // Required empty public constructor
    }

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private  String userID;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private StorageReference mStorageReference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        fragmentView =  inflater.inflate(R.layout.fragment_profile, container, false);
        databaseReference = FirebaseDatabase.getInstance().getReference("USERS");

        mStorageReference = FirebaseStorage.getInstance().getReference("files");

        textName = fragmentView.findViewById(R.id.textName);
        textGender = fragmentView.findViewById(R.id.textGender);
        textAge = fragmentView.findViewById(R.id.textAge);
        textHeight = fragmentView.findViewById(R.id.textHeight);
        textWeight = fragmentView.findViewById(R.id.textWeight);
        textBMI = fragmentView.findViewById(R.id.textBMI);
        button = fragmentView.findViewById(R.id.logoutButton);
        textDel = fragmentView.findViewById(R.id.deleteAccount);
        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser current = mAuth.getCurrentUser();
        userID = current.getUid();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        editor = sharedPreferences.edit();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clear();
                 mAuth.signOut();
                startActivity(new Intent(getActivity(), Login.class));
            }
        });

        databaseReference.child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                readUser(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        textDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Delete Account");
                builder.setMessage("Are you sure?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        clear();
                        current.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful())
                                {


                                    Toast.makeText(getActivity(), "Deleted Authorization Successfully", Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    Toast.makeText(getActivity(), "Deleted Authorization Unsuccessfully", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                        StorageReference storageReference = mStorageReference.child(userID + ".csv");

                        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid)
                            {

                                Toast.makeText(getActivity(), "Deleted File Successfully", Toast.LENGTH_SHORT).show();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                Toast.makeText(getActivity(), "File Error", Toast.LENGTH_SHORT).show();
                            }
                        });
                        databaseReference.child(userID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful())
                                {


                                    startActivity(new Intent(getActivity(), Login.class));

                                }
                                else
                                {
                                    Toast.makeText(getActivity(), "DB Error", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });


                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();


            }
        });


        return fragmentView;
    }

    public void clear()
    {
        editor.putString(getString(R.string.email),"").apply();
        editor.putString(getString(R.string.password),"").apply();
        editor.commit();

    }
    public void readUser(DataSnapshot dataSnapshot)
    {

        userInfo = new UserInfo();
        userInfo.setName(dataSnapshot.child("name").getValue(String.class));
        userInfo.setGender(dataSnapshot.child("gender").getValue(String.class));
        userInfo.setDOB(dataSnapshot.child("dob").getValue(String.class));
        userInfo.setHeight(dataSnapshot.child("height").getValue(String.class));
        userInfo.setWeight(dataSnapshot.child("weight").getValue(String.class));
        setInfo();


    }

    public void setInfo()
    {
       textName.setText(userInfo.getName());
        textGender.setText(userInfo.getGender());
        textWeight.setText(userInfo.getWeight());
        textHeight.setText(userInfo.getHeight());

        String weight = userInfo.getWeight();
        String height = userInfo.getHeight();
        String dobString = userInfo.getDOB();
        Date date = null;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            date = sdf.parse(dobString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(date == null)
        {
            textAge.setText(dobString);
        }
        else
        {
            Calendar dob = Calendar.getInstance();
            Calendar today = Calendar.getInstance();

            dob.setTime(date);

            int year = dob.get(Calendar.YEAR);
            int month = dob.get(Calendar.MONTH);
            int day = dob.get(Calendar.DAY_OF_MONTH);

            dob.set(year, month + 1, day);

            int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

            if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }
            editor.putString(getString(R.string.age),String.valueOf(age)).apply();
            textAge.setText(String.valueOf(age));
        }

        DecimalFormat df = new DecimalFormat("0.00");
        if (height != null && weight != null)
        {
            float heightValue = Float.parseFloat(height) / 100;
            float weightValue = Float.parseFloat(weight);

            float bmi = weightValue / (heightValue * heightValue);
            textBMI.setText(String.valueOf(df.format(bmi)));

        }
    }

}
