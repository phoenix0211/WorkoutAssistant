package com.inducesmile.workoutassistant;

import android.app.Application;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Home extends Application {
    public void onCreate() {
        super.onCreate();

        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
        FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();

        if(firebaseUser!=null){
            Intent intent=new Intent(Home.this,ChooseObjective.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

    }

}
