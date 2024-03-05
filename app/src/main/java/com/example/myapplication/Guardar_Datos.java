package com.example.myapplication;


import android.content.Intent;
import android.graphics.Color;


import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class Guardar_Datos extends AppCompatActivity {

    private String Nombre;
    private String Apellido;
    private String FechaNacimiento;
    private String Altura;
    private String Peso;
    private String Sexo;
    private static final String[] Datos = new String[6];
    private final String regexNacimiento = "\\d{2}/\\d{2}/\\d{4}";
    private final String regexAltura = "^(\\d{1,3}(\\.\\d{1,2})?)$";
    private final String regexPeso = "^(\\d{1,3}(\\.\\d{1,2})?)$";
    private TextView Mensaje;
    private RadioButton radioButtonMale, radioButtonFemale;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guardar_datos);

        EditText txtNombre = findViewById(R.id.txtNombre);
        EditText txtApellido = findViewById(R.id.txtApellido);
        EditText txtNacimiento = findViewById(R.id.txtNacimiento);
        EditText txtAltura = findViewById(R.id.txtALTURA);
        EditText txtPeso = findViewById(R.id.txtPeso);
        Button buttonSiguiente = findViewById(R.id.btnSiguiente);
        Mensaje = findViewById(R.id.mensajeError);
        RadioGroup radioGroup = findViewById(R.id.radioGroup);
        radioButtonMale = findViewById(R.id.radioButtonMale);
        radioButtonFemale = findViewById(R.id.radioButtonFemale);

        // Configurar Listener para el RadioGroup

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                // Verificar cuál radioButton está seleccionado
                if (checkedId == radioButtonMale.getId()){
                    Sexo = "M";
                    Datos[5] = Sexo;
                    Mensaje.setText(R.string.Male_selection);
                    Mensaje.setTextColor(Color.GREEN);
                } else if (checkedId == radioButtonFemale.getId()){
                    Sexo = "F";
                    Datos[5] = Sexo;
                    Mensaje.setText(R.string.Female_selection);
                    Mensaje.setTextColor(Color.GREEN);
                }
            }
        });

        buttonSiguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Nombre = txtNombre.getText().toString();
                Datos[0] = Nombre;
                Apellido = txtApellido.getText().toString();
                Datos[1] = Apellido;
                FechaNacimiento = txtNacimiento.getText().toString();
                Datos[2] = FechaNacimiento;
                Altura = txtAltura.getText().toString();
                Datos[3] = Altura;
                Peso = txtPeso.getText().toString();
                Datos[4] = Peso;


                if (!Nombre.isEmpty() && !Apellido.isEmpty() && !FechaNacimiento.isEmpty()
                        && !Altura.isEmpty() && !Peso.isEmpty() && !Sexo.isEmpty()) {
                    if(FechaNacimiento.matches(regexNacimiento) && Altura.matches(regexAltura) && Peso.matches(regexPeso)){
                        // Pasamos a la Comienzo_Espirar
                        Intent intent = new Intent(Guardar_Datos.this,Comienzo_Espirar.class);
                        startActivity(intent);

                    } else {
                        //El formato de la fecha, altura o peso es incorrecto
                        Mensaje.setText(R.string.Wrong_input_message);
                        Mensaje.setTextColor(Color.RED);
                    }

                } else {
                    // Al menos una de las variables está vacía, muestra un mensaje de error
                    Mensaje.setText(R.string.empty_data_message);
                    Mensaje.setTextColor(Color.RED);
                }
            }
        });
    }

    public static String[] getDatos(){
        return Datos;
    }

    public void inVolver(View view) {
        finish();
    }
}