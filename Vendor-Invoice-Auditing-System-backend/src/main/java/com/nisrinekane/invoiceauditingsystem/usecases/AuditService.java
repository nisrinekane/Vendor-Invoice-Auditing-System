package com.nisrinekane.invoiceauditingsystem.usecases;

public interface AuditService {
    String auditInvoiceAgainstContract(byte[] invoiceBytes, String invoiceExtension, byte[] contractBytes, String contractExtension) throws Exception;
    byte[] getPDFReport();
}
