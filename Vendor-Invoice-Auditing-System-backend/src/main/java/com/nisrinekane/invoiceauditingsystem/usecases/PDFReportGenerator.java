package com.nisrinekane.invoiceauditingsystem.usecases;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

@Component
public class PDFReportGenerator {

    public byte[] generatePDFReport(String content) {
        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
        PdfDocument pdfDocument = new PdfDocument(new PdfWriter(pdfOutputStream));
        Document document = new Document(pdfDocument);
        document.add(new Paragraph(content));
        document.close();

        return pdfOutputStream.toByteArray();
    }
}
