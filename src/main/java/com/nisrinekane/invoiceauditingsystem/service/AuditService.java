package com.nisrinekane.invoiceauditingsystem.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import opennlp.tools.namefind.*;
import opennlp.tools.sentdetect.*;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.util.Span;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
public class AuditService {

    private SentenceDetectorME sentenceDetector;
    private NameFinderME dateFinder;
    private NameFinderME moneyFinder;
    private NameFinderME percentageFinder;
    private NameFinderME timeFinder;
    private NameFinderME locationFinder;
    private NameFinderME personFinder;

    private byte[] pdfReport;

    @PostConstruct
    public void init() {
        try {
            this.sentenceDetector = loadSentenceModel("src/main/resources/opennlp-en-ud-ewt-sentence-1.0-1.9.3.bin");
            this.dateFinder = loadNameFinderModel("src/main/resources/en-ner-person.bin");
            this.moneyFinder = loadNameFinderModel("src/main/resources/en-ner-money.bin");
            this.percentageFinder = loadNameFinderModel("src/main/resources/en-ner-percentage.bin");
            this.timeFinder = loadNameFinderModel("src/main/resources/en-ner-time.bin");
            this.locationFinder = loadNameFinderModel("src/main/resources/en-ner-location.bin");
            this.personFinder = loadNameFinderModel("src/main/resources/en-ner-person.bin");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize models", e);
        }
    }

    private SentenceDetectorME loadSentenceModel(String path) throws Exception {
        try (InputStream modelIn = new FileInputStream(path)) {
            SentenceModel model = new SentenceModel(modelIn);
            return new SentenceDetectorME(model);
        }
    }

    private NameFinderME loadNameFinderModel(String path) throws Exception {
        try (InputStream modelIn = new FileInputStream(path)) {
            TokenNameFinderModel model = new TokenNameFinderModel(modelIn);
            return new NameFinderME(model);
        }
    }

    public String auditInvoiceAgainstContract(byte[] invoiceBytes, String invoiceExtension, byte[] contractBytes, String contractExtension) {
        try {
            String invoiceText = readFileContentBasedOnExtension(invoiceBytes, invoiceExtension);
            String contractText = readFileContentBasedOnExtension(contractBytes, contractExtension);

            StringBuilder reportContent = new StringBuilder();

            auditEntities(sentenceDetector.sentDetect(invoiceText), reportContent, "Invoice");
            auditEntities(sentenceDetector.sentDetect(contractText), reportContent, "Contract");

            ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
            PdfDocument pdfDocument = new PdfDocument(new PdfWriter(pdfOutputStream));
            Document document = new Document(pdfDocument);
            document.add(new Paragraph(reportContent.toString()));
            document.close();

            pdfReport = pdfOutputStream.toByteArray();

            return "Audit complete. PDF report generated.";
        } catch (Exception e) {
            return "Failed to perform the audit: " + e.getMessage();
        }
    }

    private void auditEntities(String[] sentences, StringBuilder reportContent, String source) {
        for (String sentence : sentences) {
            String[] tokens = SimpleTokenizer.INSTANCE.tokenize(sentence);
            reportContent.append("Source: ").append(source).append(", Sentence: ").append(sentence).append("\n");
            appendEntityInformation("Found dates", dateFinder, tokens, reportContent);
            appendEntityInformation("Found money amounts", moneyFinder, tokens, reportContent);
            appendEntityInformation("Found percentages", percentageFinder, tokens, reportContent);
            appendEntityInformation("Found times", timeFinder, tokens, reportContent);
            appendEntityInformation("Found locations", locationFinder, tokens, reportContent);
            appendEntityInformation("Found persons", personFinder, tokens, reportContent);
        }
    }

    private void appendEntityInformation(String message, NameFinderME finder, String[] tokens, StringBuilder sb) {
        Span[] spans = finder.find(tokens);
        sb.append(message).append(": ");
        for (Span span : spans) {
            sb.append(tokens[span.getStart()]).append(" ");
        }
        sb.append("\n");
    }

    private String readFileContentBasedOnExtension(byte[] fileBytes, String extension) throws Exception {
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

    public byte[] getPDFReport() {
        return pdfReport;
    }
}
