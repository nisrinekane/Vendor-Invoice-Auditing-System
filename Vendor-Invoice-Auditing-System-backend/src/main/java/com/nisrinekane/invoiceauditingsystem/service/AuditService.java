package com.nisrinekane.invoiceauditingsystem.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import opennlp.tools.sentdetect.*;
import opennlp.tools.tokenize.SimpleTokenizer;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AuditService {
    @Autowired
    private FileUtils fileUtils;

    private SentenceDetectorME sentenceDetector;
    private byte[] pdfReport;

    @PostConstruct
    public void init() throws Exception {
        this.sentenceDetector = loadSentenceModel("src/main/resources/opennlp-en-ud-ewt-sentence-1.0-1.9.3.bin");
    }

    private SentenceDetectorME loadSentenceModel(String path) throws Exception {
        try (InputStream modelIn = new FileInputStream(path)) {
            SentenceModel model = new SentenceModel(modelIn);
            return new SentenceDetectorME(model);
        }
    }

    public String auditInvoiceAgainstContract(byte[] invoiceBytes, String invoiceExtension, byte[] contractBytes, String contractExtension) throws Exception {

        String invoiceText = FileUtils.readFileContentBasedOnExtension(invoiceBytes, invoiceExtension);
        String contractText = FileUtils.readFileContentBasedOnExtension(contractBytes, contractExtension);

        StringBuilder reportContent = new StringBuilder();

        reportContent.append("Audit Report for Invoice vs Contract\n");
        reportContent.append("Date: ").append(new SimpleDateFormat("MM/dd/yyyy").format(new java.util.Date())).append("\n\n");

        String invoiceCompany = extractAndReportEntity("Company", "Company: (.+?)\\n", invoiceText);
        String contractCompany = extractAndReportEntity("Company", "Company: (.+?)\\n", contractText);

        reportContent.append("--- Invoice Company: ").append(invoiceCompany).append(" ---\n");
        auditEntities(sentenceDetector.sentDetect(invoiceText), reportContent, "Invoice");

        reportContent.append("\n--- Contract Company: ").append(contractCompany).append(" ---\n");
        auditEntities(sentenceDetector.sentDetect(contractText), reportContent, "Contract");

        String invoiceAmount = extractAndReportEntity("Amount", "\\$\\d+", invoiceText);
        String contractAmount = extractAndReportEntity("Amount", "\\$\\d+", contractText);

        String invoiceDate = extractAndReportEntity("Date", "\\d{4}-\\d{2}-\\d{2}", invoiceText);
        String contractDate = extractAndReportEntity("Date", "\\d{4}-\\d{2}-\\d{2}", contractText);

        boolean discrepanciesFound = false;

        // compare amounts
        if (invoiceAmount != null && contractAmount != null && !invoiceAmount.equals(contractAmount)) {
            discrepanciesFound = true;
            reportContent.append("\nDiscrepancy: Invoice amount and contract amount do not match.\n");
        }

        // compare dates
        if (invoiceDate != null && contractDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date invDate = sdf.parse(invoiceDate);
            java.util.Date conDate = sdf.parse(contractDate);

            if (invDate.before(conDate)) {
                discrepanciesFound = true;
                reportContent.append("\nDiscrepancy: Invoice date is before contract date.\n");
            }
        }

        // recommendations
        reportContent.append("\nRecommendations: ");
        if (discrepanciesFound) {
            reportContent.append("Immediate attention required to resolve the discrepancies.\n");
        } else {
            reportContent.append("No action required.\n");
        }

        // generate PDF
        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
        PdfDocument pdfDocument = new PdfDocument(new PdfWriter(pdfOutputStream));
        Document document = new Document(pdfDocument);
        document.add(new Paragraph(reportContent.toString()));
        document.close();

        pdfReport = pdfOutputStream.toByteArray();

        return discrepanciesFound ? "Audit complete with discrepancies. PDF report generated." : "Audit complete. No discrepancies. PDF report generated.";
    }

    private String extractAndReportEntity(String entityLabel, String regexPattern, String sourceText) {
        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(sourceText);
        if (matcher.find()) {
            return matcher.group(0);
        } else {
            return "Not found";
        }
    }



    private void auditEntities(String[] sentences, StringBuilder reportContent, String source) {
        for (String sentence : sentences) {
            String[] tokens = SimpleTokenizer.INSTANCE.tokenize(sentence);
            reportContent.append("Source: ").append(source).append(", ").append(sentence).append("\n");
            appendCustomEntityInformation("Found dates", "\\d{4}-\\d{2}-\\d{2}", sentence, reportContent);
            appendCustomEntityInformation("Found amounts", "\\$\\d+", sentence, reportContent);
            appendCustomEntityInformation("Found percentages", "\\d+%\\b", sentence, reportContent);
            appendCustomEntityInformation("Found locations", "[A-Za-z]+, [A-Za-z]+", sentence, reportContent);
            appendCustomEntityInformation("Found persons", "[A-Z][a-z]+ [A-Z][a-z]+", sentence, reportContent);
        }
    }

    private void appendCustomEntityInformation(String message, String regexPattern, String sourceText, StringBuilder sb) {
        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(sourceText);
        sb.append(message).append(": ");
        if (matcher.find()) {
            sb.append(matcher.group(0));
        } else {
            sb.append("Not found");
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
