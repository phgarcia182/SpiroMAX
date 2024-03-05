package com.example.myapplication;
import android.graphics.Color;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.TextView;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Arrays;

public class Comienzo_Espirar extends AppCompatActivity {

    private Button iniciarContadorButton, anularButton, resultadosButton, calibrarButton;
    private TextView  mensajeTextView, cronoTextView;
    private final Handler handler = new Handler();
    private int segundosTranscurridos = 6;
    private ScheduledExecutorService scheduledExecutorService;

    private Future<?> future;


    private final double[] Flujo = new double[600];
    private static final double lsb = (5d/1024d); // bit menos significativo
    private double Offset = 0;
    public static float[] copiaFlujo = new float[600];
    public static float PEF;
    public static float FEV1;
    public static float FVC;
    private final double[] Volumen = new double[600];
    public static float[] copiaVolumen = new float[600];
    public static float[] VolumenEsp = new float[600];

    //Las listas creadas serán de tipo double, y la función copia de dichas listas será en formato float



    //Se Llama a la instancia de la comunicación creada en Paso1
    Paso1.ConnectedThread paso1ConnectedThread = Paso1.connectedThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comienzo_espirar);

        // UI Initialization
        cronoTextView=findViewById(R.id.crono);
        mensajeTextView = findViewById(R.id.No_detener);
        iniciarContadorButton = findViewById(R.id.iniciar_grab);
        anularButton = findViewById(R.id.Reintentar);
        anularButton.setEnabled(false);
        resultadosButton = findViewById(R.id.sig);
        resultadosButton.setEnabled(false);
        calibrarButton = findViewById(R.id.calib);

        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(); // Se utiliza un schedule para iniciar la escucha de datos en segundo plano


        // Al presionar el botón iniciar, el contador comienza
        iniciarContadorButton.setOnClickListener(v -> {
            mensajeTextView.setText("");
            if (future != null && !future.isDone()) {
                future.cancel(true); // Para evitar que coexistan varias instancias del runnable escuchayprocesadoRunnable a la vez
            }
            future = scheduledExecutorService.schedule(escuchayprocesadoRunnable,0, TimeUnit.MILLISECONDS); //El scheduled se ejecuta de inmediato
            startCountdown();
        });



        // Al presionar el botón calibrar, se realiza la puesta a cero del dispositivo médico.
        calibrarButton.setOnClickListener(view -> {
            iniciarContadorButton.setEnabled(false);
            calibrarButton.setEnabled(false);
            mensajeTextView.setText(getString(R.string.mensaje_calibrando));
            mensajeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            mensajeTextView.setTextColor(Color.BLACK);
            if (future != null && !future.isDone()) {
                future.cancel(true); // Para evitar que coexistan varias instancias del runnable promediadoRunnable a la vez
            }
            future = scheduledExecutorService.schedule(promediadoRunnable,0, TimeUnit.MILLISECONDS); //El scheduled se ejecuta de inmediato
        });

        // Al presionar el botón anular, se elimina cualquier medición realizada.
        anularButton.setOnClickListener(v -> {
            Arrays.fill(Flujo, 0); // Reiniciar todos los valores de Flujo a 0
            Arrays.fill(copiaFlujo,0);
            Arrays.fill(Volumen,0);// Reiniciar todos los valores de Volumen a 0
            Arrays.fill(copiaVolumen,0);
            PEF = 0;
            FEV1 = 0;
            FVC = 0;
            iniciarContadorButton.setEnabled(true); // Habilitar el botón de iniciar
            resultadosButton.setEnabled(false); // En caso de estar habilitado, deshabilita los resultados
            segundosTranscurridos = 6; // Reiniciar el contador y ocultar el botón de reintentar
            mensajeTextView.setText(""); // Limpiar el texto
            cronoTextView.setText(getString(R.string.contador_text_template, segundosTranscurridos));
            calibrarButton.setEnabled(true); // Habilita el botón calibrar
            anularButton.setEnabled(false);// Deshabilitar el botón de anular
        });


        resultadosButton.setOnClickListener(v -> {
            Intent i = new Intent(Comienzo_Espirar.this, Grafica1.class);
            startActivity(i);
        });

    }




    private final Runnable promediadoRunnable = () -> {
        paso1ConnectedThread.write('I'); //Se envia una I a Arduino
        paso1ConnectedThread.startListening(); //Comienza la escucha de datos
        while (paso1ConnectedThread.getListening()){
            //Se espera hasta que termine la escucha
        }
        byte[] copiabuffer = paso1ConnectedThread.getbuffercopy();
        try {
            int p = 0;
            int c = 0;
            int d = 0;
            for (int i = 0 ; i < copiabuffer.length ; i+=2){ //Conversión bytes a tensión en volts
                int a = copiabuffer[i];
                int b = copiabuffer[i+1];
                if(a%2 == 0  &&  b%2 != 0){ //Control de comunicación por bit de paridad
                    c = a/2;
                    d = b/2;
                    p = i/2;
                    Flujo[p] = (32*c + d);
                }
                if(b%2 == 0  &&  a%2 != 0){
                    c = a/2;
                    d = b/2;
                    p = i/2;
                    Flujo[p] = (32*d + c);
                }
                if((a%2 == 0  && b%2 == 0) || (a%2 != 0 &&  b%2 != 0)){ //Hay un byte alterado
                    if(i > 0){
                        p = i/2;
                        Flujo[p] = Flujo[p-1];
                    } else{
                        Flujo[i] = 0;
                    }
                }
            }


            for (double valor : Flujo){
                //if(valor < Offset && valor > 0){
                //    Offset = valor;
                //}
                Offset+=valor;
            }
            Offset = Offset/600;
            Log.d("Offset","es " + Offset);
            Offset = lsb*Offset;

        } catch (Exception e){
            e.printStackTrace();
        }

        runOnUiThread(() -> {
            // Actualizaciones de la interfaz de usuario
            mensajeTextView.setText(getString(R.string.mensaje_calibracion_finalizada));
            mensajeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            mensajeTextView.setTextColor(Color.GREEN);
            iniciarContadorButton.setEnabled(true);
        });
    };



    private final Runnable escuchayprocesadoRunnable = () -> {
        paso1ConnectedThread.write('I'); //Se envia una I a Arduino
        paso1ConnectedThread.startListening(); //Comienza la escucha de datos
        while (paso1ConnectedThread.getListening()){
            //Se espera hasta que termine la escucha
        }
        byte[] copiabuffer = paso1ConnectedThread.getbuffercopy();
        Log.d("Longitud de copiabuffer", "Es " + copiabuffer.length);
        try {
            int p = 0;
            int c = 0;
            int d = 0;
            int cont1 = 0;
            int cont2 = 0;
            int cont3 = 0;
            for (int i = 0 ; i < copiabuffer.length ; i+=2){ //Conversión bytes a tensión en volts
                int a = copiabuffer[i];
                int b = copiabuffer[i+1];
                if(a%2 == 0  &&  b%2 != 0){ //Control de comunicación por bit de paridad
                    cont1++;
                    c = a/2;
                    d = b/2;
                    p = i/2;
                    Flujo[p] = (lsb*(32*c + d)) - Offset;
                }
                if(b%2 == 0  &&  a%2 != 0){
                    cont2++;
                    c = a/2;
                    d = b/2;
                    p = i/2;
                    Flujo[p] = (lsb*(32*d + c)) - Offset;
                }
                if((a%2 == 0  && b%2 == 0) || (a%2 != 0 &&  b%2 != 0)){ //Hay un byte alterado
                    cont3++;
                    if(i > 0){
                        p = i/2;
                        Flujo[p] = Flujo[p-1];
                    } else{
                        Flujo[i] = 0;
                    }
                }
            }
            Log.d("Contador1","Es" + cont1);
            Log.v("Contador2","Es" + cont2);
            Log.e("Contador3","Es" + cont3);

            double a = 0;
            double b = 0;

            for (int i = 0 ; i < Flujo.length ; i++){ //Conversión tensión a flujo en L/Seg
                if(Flujo[i] >= 0d){ // Espiración
                    a = 0.000011895d;
                    b = 0.00010537d;
                } else { // Inspiración
                    a = -0.000011937;
                    b = 0.0000065349;
                }

                double cte1 = b/(2*a);
                double cte2 = a/(b*b);

                //Se implementa la curva de calibración
                //Flujo = cte1*(-1 + sqrt(1+4*cte2*Tension))
                double temp1 = 1 + 4*cte2*Flujo[i];
                double temp2 = Math.sqrt(temp1);
                double temp3 = -1 + temp2;
                double temp4 = cte1*temp3;
                Flujo[i] = temp4/(60d);  // L/Seg
            }

            generarCopiayPEF(); // Obtengo copiaFlujo y PEF

            // Integramos el Flujo para obtener el Volumen, utilizando el método de los trapecios
            Volumen[0] = 0d;
            VolumenEsp[0] = 0f;
            for (int i = 1; i < Flujo.length ; i++){
                Volumen[i] = Volumen[i - 1] + (Flujo[i - 1] + Flujo[i]) * (0.01d / 2d); // ▲t = 0.01 Seg y Volumen en L
                VolumenEsp[i] = VolumenEsp[i - 1] + (float) ((Flujo[i - 1] + Flujo[i]) * (0.01d / 2d));

                if((Flujo[i - 1] + Flujo[i]) < 0){
                    VolumenEsp[i] = VolumenEsp[i - 1];
                }
            }

            generarCopiaFEVyFVC(); // Obtenemos copiaVolumen, FEV1 y FVC


        } catch (Exception e){
            e.printStackTrace();
        }

        runOnUiThread(() -> {
            // Actualizaciones de la interfaz de usuario
            mensajeTextView.setText(getString(R.string.mensaje_finalizado));
            mensajeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            mensajeTextView.setTextColor(Color.GREEN);
            anularButton.setEnabled(true); //Se habilita el botón de anular
            resultadosButton.setEnabled(true); //Habilita botón resultados
        });
    };



    private void startCountdown() {

        // Actualizar cada 1 segundo
        // Mostrar "Procesando..." con el formato deseado
        // Cambia el tamaño y el formato si es necesario
        Runnable contadorRunnable = new Runnable() {
            @Override
            public void run() {
                segundosTranscurridos--;
                if (segundosTranscurridos > 0) {
                    cronoTextView.setText(getString(R.string.contador_text_template, segundosTranscurridos));
                    handler.postDelayed(this, 1000); // Actualizar cada 1 segundo
                } else {
                    cronoTextView.setText(getString(R.string.contador_text_template, segundosTranscurridos));
                    // Mostrar "Procesando..." con el formato deseado
                    mensajeTextView.setText(getString(R.string.mensaje_procesando));
                    // Cambia el tamaño y el formato si es necesario
                    mensajeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                    mensajeTextView.setTextColor(Color.BLACK);
                }
            }
        };

        iniciarContadorButton.setEnabled(false);
        calibrarButton.setEnabled(false);

        // Mostrar "¡No te detengas!" con el formato deseado
        mensajeTextView.setText(getString(R.string.mensaje_no_detener));
        // Cambia el tamaño y el formato si es necesario
        mensajeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
        mensajeTextView.setTextColor(Color.RED);

        handler.postDelayed(contadorRunnable, 1000); // Iniciar el contador
    }



    @Override
    protected void onDestroy(){
        super.onDestroy();
        //Detener el servicio ejecutor programado cuando la actividad se destruye
        if (scheduledExecutorService != null && !scheduledExecutorService.isShutdown()) {
            scheduledExecutorService.shutdownNow();
        }
    }



    private void generarCopiayPEF(){
        float valor = 0;
        for (int i = 0; i < Flujo.length; i++) {
            copiaFlujo[i] = (float) Flujo[i];
            if(copiaFlujo[i] > valor){
                valor = Math.round(copiaFlujo[i]*100f)/100f; //Para que tenga dos decimales.
                PEF = valor;
            }
        }
    }



    private void generarCopiaFEVyFVC(){
        for (int i = 0; i < Volumen.length; i++) {
            copiaVolumen[i] = (float) Volumen[i];
        }
        for (int i = 0; i < (VolumenEsp.length - 100);i++) {
            if((VolumenEsp[i+100]-VolumenEsp[i]) <= 0.025d){
                FVC = Math.round(VolumenEsp[i]*100f)/100f;
            } else {
                FVC = Math.round(VolumenEsp[(VolumenEsp.length-1)]*100f)/100f;
            }
        }
        float valor1 = PEF;
        float valor2 = 0;
        float pendiente = 0;
        float t = 0;
        int j = 0;
        for (int i = 1; i < VolumenEsp.length; i++) {
            pendiente = ((VolumenEsp[i]-VolumenEsp[i-1])/(0.01f));
            valor2 = PEF - pendiente;
            if(valor2 < valor1){ // La pendiente se aproxima a PEF
                valor1 = valor2;
                j = i; // Guardo el índice
            }
        }
        t = ((j-1)/100f) - (VolumenEsp[j-1]/(PEF-valor1)); // t = t(j-1) - V(i-1)/(pendiente)
        j = 0;
        while((j/100f) < t){
            j++;
        }
        FEV1 = Math.round(VolumenEsp[j+100]*100f)/100f;
    }
}