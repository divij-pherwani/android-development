package com.example.healthanalytics;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class Registration extends AppCompatActivity {

    private EditText editName;
    private EditText editHeight;
    private EditText editWeight;
    private EditText editEmail;
    private EditText editPassword;
    private EditText editGender;
    private EditText editDOB;
    private FirebaseDatabase database;
    private Button registerButton;
    DatabaseReference dataUser;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        editName = findViewById(R.id.editName);
        editDOB = findViewById(R.id.editDOB);
        editHeight = findViewById(R.id.editHeight);
        editWeight = findViewById(R.id.editWeight);
        editEmail = findViewById(R.id.eEmail);
        editGender = findViewById(R.id.editGender);
        editPassword=findViewById(R.id.ePassword);
        registerButton = findViewById(R.id.registerButton);
        dataUser = FirebaseDatabase.getInstance().getReference("USERS");
        mAuth = FirebaseAuth.getInstance();
       // ref = new FirebaseDatabase("https://healthanalytics-f2c8a.firebaseio.com/");


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkEmpty();
            }
        });
    }


    boolean isEmpty(EditText text) {
        CharSequence str = text.getText().toString();
        return TextUtils.isEmpty(str);
    }
    public void checkEmpty()
    {
        if (isEmpty(editName))
        {
            Toast.makeText(this, "Enter name to register!", Toast.LENGTH_SHORT).show();
        }
        else if (isEmpty(editDOB))
        {
            Toast.makeText(this, "Enter date of birth to register!", Toast.LENGTH_SHORT);
        }
        else if (isEmpty(editGender))
        {
            Toast.makeText(this, "Enter gender to register!", Toast.LENGTH_SHORT);
        }
        else if (isEmpty(editHeight))
        {
            Toast.makeText(this, "Enter height to register!", Toast.LENGTH_SHORT);
        }
        else if (isEmpty(editWeight))
        {
            Toast.makeText(this, "Enter weight to register!", Toast.LENGTH_SHORT);
        }
        else if (isEmpty(editEmail))
        {
            Toast.makeText(this, "Enter email to register!", Toast.LENGTH_SHORT);
        }
        else if (isEmpty(editPassword))
        {
            Toast.makeText(this, "Enter password to register!", Toast.LENGTH_SHORT);
        }
        else
        {
            registerUser();
        }
    }

    public void registerUser(){
        mAuth.createUserWithEmailAndPassword(editEmail.getText().toString(), editPassword.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser current = mAuth.getCurrentUser();
                    String uid = current.getUid();
                    UserInfo userInfo = new UserInfo(editName.getText().toString(),editEmail.getText().toString(),editHeight.getText().toString(),editWeight.getText().toString(),editGender.getText().toString(),editDOB.getText().toString());
                    dataUser.child(uid).setValue(userInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                     @Override
                         public void onComplete(@NonNull Task<Void> task) {
                             if(task.isSuccessful())
                             {
                                 Toast.makeText(Registration.this, "Successful", Toast.LENGTH_SHORT).show();
                                 Intent i = new Intent(Registration.this, Login.class);
                                 startActivity(i);
                             }
                         }
                     });


                } else {
                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        Toast.makeText(Registration.this, "User with this email already exist.", Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(Registration.this, "Unsuccessful", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }




}
