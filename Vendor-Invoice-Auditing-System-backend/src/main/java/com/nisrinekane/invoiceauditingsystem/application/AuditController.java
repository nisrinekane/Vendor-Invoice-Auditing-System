package com.nisrinekane.invoiceauditingsystem.controllers;

import com.nisrinekane.invoiceauditingsystem.exceptions.ResourceNotFoundException;
import com.nisrinekane.invoiceauditingsystem.usecases.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ContentDisposition;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuditController {

    @Autowired
    private AuditService auditService;

    @PostMapping("/upload")
    public String uploadFiles(@RequestParam("invoice") MultipartFile invoice,
                              @RequestParam("contract") MultipartFile contract,
                              @RequestParam("invoiceExtension") String invoiceExtension,
                              @RequestParam("contractExtension") String contractExtension) {
        try {
            byte[] invoiceBytes = invoice.getBytes();
            byte[] contractBytes = contract.getBytes();
            return auditService.auditInvoiceAgainstContract(invoiceBytes, invoiceExtension, contractBytes, contractExtension);
        } catch (Exception e) {
            throw new ResourceNotFoundException("Error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/report")
    public ResponseEntity<byte[]> getAuditReport() {
        byte[] pdfBytes = auditService.getPDFReport();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.builder("inline; filename=report.pdf").build());
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    // Catch-all route to handle unrecognized routes
    @GetMapping("/**")
    public void handleAllOtherRoutes() {
        throw new ResourceNotFoundException("Resource not found");
    }

}

