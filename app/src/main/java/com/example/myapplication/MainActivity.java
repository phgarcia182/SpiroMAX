package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void onCOMENZAR(View view) {
        Intent j = new Intent(this, InicioSesion.class);
        startActivity(j);

    }

    /* ============================ Terminate Connection at BackPress ====================== */
     @Override
     public void onBackPressed() {
         Intent a = new Intent(Intent.ACTION_MAIN);
         a.addCategory(Intent.CATEGORY_HOME);
         a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         startActivity(a);
    }
}