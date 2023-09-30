import React, { useState } from 'react';
import './FileUploadForm.css';
import axios from 'axios';

const FileUploadForm = () => {
    const [invoice, setInvoice] = useState(null);
    const [contract, setContract] = useState(null);
    const [invoiceFileName, setInvoiceFileName] = useState("");
    const [contractFileName, setContractFileName] = useState("");

    const handleFileChange = (e, setter, setFileName) => {
        setter(e.target.files[0]);
        setFileName(e.target.files[0]?.name);
    };

    const handleSubmit = async () => {
        const formData = new FormData();
        formData.append('invoice', invoice);
        formData.append('contract', contract);
        formData.append('invoiceExtension', invoice.name.split('.').pop());
        formData.append('contractExtension', contract.name.split('.').pop());

        try {
            const response = await axios.post('http://localhost:8080/api/upload', formData);
            alert(response.data);
            downloadPDFReport();
        } catch (error) {
            alert('An error occurred: ' + error);
        }
    };


    const downloadPDFReport = () => {
        window.open('http://localhost:8080/api/report', '_blank');
    }



    return (
        <div className="Paper">
            <h2 className="FormHeader">Generate Invoice Audit</h2>
            <div className="form-controls">
                <div className="form-control">
                    <label htmlFor="invoice" className="UploadButton">
                        Upload Invoice
                    </label>
                    <input type="file" id="invoice" onChange={(e) => handleFileChange(e, setInvoice, setInvoiceFileName)} />
                    {invoiceFileName && <span>{invoiceFileName}</span>}
                </div>
                <div className="form-control">
                    <label htmlFor="contract" className="UploadButton">
                        Upload Contract
                    </label>
                    <input type="file" id="contract" onChange={(e) => handleFileChange(e, setContract, setContractFileName)} />
                    {contractFileName && <span>{contractFileName}</span>}
                </div>
            </div>
            <button className="Button" onClick={handleSubmit}>
                Generate Audit
            </button>
        </div>
    );
};

export default FileUploadForm;
