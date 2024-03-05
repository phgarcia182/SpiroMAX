package com.example.myapplication;

import com.github.mikephil.charting.charts.LineChart;

public class LineChartSingleton {
    private static LineChart lineChart1;
    private static LineChart lineChart2;

    public static LineChart getLineChart1() {
        //Para obtener el gráfico 1
        return lineChart1;
    }

    public static LineChart getLineChart2() {
        //Para obtener el gráfico 2
        return lineChart2;
    }

    public static void setLineChart1(LineChart chart1){
        lineChart1= chart1;
    }

    public static void setLineChart2(LineChart chart2){
        lineChart2= chart2;
    }
}
