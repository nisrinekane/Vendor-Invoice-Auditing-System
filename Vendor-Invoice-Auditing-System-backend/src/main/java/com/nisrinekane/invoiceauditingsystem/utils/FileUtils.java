package com.nisrinekane.invoiceauditingsystem.utils;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.kernel.pdf.canvas.parser.listener.SimpleTextExtractionStrategy;

import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class FileUtils {

    public static String readFileContentBasedOnExtension(byte[] fileBytes, String extension) throws IOException {
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

    private static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }

    private static String readTxtFile(Path path) throws IOException {
        return new String(Files.readAllBytes(path));
    }

    private static String readPdfFile(Path path) throws IOException {
        PdfDocument pdfDoc = new PdfDocument(new PdfReader(path.toString()));
        StringBuilder text = new StringBuilder();
        int numberOfPages = pdfDoc.getNumberOfPages();

        for (int i = 1; i <= numberOfPages; i++) {
            text.append(PdfTextExtractor.getTextFromPage(pdfDoc.getPage(i), new SimpleTextExtractionStrategy()));
        }

        pdfDoc.close();
        return text.toString();
    }

    private static String readDocxFile(Path path) throws IOException {
        FileInputStream fis = new FileInputStream(new File(path.toString()));
        XWPFDocument document = new XWPFDocument(fis);
        XWPFWordExtractor extractor = new XWPFWordExtractor(document);
        return extractor.getText();
    }
}
