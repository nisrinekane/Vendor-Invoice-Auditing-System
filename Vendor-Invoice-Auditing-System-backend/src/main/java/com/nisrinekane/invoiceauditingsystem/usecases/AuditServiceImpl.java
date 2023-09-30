package com.nisrinekane.invoiceauditingsystem.usecases;

import com.nisrinekane.invoiceauditingsystem.port.FileUtilsPort;
import opennlp.tools.sentdetect.*;
import opennlp.tools.tokenize.SimpleTokenizer;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import opennlp.tools.util.Span;


import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;

@Service
public class AuditServiceImpl implements AuditService {

    @Autowired
    private FileUtilsPort fileUtils;

    @Autowired
    private PDFReportGenerator pdfReportGenerator;

    private SentenceDetectorME sentenceDetector;
    private NameFinderME organizationFinder;
    private NameFinderME personFinder;
    private NameFinderME timeFinder;
    private NameFinderME locationFinder;
    private NameFinderME moneyFinder;
    private NameFinderME percentageFinder;
    private byte[] pdfReport;

    @PostConstruct
    public void init() throws Exception {
        this.sentenceDetector = loadSentenceModel("/opennlp-en-ud-ewt-sentence-1.0-1.9.3.bin");
        this.organizationFinder = loadNameFinderModel("/en-ner-organization.bin");
        this.personFinder = loadNameFinderModel("/en-ner-person.bin");
        this.timeFinder = loadNameFinderModel("/en-ner-time.bin");
        this.locationFinder = loadNameFinderModel("/en-ner-location.bin");
        this.moneyFinder = loadNameFinderModel("/en-ner-money.bin");
        this.percentageFinder = loadNameFinderModel("/en-ner-percentage.bin");

    }

    private SentenceDetectorME loadSentenceModel(String path) throws Exception {
        try (InputStream modelIn = getClass().getResourceAsStream(path)) {
            SentenceModel model = new SentenceModel(modelIn);
            return new SentenceDetectorME(model);
        }
    }

    private NameFinderME loadNameFinderModel(String path) throws Exception {
        try (InputStream modelIn = getClass().getResourceAsStream(path)) {
            TokenNameFinderModel model = new TokenNameFinderModel(modelIn);
            return new NameFinderME(model);
        }
    }

    // Your auditInvoiceAgainstContract method here...

    private String extractEntity(String[] tokens, NameFinderME finder) {
        Span[] spans = finder.find(tokens);
        if (spans.length > 0) {
            return spans[0].getCoveredText(String.join(" ", tokens)).toString();
        }
        return "Not found";
    }

    private void auditEntities(String[] sentences, StringBuilder reportContent, String source) {
        for (String sentence : sentences) {
            String[] tokens = SimpleTokenizer.INSTANCE.tokenize(sentence);
            reportContent.append("Source: ").append(source).append(", ").append(sentence).append("\n");
            reportContent.append("Found dates: ").append(extractEntity(tokens, timeFinder)).append("\n");
            reportContent.append("Found amounts: ").append(extractEntity(tokens, moneyFinder)).append("\n");
            reportContent.append("Found percentages: ").append(extractEntity(tokens, percentageFinder)).append("\n");
            reportContent.append("Found locations: ").append(extractEntity(tokens, locationFinder)).append("\n");
            reportContent.append("Found persons: ").append(extractEntity(tokens, personFinder)).append("\n");
            reportContent.append("Found organizations: ").append(extractEntity(tokens, organizationFinder)).append("\n");
        }
    }

    @Override
    public String auditInvoiceAgainstContract(byte[] invoiceBytes, String invoiceExtension,
                                              byte[] contractBytes, String contractExtension) throws Exception {

        String invoiceText = fileUtils.readFileContentBasedOnExtension(invoiceBytes, invoiceExtension);
        String contractText = fileUtils.readFileContentBasedOnExtension(contractBytes, contractExtension);

        StringBuilder reportContent = new StringBuilder();
        reportContent.append("Audit Report for Invoice vs Contract\n");
        reportContent.append("Date: ").append(new SimpleDateFormat("MM/dd/yyyy").format(new java.util.Date())).append("\n\n");

        auditEntities(sentenceDetector.sentDetect(invoiceText), reportContent, "Invoice");
        auditEntities(sentenceDetector.sentDetect(contractText), reportContent, "Contract");

        String invoiceAmount = extractEntity(SimpleTokenizer.INSTANCE.tokenize(invoiceText), moneyFinder);
        String contractAmount = extractEntity(SimpleTokenizer.INSTANCE.tokenize(contractText), moneyFinder);

        String invoiceDate = extractEntity(SimpleTokenizer.INSTANCE.tokenize(invoiceText), timeFinder);
        String contractDate = extractEntity(SimpleTokenizer.INSTANCE.tokenize(contractText), timeFinder);

        boolean discrepanciesFound = false;

        if (!"Not found".equals(invoiceAmount) && !"Not found".equals(contractAmount) && !invoiceAmount.equals(contractAmount)) {
            discrepanciesFound = true;
            reportContent.append("\nDiscrepancy: Invoice amount and contract amount do not match.\n");
        }

        if (!"Not found".equals(invoiceDate) || !"Not found".equals(contractDate)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            try {
                java.util.Date invDate = sdf.parse(invoiceDate);
                java.util.Date conDate = sdf.parse(contractDate);

                if (invDate.before(conDate)) {
                    discrepanciesFound = true;
                    reportContent.append("\nDiscrepancy: Invoice date is before contract date.\n");
                }
            } catch (ParseException pe) {
                discrepanciesFound = true;
                reportContent.append("\nDiscrepancy: Unable to parse date from invoice or contract.\n");
            }
        }

        reportContent.append("\nRecommendations: ");
        if (discrepanciesFound) {
            reportContent.append("Immediate attention required to resolve the discrepancies.\n");
        } else {
            reportContent.append("No action required.\n");
        }

        pdfReport = pdfReportGenerator.generatePDFReport(reportContent.toString());

        return discrepanciesFound ? "Audit complete with discrepancies. PDF report generated." : "Audit complete. No discrepancies. PDF report generated.";
    }

    @Override
    public byte[] getPDFReport() {
        return pdfReport;
    }
}