package com.nisrinekane.invoiceauditingsystem.controllers;

import com.nisrinekane.invoiceauditingsystem.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
            return "Error occurred: " + e.getMessage();
        }
    }

    @GetMapping("/report")
    public byte[] getAuditReport() {
        return auditService.getPDFReport();
    }
}
