package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;


public class InicioSesion extends AppCompatActivity {

    private TextView mensajeError;
    private FirebaseAuth mAuth;
    private EditText editTextUsuario, editPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio_sesion);

        mAuth = FirebaseAuth.getInstance();

        editTextUsuario = findViewById(R.id.editTextUsuario);
        editPassword = findViewById(R.id.password);
        mensajeError = findViewById(R.id.mensajeError);

        Button botonIniciarSesion = findViewById(R.id.botonIniciarSesion);

       //Para Iniciar sesión, NO UTILIZAR EL AUTOCOMPLETADO DEL TECLADO

       botonIniciarSesion.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               String usuario = editTextUsuario.getText().toString();
               String password = editPassword.getText().toString();

               signIn(usuario,password);
           }
       });
    }

    private void signIn(String email, String password){
        if(email.isEmpty() || password.isEmpty()) {
            //Si falla el inicio de sesión, muestra un mensaje de error
            mensajeError.setText(R.string.error_message);
            editTextUsuario.setText("");
            editPassword.setText("");
        } else {
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    // Inicio de sesión exitoso, redirige a la siguiente actividad
                    mensajeError.setText("");
                    editTextUsuario.setText("");
                    editPassword.setText("");
                    Intent i = new Intent(InicioSesion.this, Opciones.class);
                    startActivity(i);
                } else {
                    //Si falla el inicio de sesión, muestra un mensaje de error
                    mensajeError.setText(R.string.error_message);
                    editTextUsuario.setText("");
                    editPassword.setText("");
                }
            });
        }
    }
}
