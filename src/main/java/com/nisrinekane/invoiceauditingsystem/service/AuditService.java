package com.nisrinekane.invoiceauditingsystem.service;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.util.Span;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class AuditService {

    private byte[] pdfReport;

    public Future<String> auditInvoiceAgainstContract(byte[] invoiceBytes, byte[] contractBytes) {
        return Executors.newSingleThreadExecutor().submit(new AuditRunnable(invoiceBytes, contractBytes));
    }

    private class AuditRunnable implements Callable<String> {

        private byte[] invoiceBytes;
        private byte[] contractBytes;

        public AuditRunnable(byte[] invoiceBytes, byte[] contractBytes) {
            this.invoiceBytes = invoiceBytes;
            this.contractBytes = contractBytes;
        }

        @Override
        public String call() throws Exception {
            String invoiceText = new String(invoiceBytes);
            String contractText = new String(contractBytes);

            // Initialize sentence model and detector
            InputStream sentenceInputStream = getClass().getResourceAsStream("/en-sent.bin");
            SentenceModel sentenceModel = new SentenceModel(sentenceInputStream);
            SentenceDetectorME sentenceDetector = new SentenceDetectorME(sentenceModel);

            String[] invoiceSentences = sentenceDetector.sentDetect(invoiceText);
            String[] contractSentences = sentenceDetector.sentDetect(contractText);

            NameFinderME dateFinder = loadModel("/en-ner-date.bin");
            NameFinderME moneyFinder = loadModel("/en-ner-money.bin");

            SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;

            Map<String, String> invoiceEntities = new HashMap<>();
            Map<String, String> contractEntities = new HashMap<>();
            Map<String, String> anomalies = new HashMap<>();

            processText(invoiceSentences, tokenizer, new NameFinderME[]{dateFinder, moneyFinder}, invoiceEntities);
            processText(contractSentences, tokenizer, new NameFinderME[]{dateFinder, moneyFinder}, contractEntities);

            checkAnomalies(invoiceEntities, contractEntities, anomalies);

            pdfReport = generatePDFReport(invoiceEntities, contractEntities, anomalies);

            return "Audit completed";
        }

        private NameFinderME loadModel(String path) throws Exception {
            InputStream modelInput = getClass().getResourceAsStream(path);
            TokenNameFinderModel model = new TokenNameFinderModel(modelInput);
            return new NameFinderME(model);
        }

        private void processText(String[] sentences, SimpleTokenizer tokenizer, NameFinderME[] finders, Map<String, String> entities) {
            for (String sentence : sentences) {
                String[] tokens = tokenizer.tokenize(sentence);
                for (NameFinderME finder : finders) {
                    Span[] spans = finder.find(tokens);
                    for (Span span : spans) {
                        String entity = tokens[span.getStart()];
                        entities.put(entity, span.getType());
                    }
                }
            }
        }

        private void checkAnomalies(Map<String, String> invoiceEntities, Map<String, String> contractEntities, Map<String, String> anomalies) {
            for (String key : invoiceEntities.keySet()) {
                if (!contractEntities.containsKey(key)) {
                    anomalies.put(key, "Missing in contract");
                }
            }

            for (String key : contractEntities.keySet()) {
                if (!invoiceEntities.containsKey(key)) {
                    anomalies.put(key, "Missing in invoice");
                }
            }
        }

        private byte[] generatePDFReport(Map<String, String> invoiceEntities, Map<String, String> contractEntities, Map<String, String> anomalies) throws DocumentException {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(new Paragraph("Audit Report"));
            document.add(new Paragraph("Invoice: " + invoiceEntities));
            document.add(new Paragraph("Contract: " + contractEntities));
            document.add(new Paragraph("Anomalies: " + anomalies));
            document.close();
            return out.toByteArray();
        }
    }

    public byte[] getPDFReport() {
        return this.pdfReport;
    }
}
