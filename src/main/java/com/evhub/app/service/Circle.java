package com.evhub.app.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

public class Circle {

    public void createCircle(PdfWriter writer, float x, float y, float radius, float width, int percentage, BaseColor baseColor) throws DocumentException {

        int blue = baseColor.getBlue();
        int green = baseColor.darker().getGreen();
        int red = baseColor.getRed();
        PdfContentByte cb = writer.getDirectContent();
        float martingale = 89;
        // cb.roundRectangle(x-2 ,y +radius-5,10f,10f,5);
        cb.setColorFill(new BaseColor(253,41,2));//253,41,1
        cb.fill();

        for (int i = 0; i < percentage + 4; i++) {
            float angle = 3.6f;
            cb.setColorStroke(new BaseColor(red, green, blue));
            cb.setLineWidth(width);
            cb.arc(x - radius, y - radius, x + radius, y + radius, martingale, -angle);
            cb.stroke();
            martingale = martingale - 3.30f;

            red -= 2;
            green += 2;

            if(blue != 20) { blue += 1;}
        }
        if (percentage == 100) {
            cb.roundRectangle(x - 15, y + radius - 6, 12f, 10f, 6);
            cb.setColorFill(new BaseColor(red, green, blue));
            cb.fill();
        }
        addPercentage(writer, percentage, new BaseColor(red, green, blue));
    }

    private void addPercentage(PdfWriter writer, int percentage, BaseColor baseColor) throws DocumentException {
        ColumnText column = new ColumnText(writer.getDirectContent());
//        column.setSimpleColumn(480,530,590,720);
        column.setSimpleColumn(505, 530, 590, 760);
        if (percentage < 10) {
            column.setSimpleColumn(510, 530, 590, 760);
        }
        if (percentage == 100) {
            column.setSimpleColumn(498, 530, 590, 760);
        }

        Font percentageFont = new Font(Font.FontFamily.UNDEFINED, 30, Font.BOLD, baseColor);

        column.addElement(new Paragraph(String.valueOf(percentage), percentageFont));
        column.go();
        Font font = new Font(Font.FontFamily.UNDEFINED, 10, Font.BOLD, BaseColor.GRAY);
        column.setSimpleColumn(500, 530, 590, 720);
        column.addElement(new Paragraph("out of 100", font));
        column.go();
    }

    public void createCircle(PdfWriter writer, float x, float y, float radius, float width, BaseColor baseColor) {
        PdfContentByte cb = writer.getDirectContent();
        cb.setColorStroke(baseColor);
        cb.setLineWidth(width);
        cb.circle(x - radius, y - radius, radius);
        cb.stroke();
    }
}
