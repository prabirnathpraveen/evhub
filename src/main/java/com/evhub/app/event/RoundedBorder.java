package com.evhub.app.event;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPCellEvent;
import com.itextpdf.text.pdf.PdfPTable;

public class RoundedBorder implements PdfPCellEvent {

   private BaseColor backgroundColour;
    private float[] positions;
    public RoundedBorder(BaseColor backgroundColour,float[] positions) {
        this.backgroundColour = backgroundColour;
        this.positions=positions;
    }


    @Override
    public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
        cell.setBorder(Rectangle.NO_BORDER);
        PdfContentByte canvas = canvases[PdfPTable.BACKGROUNDCANVAS];

//        float x = position.getLeft() + 1;
//        float y = position.getTop() - 30;
//        float w = position.getWidth() - 2;
//        float h = position.getHeight() - 10;
//        float r = 5;
        float x = position.getLeft() + positions[0];
        float y = position.getTop() + positions[1];
        float w = position.getWidth() + positions[2];
        float h = position.getHeight() + positions[3];
        float r = 5;

        canvas.roundRectangle(x, y, w, h, r);
        canvas.setColorFill(backgroundColour);
        canvas.fill();
        canvas.stroke();
    }

}
