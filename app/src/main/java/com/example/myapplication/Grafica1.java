package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;




public class Grafica1 extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grafico1);
        final Button buttonsiguiente = findViewById(R.id.siggraf1);
        final Button buttonvolver = findViewById(R.id.volvergraf1);
        final LineChart linechart = findViewById(R.id.chart1);


        float[] volumenEsp = Comienzo_Espirar.VolumenEsp;

        int contador = 0;


        List<Entry> entries = new ArrayList<Entry>(); // Representa la curva que uno genera

        for (float dato : volumenEsp){
            entries.add(new Entry(contador,dato)); // El Entry representa un punto con sus coordenadas x e y
            contador+=1;
        }

        LineDataSet dataSet = new LineDataSet(entries,""); // Cada curva debe ser almacenada en un LineDataSet
        linechart.getLegend().setEnabled(false); // Para ocultar la leyenda
        dataSet.setAxisDependency(YAxis.AxisDependency.LEFT); // Para que utilice el eje y de la izquierda como referencia
        dataSet.setDrawValues(false); // Desactiva las etiquetas de los valores sobre la curva

        LineData lineData = new LineData(dataSet); // En LineData se almacenan todos los LineDataSets y luego se lo pasa a LineChart para graficarlo
        linechart.setData(lineData); // Se almacena el LineData en el espacio para graficar
        linechart.invalidate(); // Actualiza la interfaz de usuario con los datos proporcionados a linechart



        // Edito los ejes, título y labels

        // Título
        Description titleDescription = new Description();
        titleDescription.setText("Volumen vs Tiempo");
        titleDescription.setTextSize(16f); // Tamaño de fuente opcional
        titleDescription.setPosition(480f, 20f); // Ajusta las coordenadas (x, y) para la posición del título
        linechart.setDescription(titleDescription);



        // Eje x
        XAxis xaxis = linechart.getXAxis(); // Obtengo el eje x
        List<String> etiquetas = new ArrayList<>();
        float c;
        for(int i=0;i<=contador;i+=1){
           c = i;
           c = c/100;
           etiquetas.add(String.valueOf(c));
        }

        // Agrega 49 etiquetas vacías seguidas de "[Seg]"
        for (int i = 0; i < 49; i++) {
            etiquetas.add("");
        }
        etiquetas.add("[Seg]");

        xaxis.setValueFormatter(new IndexAxisValueFormatter(etiquetas)); //Para 0,1,50,100,... se mostrarán las etiquetas 0.0,0.01,0.5,1

        xaxis.setGranularity(50f); // El eje x muestra de 50 en 50
        xaxis.setLabelCount(etiquetas.size());

        xaxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xaxis.setAxisMinimum(0f);
        xaxis.setAxisMaximum(etiquetas.size()-1);
        xaxis.setDrawAxisLine(true); // Dibujar línea del eje x
        xaxis.setDrawLabels(true); // Dibujar etiquetas en el eje X





        //Eje Y
        float maxYValue = 6.5f;
        YAxis yaxis = linechart.getAxisLeft();
        yaxis.setAxisMinimum(0f);
        yaxis.setAxisMaximum(maxYValue);
        yaxis.setDrawAxisLine(true);
        yaxis.setDrawLabels(true);
        yaxis.setGranularity(0.5f);
        yaxis.setLabelCount(entries.size());

        // Utiliza un ValueFormatter personalizado para mostrar "[L]" en lugar del valor máximo
        yaxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                // Verifica si el valor es igual al máximo y muestra "[L]" en su lugar
                return (value == maxYValue) ? "[L]" : super.getAxisLabel(value, axis);
            }
        });



        linechart.getAxisRight().setDrawGridLines(false); // Desactivar las líneas de la cuadrícula horizontal en el eje derecho
        linechart.getAxisRight().setDrawLabels(false); // Oculto el eje y izquierdo

        LineChartSingleton.setLineChart1(linechart); // Guardo el linechart del gráfico1 para luego poder almacenarlo en el PDF


        buttonvolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Volver al slide de la toma de datos
                finish();
            }
        });


        buttonsiguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Grafica1.this,Grafica2.class);
                startActivity(intent);
            }
        });




    }
}
