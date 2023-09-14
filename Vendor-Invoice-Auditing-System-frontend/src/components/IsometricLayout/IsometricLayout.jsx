import React from 'react';
import './IsometricLayout.css';
import FileUploadForm from '../FileUploadFormComponent/FileUploadForm';

const IsometricLayout = () => {
    return (
        <>
        <div className="isometric-layout">
                <div className="isometric-illustration">
                    {/* Background image for this div is set via CSS */}
                </div>
                <div className="isometric-content">
                    <FileUploadForm />
                </div>
            </div>
        </>
    );
};

export default IsometricLayout;
