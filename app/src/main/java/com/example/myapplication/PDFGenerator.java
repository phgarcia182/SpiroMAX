package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.UnitValue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PDFGenerator {

    public static void generatePDF(Context context, String fileName){
        //Verificar si hay almacenamiento disponible
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state)){
            //Obtener la carpeta de documentos
            File externalFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            //File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);


            // Crear el archivo PDF en blanco en la carpeta de descargas
            File file = new File(externalFilesDir, fileName);
            addContentToPDF(file);

            try {
                if (file.createNewFile()){
                    Log.d("PDFGenerator", "Archivo PDF creado correctamente");
                }
            } catch (IOException e){
                Log.e("PDFGenerator", "Error al crear el archivo PDF:" + e.getMessage());
            }

            // Puedes agregar contenido al PDF aquí si es necesario

        } else {
            Log.e("PDFGenerator", "El almacenamiento externo no está disponible");
        }
    }

    private static void addContentToPDF(File file){
     try {
         // Crear un escritor PDF
         PdfWriter writer = new PdfWriter(file);

         // Crear un documento PDF
         PdfDocument pdf = new PdfDocument(writer);
         Document document = new Document(pdf);

         // Crear un nuevo objeto PdfFont para establecer el tipo de fuente
         PdfFont fontTitle = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
         PdfFont fontNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);

         // Obtengo datos del paciente, del médico y la Fecha con su hora
         String[] Datos = Guardar_Datos.getDatos(); // Los datos del paciente (Son 6)
         String[] Resultados = resultadosValores.getDatos(); // Los resultados (FEV1, FVC, PEF y ratio)
         String Diagnostico = resultadosValores.getDiagnostico(); // El string que muestra el problema respiratorio
         String FechaYHoraActual = getCurrentDateTime();

         String tituloText = "Informe de Resultados:";
         String descriptionText = "Fecha del Estudio: " + FechaYHoraActual + "\nNombre: "+ Datos[0]
                 + "\nApellido: "+ Datos[1] + "\nFecha de Nacimiento: "+ Datos[2] + "\nAltura: "
                 + Datos[3] + " metros\nPeso: "+ Datos[4] + " Kg\nSexo: "+ Datos[5]
                 + "\nEstado de Salud: " + Diagnostico;


         // Agregar Título al documento
         document.add(new Paragraph(tituloText).setFont(fontTitle).setFontSize(20));

         // Agregar Texto al documento
         document.add(new Paragraph(descriptionText).setFont(fontNormal).setFontSize(14));

         // Agregar un espacio en blanco entre texto y tabla
         document.add(new Paragraph("\n"));
         document.add(new Paragraph("\n"));

         // Dibujar la tabla
         List<String> columnHeaders = new ArrayList<>();
         columnHeaders.add("FEV1");
         columnHeaders.add("FVC");
         columnHeaders.add("PEF");
         columnHeaders.add("FEV1/FVC");

         List<String> row = new ArrayList<>();
         row.add(Resultados[0]);
         row.add(Resultados[1]);
         row.add(Resultados[2]);
         row.add(Resultados[3]);


         // Crear la tabla
         Table table = new Table(columnHeaders.size());
         table.setWidth(UnitValue.createPercentValue(100));

         // Agregar encabezados de columna a la tabla
         for (String columnHeader : columnHeaders){
             table.addHeaderCell(new Cell().add(new Paragraph(columnHeader).setFont(fontNormal).setFontSize(16)));
         }

         // Agregar filas a la tabla
         for (String data : row){
            table.addCell(new Cell().add(new Paragraph(data).setFont(fontNormal).setFontSize(14)));
         }

         // Agregar la tabla al documento
         document.add(table);


         // Obtener el ancho y alto de la página
         float pageWidth = pdf.getDefaultPageSize().getWidth();
         float pageHeight = pdf.getDefaultPageSize().getHeight();

         // Definir el tamaño deseado para la imagen (un cuarto de la página)
         float desiredWidth = pageWidth / 2;
         float desiredHeight = pageHeight / 2;

         // Agregar gráfico 1 al documento
         LineChart lineChart1 = LineChartSingleton.getLineChart1();
         Bitmap resizedBitmap = Bitmap.createScaledBitmap(lineChart1.getChartBitmap(), (int) desiredWidth, (int) desiredHeight, true);

         // Agregar la imagen del gráfico 1 al documento
         Image chartImage1 = new Image(ImageDataFactory.create(convertBitmapToByteArray(resizedBitmap)));
         chartImage1.setFixedPosition(0, 0);  // Ajusta las coordenadas según sea necesario
         document.add(chartImage1);


         // Agregar gráfico 2 al documento
         LineChart lineChart2 = LineChartSingleton.getLineChart2();
         Bitmap resizedBitmap2 = Bitmap.createScaledBitmap(lineChart2.getChartBitmap(), (int) desiredWidth, (int) desiredHeight, true);


         // Agregar la imagen del gráfico 2 al documento
         Image chartImage2 = new Image(ImageDataFactory.create(convertBitmapToByteArray(resizedBitmap2)));
         chartImage2.setFixedPosition(pageWidth-desiredWidth, 0);  // Ajusta las coordenadas según sea necesario
         document.add(chartImage2);



         //Cerrar el documento
         document.close();

     } catch (IOException e){
         Log.e("PDFGenerator","Error al agregar contenido al PDF:" +e.getMessage());
     }
    }

    private static byte[] convertBitmapToByteArray(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    private static String getCurrentDateTime() {
        // Obtener la fecha y hora actual
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }
}
