package com.evhub.app.event;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class HeaderFooterPageEvent extends PdfPageEventHelper {

    private PdfTemplate pdfTemplate;
    private Image image;
    private String headerText;
    private boolean termsAndCondition;

    public Date getServiceDate() {
        return serviceDate;
    }

    public void setServiceDate(Date serviceDate) {
        this.serviceDate = serviceDate;
    }

    private Date serviceDate;

    public String getServiceNumber() {
        return serviceNumber;
    }

    public void setServiceNumber(String serviceNumber) {
        this.serviceNumber = serviceNumber;
    }

    private String serviceNumber;


    public String getHeaderText() {
        return headerText;
    }

    public void setHeaderText(String headerText) {
        this.headerText = headerText;
    }

    public HeaderFooterPageEvent(String headerText) {
        this.headerText = headerText;
    }

    public HeaderFooterPageEvent() {
    }

    public boolean isTermsAndCondition() {
        return termsAndCondition;
    }

    public void setTermsAndCondition(boolean termsAndCondition) {
        this.termsAndCondition = termsAndCondition;
    }

    public void onOpenDocument(PdfWriter writer, Document document) {
        pdfTemplate = writer.getDirectContent().createTemplate(30, 16);
        try {
            image = Image.getInstance(pdfTemplate);
            image.setRole(PdfName.ARTIFACT);
        } catch (DocumentException de) {
            throw new ExceptionConverter(de);
        }
    }

    public void onEndPage(PdfWriter writer, Document document) {
        addFooter(writer,document);
    }
//        onStartPage(writer,document);


    private void addFooter(PdfWriter writer,Document document) {
     /*   try {
            URL logoImage = this.getClass().getResource("/image/evhub.jpeg");
            Image leftImage = Image.getInstance(logoImage);
            Rectangle page = document.getPageSize();
                float x, y;
                int fontSize = 10;
                // Add left image to header
                x = 20;
                y = 22;
                leftImage.scaleToFit(50, 40); // Scale the image to fit in the header
                leftImage.setAbsolutePosition(x, y);
                document.add(leftImage);


                Font pageFont = new Font(Font.FontFamily.UNDEFINED, fontSize, Font.NORMAL, new BaseColor(89,89,89,255));
                y = page.getHeight() - document.topMargin() - 10;
                Font numberFont = new Font(Font.FontFamily.UNDEFINED, fontSize,Font.BOLD, BaseColor.BLACK);
                Phrase pagePhrase = new Phrase("Page ", pageFont);
//                Phrase last = new Phrase(,pageFont);

//                pageFont.setStyle(Font.BOLD);
                Phrase number = new Phrase(String.format("%d",writer.getPageNumber()),numberFont);
                pagePhrase.add(number);
                pagePhrase.add(new Phrase("/",pageFont));

                ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_RIGHT, pagePhrase, 555, 30, 0);
                PdfContentByte contentByte = writer.getDirectContent();
                contentByte.setLineWidth(0.5f);
                contentByte.setColorStroke(BaseColor.LIGHT_GRAY);
                contentByte.moveTo(20, 50);
                contentByte.lineTo(575, 50);
                contentByte.closePathStroke();
                contentByte.stroke();

            } catch (DocumentException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }*/
        PdfPTable footer = new PdfPTable(3);
        try {
            // set defaults
            footer.setWidths(new int[]{24, 2, 1});
            footer.setTotalWidth(550);
            footer.setHorizontalAlignment(Element.ALIGN_RIGHT);
            footer.setLockedWidth(true);
            footer.getDefaultCell().setFixedHeight(40);
            footer.getDefaultCell().setBorder(Rectangle.TOP);
            footer.getDefaultCell().setBorderColor(BaseColor.LIGHT_GRAY);
            URL logoImage = this.getClass().getResource("/image/evhub.jpeg");
            Image leftImage = Image.getInstance(logoImage);
            Rectangle page = document.getPageSize();
            float x, y;
            // Add left image to header
            x = 20;
            y = 22;
            leftImage.scaleToFit(30, 40);
            leftImage.setAbsolutePosition(x, y);

            PdfPCell cell = new PdfPCell();
            cell.setBorder(Rectangle.TOP);
            cell.setBorderColor(BaseColor.LIGHT_GRAY);
            footer.addCell(cell);
            document.add(leftImage);
            // add current page count
            footer.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            footer.addCell(new Phrase(String.format("Page %d/", writer.getPageNumber()), new Font(Font.FontFamily.HELVETICA, 8)));

            // add placeholder for total page count
            PdfPCell totalPageCount = new PdfPCell(image);
            totalPageCount.setBorder(Rectangle.TOP);
            totalPageCount.setBorderColor(BaseColor.LIGHT_GRAY);
            footer.addCell(totalPageCount);

            // write page
            PdfContentByte canvas = writer.getDirectContent();
            canvas.beginMarkedContentSequence(PdfName.ARTIFACT);
            footer.writeSelectedRows(-2, -2, 20, 50, canvas);
            canvas.endMarkedContentSequence();
        } catch (DocumentException de) {
            throw new ExceptionConverter(de);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void onStartPage(PdfWriter writer, Document document) {
        try {
            URL logoImage = this.getClass().getResource("/image/evhub.jpeg");
            URL scannerImage = this.getClass().getResource("/image/qrCode.png");
            Image leftImage = Image.getInstance(scannerImage);
            Image rightImage = Image.getInstance(logoImage);
            Rectangle page = document.getPageSize();
        if (!termsAndCondition) {
            float x, y;
            int fontSize = 15;
            // Add left image to header
            x = 20;
            y = page.getHeight() - document.topMargin() - 10;
            leftImage.scaleToFit(70, 40); // Scale the image to fit in the header
            leftImage.setAbsolutePosition(x, y);
            document.add(leftImage);


            // Add right image to header
//            x = 560;
            x = page.getWidth() - document.rightMargin() - 40;
            y = page.getHeight() - document.topMargin()-5;
            rightImage.scaleToFit(70, 130); // Scale the image to fit in the header
            rightImage.setAbsolutePosition(x, y);
            document.add(rightImage);

            // Add page number to header
//            Font headerFont = new Font(Font.FontFamily.UNDEFINED, fontSize, Font.NORMAL, new BaseColor(16,75,140,255));
            Font headerFont = FontFactory.getFont("Poppins-400",BaseFont.IDENTITY_H,BaseFont.EMBEDDED,16);
            Font serviceFont = FontFactory.getFont("Poppins-bold",BaseFont.IDENTITY_H,BaseFont.EMBEDDED,12);
           serviceFont.setColor(new BaseColor(89,89,89));
            headerFont.setColor(new BaseColor(16,75,140));
            y = page.getHeight() - document.topMargin() - 10;
//            Font dateFont = new Font(Font.FontFamily.UNDEFINED, 10,Font.NORMAL, BaseColor.BLACK);
            Font dateFont = FontFactory.getFont("Poppins-300",BaseFont.IDENTITY_H,BaseFont.EMBEDDED,12);
            dateFont.setColor(89,89,89);
            Phrase header = new Phrase("Electric Vehicle Diagnostic report", headerFont);
//            Font serviceFont = new Font(Font.FontFamily.UNDEFINED, 10, Font.BOLD, new BaseColor(89,89,89,255));
            Phrase service = new Phrase(serviceNumber, serviceFont);
            if (!ObjectUtils.isEmpty(serviceDate)) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(serviceDate);
                DateFormat dateFormat = getSimpleDateFormat(calendar.get(Calendar.DATE));
                service.setFont(dateFont);
                    service.add(" | " + dateFormat.format(serviceDate));
                }
                Phrase instance = new Phrase(header);
                instance.add(service);
                ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, header, 210, y + 20, 0);
                ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, service, 195, y + 3, 0);

            PdfContentByte contentByte = writer.getDirectContent();
            contentByte.setLineWidth(0.5f);
            contentByte.setColorStroke(BaseColor.LIGHT_GRAY);
            contentByte.moveTo(20, page.getHeight() - 70);
            contentByte.lineTo(575, page.getHeight() - 70);
            contentByte.closePathStroke();
            contentByte.stroke();

            PdfContentByte contentByte1= writer.getDirectContent();
            contentByte1.setLineWidth(0.5f);
            contentByte1.setColorStroke(BaseColor.LIGHT_GRAY);
            contentByte1.moveTo(69, 825);
            contentByte1.lineTo(69, 780);
            contentByte1.stroke();

        } else{
            float x, y;
            int fontSize = 13;
            // Add left image to header
            x = 45;
            y = page.getHeight() - document.topMargin() - 10;
            rightImage.scaleToFit(70, 40); // Scale the image to fit in the header
            rightImage.setAbsolutePosition(x, y);
            document.add(rightImage);
//            x = page.getWidth() - document.rightMargin() +30;
//            y = page.getHeight() - document.topMargin();
//           // rightImage.scaleToFit(100, 100); // Scale the image to fit in the header
//            rightImage.setAbsolutePosition(x, y);
//            document.add(rightImage);


            Font headerFont = new Font(Font.FontFamily.UNDEFINED, 22.0f, Font.BOLD, BaseColor.BLACK);
            y = page.getHeight() - document.topMargin() - 10;
            Phrase header1 = new Phrase("EV HUB Terms & Conditions", headerFont);
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, header1, 188, y - 40, 0);
            Font headerFont1 = new Font(Font.FontFamily.UNDEFINED, 15.0f, Font.NORMAL, BaseColor.BLACK);
            Phrase header2 = new Phrase("for EV/Hybrid Vehicles ", headerFont1);
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, header2, 114, y - 63, 0);

          //  ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, service, 150, y + 8, 0);
        }
//        String p = "new Page";
//        ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, Phrase.getInstance(p), 140, y, 0);
            } catch (DocumentException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
//        } else {
//            document.setMargins(document.leftMargin(), document.rightMargin(), document.bottomMargin(), -20);
//        }

    }

    @NotNull
    private static SimpleDateFormat getSimpleDateFormat(Integer day) {
       switch (day){
           case 1:
               return new SimpleDateFormat("dd'st' MMMM yyyy");
           case 2:
               return new SimpleDateFormat("dd'nd' MMMM yyyy");
           case 3:
               return new SimpleDateFormat("dd'rd' MMMM yyyy");
           default:
               return new SimpleDateFormat("dd'th' MMMM yyyy");

       }
    }
//    public void onStartPage(PdfWriter writer, Document document) {
//        ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, new Phrase(headerText), 550, 800, 0);
//    }

    public void onCloseDocument(PdfWriter writer, Document document) {
        int totalLength = String.valueOf(writer.getPageNumber()).length();
        int totalWidth = totalLength * 5;
        ColumnText.showTextAligned(pdfTemplate, Element.ALIGN_RIGHT, new Phrase(String.valueOf(writer.getPageNumber()), new Font(Font.FontFamily.HELVETICA, 8)), totalWidth, 6, 0);
    }

}