package com.nisrinekane.invoiceauditingsystem.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import opennlp.tools.tokenize.SimpleTokenizer;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AuditService {

    private SentenceDetectorME sentenceDetector;
    private byte[] pdfReport;

    @PostConstruct
    public void init() throws Exception {
        try (InputStream modelIn = new FileInputStream("src/main/resources/opennlp-en-ud-ewt-sentence-1.0-1.9.3.bin")) {
            SentenceModel model = new SentenceModel(modelIn);
            this.sentenceDetector = new SentenceDetectorME(model);
        }
    }

    public String auditInvoiceAgainstContract(byte[] invoiceBytes, String invoiceExtension, byte[] contractBytes, String contractExtension) throws Exception {
        String invoiceText = readFileContentBasedOnExtension(invoiceBytes, invoiceExtension);
        String contractText = readFileContentBasedOnExtension(contractBytes, contractExtension);

        StringBuilder reportContent = new StringBuilder();

        auditEntities(sentenceDetector.sentDetect(invoiceText), reportContent, "Invoice");
        auditEntities(sentenceDetector.sentDetect(contractText), reportContent, "Contract");

        int invoiceAmount = 0;
        int contractAmount = 0;

        try {
            invoiceAmount = extractAmount("\\$\\d+", invoiceText);
            reportContent.append("Invoice amount: ").append(invoiceAmount).append("\n");
        } catch (ParseException e) {
            reportContent.append("Invoice amount: Not found\n");
        }

        try {
            contractAmount = extractAmount("\\$\\d+", contractText);
            reportContent.append("Contract amount: ").append(contractAmount).append("\n");
        } catch (ParseException e) {
            reportContent.append("Contract amount: Not found\n");
        }

        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
        PdfDocument pdfDocument = new PdfDocument(new PdfWriter(pdfOutputStream));
        Document document = new Document(pdfDocument);

        document.add(new Paragraph("Audit Report"));
        document.add(new Paragraph("Date: " + new SimpleDateFormat("MM/dd/yyyy").format(new Date())));

        boolean discrepanciesFound = false;
        if (invoiceAmount != contractAmount) {
            document.add(new Paragraph("Summary: DISCREPANCY DETECTED. Invoice amount does not match the contract amount."));
            discrepanciesFound = true;
        } else {
            document.add(new Paragraph("Summary: No discrepancies found between invoice and contract."));
        }

        document.add(new Paragraph("Detailed Findings"));
        document.add(new Paragraph(reportContent.toString()));

        if (discrepanciesFound) {
            document.add(new Paragraph("Recommendations: Immediate attention required to resolve the discrepancies."));
        } else {
            document.add(new Paragraph("Recommendations: No action required."));
        }

        document.close();
        pdfReport = pdfOutputStream.toByteArray();

        return discrepanciesFound ? "Audit complete with discrepancies. PDF report generated." : "Audit complete. No discrepancies. PDF report generated.";
    }

    private int extractAmount(String regex, String text) throws ParseException {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String amountString = matcher.group().substring(1);  // Skip the dollar sign
            return Integer.parseInt(amountString);
        }
        throw new ParseException("Amount not found", 0);
    }

    private void auditEntities(String[] sentences, StringBuilder reportContent, String source) {
        for (String sentence : sentences) {
            String[] tokens = SimpleTokenizer.INSTANCE.tokenize(sentence);
            reportContent.append("Source: ").append(source).append(", Sentence: ").append(sentence).append("\n");
        }
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
