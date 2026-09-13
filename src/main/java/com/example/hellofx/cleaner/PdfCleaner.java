package com.example.hellofx.cleaner;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class PdfCleaner {

    public static File cleanPDF(File originalFile) {
        if (originalFile == null || !originalFile.exists()) return null;

        try (PDDocument document = Loader.loadPDF(originalFile)) {
            // 1. Wipe standard Document Information fields
            PDDocumentInformation info = document.getDocumentInformation();
            if (info != null) {
                info.setAuthor(null);
                info.setCreator(null);
                info.setProducer(null);
                info.setTitle(null);
                info.setSubject(null);
                info.setKeywords(null);
                info.setCreationDate(null);
                info.setModificationDate(null);

                // Purge any custom dictionary keys
                Set<String> keys = info.getMetadataKeys();
                if (keys != null) {
                    for (String key : keys) {
                        info.setCustomMetadataValue(key, null);
                    }
                }
            }

            // 2. Deep Purge: Wipe Catalog XMP Metadata stream (XML packets containing software/author history)
            PDDocumentCatalog catalog = document.getDocumentCatalog();
            if (catalog != null) {
                catalog.setMetadata(null);
            }

            // 3. Save rewritten PDF to clean output path
            String cleanPath = getCleanPath(originalFile);
            File cleanCopy = new File(cleanPath);
            document.save(cleanCopy);

            System.out.println("[✓] PDF Deep-Cleaned: Info & XMP Catalog streams wiped.");
            return cleanCopy;
        } catch (IOException e) {
            System.err.println("PDF Clean Error: " + e.getMessage());
            return null;
        }
    }

    private static String getCleanPath(File file) {
        String path = file.getAbsolutePath();
        int dotIndex = path.lastIndexOf(".");
        return (dotIndex != -1)
                ? path.substring(0, dotIndex) + "_clean" + path.substring(dotIndex)
                : path + "_clean.pdf";
    }
}
