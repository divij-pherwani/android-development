package com.example.healthanalytics;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {

    private EditText eemail;
    private EditText epassword;
    String email;
    String password;
    private FirebaseAuth auth;
    private TextView textView;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button registerButton = findViewById(R.id.registerButton);
        eemail = findViewById(R.id.eEmail);
        epassword = findViewById(R.id.ePassword);
        textView = findViewById(R.id.forgotPassword);
        auth = FirebaseAuth.getInstance();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Sign In");
        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Login.this, Registration.class);
                startActivity(i);
            }
        });
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder nBuilder= new AlertDialog.Builder(Login.this);
                View nView = getLayoutInflater().inflate(R.layout.password_dialog,null);
                 final EditText nEmail = nView.findViewById(R.id.neditEmail);
                final Button nConfirm = nView.findViewById(R.id.confirmButton);
                final Button nCancel = nView.findViewById(R.id.cancelButton);
                nBuilder.setView(nView);
                 final AlertDialog dialog = nBuilder.create();
                dialog.show();
                nConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String email = nEmail.getText().toString();
                        if(email.matches(""))
                        {
                            Toast.makeText(Login.this, "Please provide email", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(Login.this, "Reset email sent", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    } else {
                                        Toast.makeText(Login.this, "Error", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                        }
                });
                nCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

            }
        });
        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = eemail.getText().toString();
                password = epassword.getText().toString();
                if(email.matches(""))
                {
                    Toast.makeText(Login.this, "Please provide email", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    if(password.matches(""))
                    {
                        Toast.makeText(Login.this, "Please provide password", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        loginUser(email, password);
                    }
                }
            }
        });
        checkSharedPreference();

    }

    public void checkSharedPreference()
    {
        String email;
        String password;
        email = sharedPreferences.getString(getString(R.string.email),"");
        password = sharedPreferences.getString(getString(R.string.password),"");
        if(email.matches("") || password.matches(""))
        {

        }
        else
        {
            eemail.setText(email);
            epassword.setText(password);
            loginUser(email, password);
        }
    }
    public void saveSharedPreferences(String email, String password)
    {
        editor.putString(getString(R.string.email), email);
        editor.commit();

        editor.putString(getString(R.string.password),password);
        editor.commit();

    }
    public void loginUser(final String email, final String password)
    {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful())
                {
                    Toast.makeText(Login.this, "Login Successful",Toast.LENGTH_SHORT).show();
                    saveSharedPreferences(email,password);
                    Intent i = new Intent(Login.this, MainActivity.class);
                    startActivity(i);
                }
                else
                {
                    Toast.makeText(Login.this, "Login Unsuccessful",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }
}
