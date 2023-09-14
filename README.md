# Automated Vendor Invoice Auditing System
## Overview
The Invoice Auditing System is a full-stack application with a Spring Boot-based backend and a React frontend. The backend leverages OpenNLP and Apache POI for auditing invoice documents against contracts, while the frontend provides an intuitive user interface for interacting with the system.

## Features

### Backend
* Upload invoice and contract documents in multiple formats (PDF, DOC, DOCX, TXT).
* Entity recognition for detecting dates, money amounts, percentages, times, locations, and persons.
* Generate a detailed PDF report containing the auditing results.
### Frontend
* Easy-to-use, responsive interface for uploading and managing documents.
* Auto download PDF reports.
  
## Requirements
### Backend
* Java 11+
* Maven
* Spring Boot 2.5+
### Frontend
* Node.js 12+
* npm

## Getting Started
### Backend
Clone the Repository
```
git clone https://github.com/nisrinekane/Vendor-Invoice-Auditing-System.git
cd Vendor-Invoice-Auditing-System/Vendor-Invoice-Auditing-System-backend
```

Build the Application
```
mvn clean install
```
Run the Application
```
mvn spring-boot:run
```
The backend application will start and be accessible at http://localhost:8080.

### Frontend
Navigate to Frontend Directory
```
cd Vendor-Invoice-Auditing-System/Vendor-Invoice-Auditing-System-frontend
```
Install Dependencies
```
npm install
```
Run the Application
```
npm start
```
The frontend application will start and be accessible at http://localhost:3000.

## Usage
Backend: Open your API tool (like Postman) or browser and interact with http://localhost:8080 for API calls.
Frontend: Open your web browser and navigate to http://localhost:3000. Upload the invoice and contract files, and click the "Generate Report" button to download the generated PDF report straight from the browser.
