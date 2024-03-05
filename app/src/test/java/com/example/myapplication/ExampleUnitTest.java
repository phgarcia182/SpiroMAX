package com.example.myapplication;

import org.junit.Test;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
        System.out.println("La prueba de suma pasó");
    }

    @Test
    public void testCalcularEdad() {
        String fechaNacimientoStr = "01/02/2004";
        float edad = calcularEdad(fechaNacimientoStr);
        System.out.println("La edad es: " + edad);
    }

    @Test
    public void altura() {
        String alturastr = "1.75";
        float alturametros = Float.parseFloat(alturastr);
        int alturacentimetros = (int) (alturametros * 100);
        System.out.println("La altura en centímetros es:" + alturacentimetros);
    }

    public static int calcularEdad(String fechaNacimientoStr){
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date fechaNacimiento = sdf.parse(fechaNacimientoStr);
            Date fechaActual = new Date();
            long diferenciaEnMillis = 0;
            if(fechaNacimiento != null) {
                diferenciaEnMillis = fechaActual.getTime() - fechaNacimiento.getTime();
            }
            long edadEnMillis = 31557600000L; // 1 año en milisegundos
            return (int) Math.floor((float) diferenciaEnMillis/ (float) edadEnMillis);
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }


}