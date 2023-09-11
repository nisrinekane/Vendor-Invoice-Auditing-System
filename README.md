## Automated Vendor Invoice Auditing System 

### Overview
The Invoice Auditing System is a Spring Boot-based application that leverages OpenNLP and Apache POI for auditing invoice documents against contracts. This application provides an easy-to-use interface to upload invoice and contract documents, runs natural language processing algorithms to detect various entities in the documents, and produces a PDF report of the auditing results.

### Features
* Upload invoice and contract documents in multiple formats (PDF, DOC, DOCX, TXT).

* Entity recognition for detecting dates, money amounts, percentages, times, locations, and persons.

* Generate a detailed PDF report containing the auditing results.

### Requirements
Java 11+

Maven

Spring Boot 2.5+

### Getting Started
Clone the Repository
```
git clone https://github.com/nisrinekane/Vendor-Invoice-Auditing-System.git
cd Vendor-Invoice-Auditing-System
```
Build the Application
```
mvn clean install
```
Run the Application

The application will start and be accessible at http://localhost:8080.

### Usage
Open your web browser and navigate to http://localhost:8080.
Upload the invoice and contract files.
Click the "Generate Report" button.
Download the generated PDF report.
