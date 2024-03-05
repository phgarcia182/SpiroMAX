package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import static android.content.ContentValues.TAG;

public class Paso1 extends AppCompatActivity {

    private String deviceName = null;
    private String deviceAddress;
    public static Handler handler;

    public static BluetoothSocket mmSocket;
    public static ConnectedThread connectedThread;
    public static CreateConnectThread createConnectThread;

    private final static int CONNECTING_STATUS = 1; // used in bluetooth handler to identify message status





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paso1);

        // UI Initialization
        final Button buttonConnect = findViewById(R.id.dispositivos_boton);
        final Button buttonSiguiente = findViewById(R.id.botonSiguiente);
        buttonSiguiente.setEnabled(false);
        buttonSiguiente.setVisibility(View.GONE);
        final Button buttonDesconectar = findViewById(R.id.desconectar_dispositivo);
        buttonDesconectar.setEnabled(false);
        final ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        final TextView mensaje = findViewById(R.id.msj);
        final Button buttonVolver = findViewById(R.id.Volver);

        // If a bluetooth device has been selected from SelectDeviceActivity
        deviceName = getIntent().getStringExtra("deviceName");
        if (deviceName != null) {
            // Get the device address to make BT Connection
            deviceAddress = getIntent().getStringExtra("deviceAddress");
            // Show progress and connection status
            progressBar.setVisibility(View.VISIBLE);
            buttonConnect.setEnabled(false);

            /*
            This is the most important piece of code. When "deviceName" is found
            the code will call a new thread to create a bluetooth connection to the
            selected device (see the thread code below)
             */
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            createConnectThread = new CreateConnectThread(bluetoothAdapter, deviceAddress);
            createConnectThread.start();
        }

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case CONNECTING_STATUS:
                        switch (msg.arg1) {
                            case 1:
                                mensaje.setText("Conectado a " + deviceName);
                                // Cambia el tamaño y el formato si es necesario
                                mensaje.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                                mensaje.setTextColor(Color.GREEN);
                                progressBar.setVisibility(View.GONE);
                                buttonConnect.setEnabled(true);
                                buttonDesconectar.setEnabled(true);
                                 // Conexión exitosa, se habilita y muestra el botón de siguiente
                                buttonSiguiente.setVisibility(View.VISIBLE);
                                buttonSiguiente.setEnabled(true);

                                break;
                            case -1:
                                mensaje.setText("Falló la Conexión");
                                // Cambia el tamaño y el formato si es necesario
                                mensaje.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                                mensaje.setTextColor(Color.RED);
                                progressBar.setVisibility(View.GONE);
                                buttonConnect.setEnabled(true);
                                // Conexión fallida, se inhabilita y oculta el botón de siguiente
                                buttonSiguiente.setVisibility(View.GONE);
                                buttonSiguiente.setEnabled(false);
                                break;
                        }
                        break;


                    case 0:
                        mensaje.setText("");
                        buttonSiguiente.setVisibility(View.GONE);
                        buttonSiguiente.setEnabled(false);
                        break;
                }
            }
        };

        // Seleccionar Dispositivo Bluetooth
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Move to adapter list
                Intent intent = new Intent(Paso1.this, Elegir_dispositivos.class);
                startActivity(intent);
            }
        });

        // Pasar al siguiente Slide
        buttonSiguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Move to paso1
                Intent intent = new Intent(Paso1.this, Guardar_Datos.class);
                startActivity(intent);
            }
        });

        // Desconectar Bluetooth
        buttonDesconectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handler.sendEmptyMessage(0);
                connectedThread.cancel();
                createConnectThread.cancel();
                buttonDesconectar.setEnabled(false);
            }
        });

        // Volver al Inicio
        buttonVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (createConnectThread != null){
                    connectedThread.cancel();
                    createConnectThread.cancel();
                }
                Intent intent = new Intent(Paso1.this, Opciones.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //Cierra todas las llamadas a Intent previas
                startActivity(intent);
            }
        });

    }


    public static class CreateConnectThread extends Thread {

            public CreateConnectThread(BluetoothAdapter bluetoothAdapter, String address) {
                /*
                Use a temporary object that is later assigned to mmSocket
                because mmSocket is final.
                */
                BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
                BluetoothSocket tmp = null;
                UUID uuid = bluetoothDevice.getUuids()[0].getUuid();

                try {
                    /*
                    Get a BluetoothSocket to connect with the given BluetoothDevice.
                    Due to Android device varieties,the method below may not work fo different devices.
                    You should try using other methods i.e. :
                    tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                    */
                    tmp = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);

                } catch (IOException e) {
                    Log.e(TAG, "Socket's create() method failed", e);
                }
                mmSocket = tmp;
            }
            public void run() {
                // Cancel discovery because it otherwise slows down the connection.
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                bluetoothAdapter.cancelDiscovery();
                try {
                    // Connect to the remote device through the socket. This call blocks
                    // until it succeeds or throws an exception.
                    mmSocket.connect();
                    Log.e("Status", "Dispositivo conectado");
                    handler.obtainMessage(CONNECTING_STATUS, 1, -1).sendToTarget();
                } catch (IOException connectException) {
                    // Unable to connect; close the socket and return.
                    try {
                        mmSocket.close();
                        Log.e("Status", "No pudo conectarse al dispositivo");
                        handler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
                    } catch (IOException closeException) {
                        Log.e(TAG, "Could not close the client socket", closeException);
                    }
                    return;
                }
                // The connection attempt succeeded. Perform work associated with
                // the connection in a separate thread.
                connectedThread = new ConnectedThread(mmSocket);
                connectedThread.start();
            }

            // Closes the client socket and causes the thread to finish.
            public void cancel() {
                try {
                    mmSocket.close();
                } catch (IOException e) {
                    Log.e(TAG, "Could not close the client socket", e);
                }
            }
        }


public static class ConnectedThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private boolean listening = false;
    private final byte[] buffer = new byte[1201];  // buffer donde se guardarán los datos


    public ConnectedThread(BluetoothSocket socket) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void startListening() {
        listening = true;
        listenForData();
    }

    private void stopListening() {
        listening = false;
    }

    public void listenForData() {
        int bytes = 0; // bytes returned from read()
        // Keep listening to the InputStream until an exception occurs
        try {
            while (listening){
                int availableBytes = mmInStream.available();
                if (availableBytes > 0) { // Si hay datos en buffer
                    byte[] tempBuffer = new byte[availableBytes];
                    int bytesRead = mmInStream.read(tempBuffer);

                    // Copiar los datos del tempBuffer al buffer principal
                    System.arraycopy(tempBuffer, 0, buffer, bytes, bytesRead);
                    bytes += bytesRead;

                    if (bytes == 1200 && buffer[0] != 'A' || bytes == 1201 && buffer[0] == 'A') {
                        bytes = 0;
                        stopListening();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean getListening() {
        return listening;
    }

    public byte[] getbuffercopy(){
        byte [] copy = new byte[buffer.length-1];
        if(buffer[0] == 'A'){
            System.arraycopy(buffer,1,copy,0,(buffer.length-1));
        } else {
            System.arraycopy(buffer,0,copy,0,(buffer.length-1));
        }
        return copy;
    }

    /* Para enviar un dato a la placa Arduino */
    public void write(char input) {
        try {
            mmOutStream.write((int) input); // Convierte el carácter en su valor ASCII
        } catch (IOException e) {
            Log.e("Send Error", "Unable to send message", e);
        }
    }

    /* Para terminar el hilo */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
        }
    }
}
}

