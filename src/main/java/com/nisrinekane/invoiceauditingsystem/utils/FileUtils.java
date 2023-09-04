package com.nisrinekane.invoiceauditingsystem.utils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.Loader;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;

@Component
public class FileUtils {
    public static String readFileContent(Path path) throws IOException {
        String extension = getFileExtension(path.toString());
//        use file extension to choose the right method to read the file
        return switch (extension) {
            case "txt" -> readTxtFile(path);
            case "pdf" -> readPdfFile(path);
            case "docx" -> readDocxFile(path);
            default -> throw new UnsupportedOperationException(extension + " File type is not supported.");
        };
    }

//    get file extension from file name
    private static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }

//    read text:
    private static String readTxtFile(Path path) throws IOException {
        return new String(Files.readAllBytes(path));
    }

//    read pdf:
    private static String readPdfFile(Path path) throws IOException {
        try (PDDocument document = Loader.loadPDF(new File(path.toUri()))) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private static String readDocxFile(Path path) throws IOException {
        FileInputStream fis = new FileInputStream(new File(path.toString()));
        XWPFDocument document = new XWPFDocument(fis);
        XWPFWordExtractor extractor = new XWPFWordExtractor(document);
        return extractor.getText();
    }
}
