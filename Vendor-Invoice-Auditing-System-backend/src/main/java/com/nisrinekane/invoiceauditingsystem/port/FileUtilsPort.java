package com.nisrinekane.invoiceauditingsystem.port;

public interface FileUtilsPort {
    String readFileContentBasedOnExtension(byte[] fileBytes, String extension) throws Exception;
}
