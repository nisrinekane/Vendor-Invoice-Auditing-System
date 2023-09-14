import React, { useState } from 'react';
import './FileUploadForm.css';
import axios from 'axios';

const FileUploadForm = () => {
    const [invoice, setInvoice] = useState(null);
    const [contract, setContract] = useState(null);

    const handleFileChange = (e, setter) => {
        setter(e.target.files[0]);
    };

    const handleSubmit = async () => {
        const formData = new FormData();
        formData.append('invoice', invoice);
        formData.append('contract', contract);

        try {
            const response = await axios.post('http://localhost:8080/api/upload', formData);
            alert(response.data);
        } catch (error) {
            alert('An error occurred: ' + error);
        }
    };

    return (
        <div className="Paper">
            <h2 className="FormHeader">Generate Invoice Audit</h2>
            <div className="form-controls">
                <div className="form-control">
                    <label htmlFor="invoice" className="UploadButton">Upload Invoice</label>
                    <input type="file" id="invoice" onChange={(e) => handleFileChange(e, setInvoice)} />
                </div>
                <div className="form-control">
                    <label htmlFor="contract" className="UploadButton">Upload Contract</label>
                    <input type="file" id="contract" onChange={(e) => handleFileChange(e, setContract)} />
                </div>
            </div>
            <button className="Button" onClick={handleSubmit}>
                Generate Audit
            </button>
        </div>
    );
};

    export default FileUploadForm;
