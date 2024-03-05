package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class resultadosValores extends AppCompatActivity {

    private static final String[] resultados = new String[4];

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;

    private Button buttonfinalizar;

    private TextView Mensaje;

    private static String mensajeDiagnostico = "";

    private final String[] DatosPaciente = Guardar_Datos.getDatos();

    private final String Nombre = DatosPaciente[0];

    private final String Apellido = DatosPaciente[1];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultados_valores);
        TextView fev1 = findViewById(R.id.txtValorFEV);
        TextView fvc = findViewById(R.id.txtFVC);
        TextView fev1yfvc = findViewById(R.id.txtFEVFVC);
        TextView pef = findViewById(R.id.txtPEF);
        Button buttonReiniciar = findViewById(R.id.reiniciar);
        buttonfinalizar = findViewById(R.id.btnFinalizar);
        buttonfinalizar.setEnabled(false);
        Button buttonPDF = findViewById(R.id.btnPDF);
        Mensaje = findViewById(R.id.mensajeCarga);
        TextView diagnostico = findViewById(R.id.mensajeDiagnostico);

        float FEV1 = Comienzo_Espirar.FEV1;
        // float FEV1 = 2f;
       float FVC = Comienzo_Espirar.FVC;
        // float FVC = 4f;
       float PEF = Comienzo_Espirar.PEF;
        // float PEF = 7f;
       float ratio = (Math.round((Comienzo_Espirar.FEV1/Comienzo_Espirar.FVC)*100f)/100f);
        // float ratio = 0.7f;

        resultados[0] = FEV1 +" L";
        resultados[1] = FVC +" L";
        resultados[2] = PEF +" L/s";
        resultados[3] = String.valueOf(ratio);

        fev1.setText(resultados[0]);
        fvc.setText(resultados[1]);
        pef.setText(resultados[2]);
        fev1yfvc.setText(resultados[3]);


        String FechaNacimiento = DatosPaciente[2];
        int edad = calcularEdad(FechaNacimiento);
        int altura = (int) (Float.parseFloat(DatosPaciente[3])*100); // Altura en centímetros
        String sexo = DatosPaciente[5];


        mensajeDiagnostico = algoritmoDiagnostico(FEV1,FVC,ratio,edad,altura,sexo);
        diagnostico.setText(mensajeDiagnostico);
        if(Objects.equals(mensajeDiagnostico,"Normal")){
            diagnostico.setTextColor(Color.GREEN);
        } else {
            if(Objects.equals(mensajeDiagnostico,"Obstrucción Límite")){
                diagnostico.setTextColor(Color.YELLOW);
            } else {
                diagnostico.setTextColor(Color.RED);
            }
        }


        buttonReiniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(resultadosValores.this, Comienzo_Espirar.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //Cierra todas las llamadas a Intent previas
                startActivity(intent);
            }
        });

        buttonPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAndRequestPermissions();
            }
        });

        buttonfinalizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Paso1.connectedThread != null){
                    Paso1.connectedThread.cancel();
                    Paso1.createConnectThread.cancel();
                }
                Intent intent = new Intent(resultadosValores.this,Opciones.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //Cierra todas las llamadas a Intent previas
                startActivity(intent);
            }
        });


    }

    public static int calcularEdad(String fechaNacimientoStr){
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date fechaNacimiento = sdf.parse(fechaNacimientoStr);
            Date fechaActual = new Date();
            long diferenciaEnMillis = fechaActual.getTime() - fechaNacimiento.getTime();
            long yearEnMillis = 31557600000L; // 1 año en milisegundos
            return (int) Math.floor((float) diferenciaEnMillis/ (float) yearEnMillis);
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static String algoritmoDiagnostico(float FEV1, float FVC, float ratio, int edad, int altura, String sexo){
        String mensajeDiagnostico;

        if (Objects.equals(sexo, "M")){
            if (edad > 25){
                //Masculino mayor a 25 años
                float[] coefsFEV1 = {-9.72308f,0.00933f,-0.00021f,2.10839f};
                float[] coefsFVC = {-10.54790f,0.00334f,-0.00012f,2.32222f};
                float[] coefsratio = {0.82621f,0.00101f,-0.00005f,-0.21653f};
                float[] LLNs = calculoLLNs(coefsFEV1,coefsFVC,coefsratio,edad,altura);
                mensajeDiagnostico = comparacion(FEV1,FVC,ratio,LLNs[0],LLNs[1],LLNs[2]);
                return mensajeDiagnostico;
            } else {
                //Masculino menor o igual a 25 años
                float[] coefsFEV1 = {-10.75820f,0.1032f,-0.00231f,2.10839f};
                float[] coefsFVC = {-11.63230f,0.09795f,-0.00217f,2.32222f};
                float[] coefsratio = {0.82621f,0.00101f,-0.00005f,-0.21653f};
                float[] LLNs = calculoLLNs(coefsFEV1,coefsFVC,coefsratio,edad,altura);
                mensajeDiagnostico = comparacion(FEV1,FVC,ratio,LLNs[0],LLNs[1],LLNs[2]);
                return mensajeDiagnostico;
            }
        } else {
            // Femenino
            float[] coefsFEV1 = {-8.68467f,0.00495f,-0.00018f,1.90019f};
            float[] coefsFVC = {-9.84941f,0.00772f,-0.00018f,2.14118f};
            float[] coefsratio = {1.00699f,-0.00196f,-0.00001f,-0.23815f};
            float[] LLNs = calculoLLNs(coefsFEV1,coefsFVC,coefsratio,edad,altura);
            mensajeDiagnostico = comparacion(FEV1,FVC,ratio,LLNs[0],LLNs[1],LLNs[2]);
            return mensajeDiagnostico;
        }
    }

    public static float[] calculoLLNs(float[] coefsFEV1, float[] coefsFVC, float[] coefsratio, int edad, int altura){
        float[] LLNs = new float[3];
        LLNs[0] = (float) Math.exp(coefsratio[0] + coefsratio[1]*edad + coefsratio[2]*(edad^2) + coefsratio[3]*Math.log(altura));
        LLNs[1] = (float) Math.exp(coefsFEV1[0] + coefsFEV1[1]*edad + coefsFEV1[2]*(edad^2) + coefsFEV1[3]*Math.log(altura));
        LLNs[2] = (float) Math.exp(coefsFVC[0] + coefsFVC[1]*edad + coefsFVC[2]*(edad^2) + coefsFVC[3]*Math.log(altura));
        return LLNs;
    }

    public static String comparacion(float FEV1, float FVC, float ratio, float LLNratio, float LLNFEV1, float LLNFVC){
        String mensaje = "";
        if(ratio >= LLNratio){
            // No es obstructivo, vemos si es restrictivo

            if(FVC >= LLNFVC){
            // Condición Normal
            mensaje = "Normal";
            }
            else {
                // Hay restricción
                mensaje = "Posible Restricción";
            }

        }
        else {
           // Podría ser obstructivo

           if(FEV1>=LLNFEV1){
               // Caso límite
               mensaje = "Obstrucción Límite";
           }
           else {
               if (FVC>=LLNFVC){
                   // Hay obstrucción
                   mensaje = "Posible Obstrucción";
               }
               else {
                   mensaje = "Posible Patrón Mixto";
               }
           }
        }

        return mensaje;
    }


    private void checkAndRequestPermissions() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            //Verificar si ya se tienen permisos
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // Si ya se tienen los permisos, proceder con la lógica para generar el PDF
                generarPDF();
            } else {
                // Si no se tienen, solicitar permisos
                requestStoragePermission();
            }
        } else {
            // Si la versión de Android es anterior a Marshmallow, no se necesario solicitar permisos
            generarPDF();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    // La solicitud de permisos se realiza en tiempo de ejecución
    private void requestStoragePermission() {
        //Solicitar Permisos al usuario
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_CODE_STORAGE_PERMISSION){
            //Verificar que los permisos fueron concedidos
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                // Permisos concedidos, proceder con la lógica para generar el PDF
                generarPDF();
            } else {
                // Permiso denegado, informar al usuario o realizar acciones necesarias
            }
        }

    }

    private void generarPDF() {
        String fileName = "Estudio"+Nombre+Apellido+".pdf";
        // Crear un nuevo hilo
        Thread PDFThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // Lógica para generar el PDF y guardarlo en una carpeta de la propia aplicación
                PDFGenerator.generatePDF(resultadosValores.this, fileName);

                // Actualizar la interaz de usuario desde el hilo principal
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Éstas líneas se ejecutarán en el hilo principal
                        Mensaje.setText(R.string.loaded);
                        Mensaje.setTextColor(Color.GREEN);
                        buttonfinalizar.setEnabled(true); // Se habilita el botón finalizar después de guardar los datos
                    }
                });
            }
        });

        // Iniciar el hilo
        PDFThread.start();
    }

    public static String[] getDatos() {
        return resultados;
    }

    public static String getDiagnostico() {return mensajeDiagnostico; }

    public void inVolver(View view) {
        finish();
}

}