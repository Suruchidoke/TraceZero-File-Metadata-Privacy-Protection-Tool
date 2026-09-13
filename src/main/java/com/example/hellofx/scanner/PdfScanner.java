package com.example.hellofx.scanner;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PdfScanner {

    public static List<String> scanPDF(File pdfFile) {
        List<String> exposedData = new ArrayList<>();
        if (pdfFile == null || !pdfFile.exists()) {
            exposedData.add("Error: PDF file does not exist.");
            return exposedData;
        }

        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDDocumentInformation info = document.getDocumentInformation();
            if (info != null) {
                if (info.getAuthor() != null) exposedData.add("[PDF] Author: " + info.getAuthor());
                if (info.getCreator() != null) exposedData.add("[PDF] Creator Software: " + info.getCreator());
                if (info.getProducer() != null) exposedData.add("[PDF] Producer: " + info.getProducer());
                if (info.getTitle() != null) exposedData.add("[PDF] Title: " + info.getTitle());
                if (info.getSubject() != null) exposedData.add("[PDF] Subject: " + info.getSubject());
                if (info.getKeywords() != null) exposedData.add("[PDF] Keywords: " + info.getKeywords());

                Calendar creationDate = info.getCreationDate();
                if (creationDate != null) exposedData.add("[PDF] Creation Date: " + creationDate.getTime());

                Calendar modDate = info.getModificationDate();
                if (modDate != null) exposedData.add("[PDF] Modification Date: " + modDate.getTime());
            }

            // Inspect Document Catalog for XML-based XMP Metadata stream
            PDDocumentCatalog catalog = document.getDocumentCatalog();
            if (catalog != null && catalog.getMetadata() != null) {
                exposedData.add("[PDF] ⚠️ Embedded XMP Catalog Stream (XML metadata packet present)");
            }
        } catch (Exception e) {
            exposedData.add("Error scanning PDF: " + e.getMessage());
        }
        return exposedData;
    }
}
