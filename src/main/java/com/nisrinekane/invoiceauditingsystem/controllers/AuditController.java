package com.nisrinekane.invoiceauditingsystem.controllers;

import com.nisrinekane.invoiceauditingsystem.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.Future;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    @Autowired
    private AuditService auditService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadInvoiceAndContract(@RequestParam("invoice") MultipartFile invoice, @RequestParam("contract") MultipartFile contract) throws Exception {
        byte[] invoiceBytes = invoice.getBytes();
        byte[] contractBytes = contract.getBytes();
        Future<String> auditResult = auditService.auditInvoiceAgainstContract(invoiceBytes, contractBytes);
        String result = auditResult.get();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/generatePDF")
    public ResponseEntity<byte[]> generatePDF() {
        byte[] pdfBytes = auditService.getPDFReport();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=report.pdf");
        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(pdfBytes);
    }
}
