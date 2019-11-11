package com.inducesmile.workoutassistant;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {
    Button button;
    private FirebaseAuth mAuth;
    EditText email,password;
    private  FirebaseAuth.AuthStateListener firebaseAuthListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        button = findViewById(R.id.loginbt);
        email=findViewById(R.id.email);
        password=findViewById(R.id.password);

        mAuth=FirebaseAuth.getInstance();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String Email=email.getText().toString();
                final String Password=password.getText().toString();

                mAuth.signInWithEmailAndPassword(Email,Password).addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(getApplicationContext(),"Error in Sign In",Toast.LENGTH_LONG).show();
                        }
                        else{
                            Intent intent=new Intent(Login.this,ChooseObjective.class);
                            startActivity(intent);
                            //            finish();
                        }

                    }
                });
            }
        });

    }
}