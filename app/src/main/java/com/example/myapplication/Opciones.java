package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Opciones extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opciones);}

    public void inNuevoEst(View view) {
        Intent i = new Intent(this, Paso1.class);
        startActivity(i);

    }

    public void inCerrarSes(View view){
        finish();
    }


}
