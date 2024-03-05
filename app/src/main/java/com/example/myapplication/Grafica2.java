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
import com.github.mikephil.charting.formatter.ValueFormatter;


import java.util.ArrayList;
import java.util.List;
public class Grafica2 extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grafico2);
        final Button buttonsiguiente = findViewById(R.id.siggraf2);
        final Button buttonvolver = findViewById(R.id.volvergraf2);
        final LineChart linechart = findViewById(R.id.chart2);

        float[] flujo = Comienzo_Espirar.copiaFlujo;
        float[] volumen = Comienzo_Espirar.copiaVolumen;


        List<Entry> entries = new ArrayList<Entry>(); // Representa la curva que uno genera
        for (int i = 0; i < flujo.length; i++ ){
            entries.add(new Entry(volumen[i],flujo[i])); // El Entry representa un punto con sus coordenadas x e y
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
        titleDescription.setText("Flujo vs Volumen");
        titleDescription.setTextSize(16f); // Tamaño de fuente opcional
        titleDescription.setPosition(480f, 20f); // Ajusta las coordenadas (x, y) para la posición del título
        linechart.setDescription(titleDescription);



        // Eje x
        float maxXValue = 6.5f;
        XAxis xaxis = linechart.getXAxis(); // Obtengo el eje x
        xaxis.setLabelCount(entries.size());
        xaxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xaxis.setAxisMinimum(0f);
        xaxis.setAxisMaximum(maxXValue);
        xaxis.setGranularity(0.5f);
        xaxis.setDrawAxisLine(true); // Dibujar línea del eje x
        xaxis.setDrawLabels(true); // Dibujar etiquetas en el eje X

        // Utiliza un ValueFormatter personalizado para mostrar "[L]" en lugar del valor máximo
        xaxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                // Verifica si el valor es igual al máximo y muestra "[L]" en su lugar
                return (value == maxXValue) ? "[L]" : super.getAxisLabel(value, axis);
            }
        });


        //Eje Y
        float maxYValue = 8f;
        float minYValue = -5f;
        YAxis yaxis = linechart.getAxisLeft(); // Obtengo el eje y
        yaxis.setAxisMinimum(minYValue);
        yaxis.setAxisMaximum(maxYValue);
        yaxis.setGranularity(0.5f);
        yaxis.setDrawAxisLine(true);
        yaxis.setDrawLabels(true);

        // Utiliza un ValueFormatter personalizado para mostrar "[L/seg]" en lugar del valor máximo
        yaxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                // Verifica si el valor es igual al máximo y muestra "[L/seg]" en su lugar
                return (value == maxYValue) ? "[L/seg]" : super.getAxisLabel(value, axis);
            }
        });


        linechart.getAxisRight().setDrawGridLines(false); // Desactivar las líneas de la cuadrícula horizontal en el eje derecho
        linechart.getAxisRight().setDrawLabels(false); // Oculto el eje y izquierdo

        LineChartSingleton.setLineChart2(linechart); // Guardo el linechart del gráfico2 para luego poder almacenarlo en el PDF

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
                Intent intent = new Intent(Grafica2.this,resultadosValores.class);
                startActivity(intent);
            }
        });




    }
}
