package com.nisrinekane.invoiceauditingsystem.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import opennlp.tools.namefind.*;
import opennlp.tools.sentdetect.*;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.util.Span;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
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
            this.sentenceDetector = loadSentenceModel("opennlp-en-ud-ewt-sentence-1.0-1.9.3.bin");
            this.dateFinder = loadNameFinderModel("en-ner-time.bin");
            this.moneyFinder = loadNameFinderModel("en-ner-money.bin");
            this.percentageFinder = loadNameFinderModel("en-ner-percentage.bin");
            this.timeFinder = loadNameFinderModel("en-ner-time.bin");
            this.locationFinder = loadNameFinderModel("en-ner-location.bin");
            this.personFinder = loadNameFinderModel("en-ner-person.bin");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize models", e);
        }
    }

    public String auditInvoiceAgainstContract(byte[] invoiceBytes, byte[] contractBytes) {
        try {
            String invoiceText = new String(invoiceBytes, StandardCharsets.UTF_8);
            String contractText = new String(contractBytes, StandardCharsets.UTF_8);

            String[] sentences = sentenceDetector.sentDetect(invoiceText);
            StringBuilder reportContent = new StringBuilder();

            for (String sentence : sentences) {
                String[] tokens = SimpleTokenizer.INSTANCE.tokenize(sentence);
                Span[] dateSpans = dateFinder.find(tokens);
                reportContent.append("Sentence: ").append(sentence).append("\n");
                reportContent.append("Found dates: ");
                for (Span span : dateSpans) {
                    reportContent.append(tokens[span.getStart()]).append(" ");
                }
                reportContent.append("\n");
            }

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

    public byte[] getPDFReport() {
        return pdfReport;
    }

    private SentenceDetectorME loadSentenceModel(String path) throws Exception {
        InputStream modelInput = getClass().getClassLoader().getResourceAsStream(path);
        SentenceModel sentenceModel = new SentenceModel(modelInput);
        return new SentenceDetectorME(sentenceModel);
    }

    private NameFinderME loadNameFinderModel(String path) throws Exception {
        InputStream modelInput = getClass().getClassLoader().getResourceAsStream(path);
        TokenNameFinderModel model = new TokenNameFinderModel(modelInput);
        return new NameFinderME(model);
    }
}
