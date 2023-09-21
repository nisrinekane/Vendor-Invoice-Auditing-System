package com.nisrinekane.invoiceauditingsystem.adapters;

import com.nisrinekane.invoiceauditingsystem.port.FileUtilsPort;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
public class FileUtilsImpl implements FileUtilsPort {
    @Override
    public String readFileContentBasedOnExtension(byte[] fileBytes, String extension) throws Exception {
        switch (extension.toLowerCase()) {
            case "txt":
                return new String(fileBytes, StandardCharsets.UTF_8);
            case "pdf":
                PdfDocument pdfDocument = new PdfDocument(new PdfReader(new ByteArrayInputStream(fileBytes)));
                return PdfTextExtractor.getTextFromPage(pdfDocument.getPage(1));
            case "doc":
            case "docx":
                InputStream is = new ByteArrayInputStream(fileBytes);
                XWPFDocument doc = new XWPFDocument(is);
                XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
                return extractor.getText();
            default:
                throw new IllegalArgumentException("Unsupported file extension: " + extension);
        }
    }
}
