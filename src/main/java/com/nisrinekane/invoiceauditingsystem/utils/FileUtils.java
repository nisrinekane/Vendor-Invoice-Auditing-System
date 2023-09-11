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

    public static String readFileContent(Path path) throws IOException {
        String extension = getFileExtension(path.toString());
        return switch (extension) {
            case "txt" -> readTxtFile(path);
            case "pdf" -> readPdfFile(path);
            case "docx" -> readDocxFile(path);
            default -> throw new UnsupportedOperationException(extension + " File type is not supported.");
        };
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
