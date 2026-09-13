package com.example.hellofx.cleaner;

import com.example.hellofx.controllers.ReportsController.ReportSummary;
import com.example.hellofx.scanner.OfficeScanner;
import com.example.hellofx.scanner.PdfScanner;
import com.example.hellofx.utils.HistoryManager;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ooxml.POIXMLProperties;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

public class CleanerSanitizationTest {

    @Test
    public void testImageCleanerPreservesPngFormatAndTransparency(@TempDir Path tempDir) throws IOException {
        File originalPng = tempDir.resolve("sample_transparent.png").toFile();

        // Create a 50x50 image with an alpha channel (transparent background)
        BufferedImage img = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(new Color(255, 0, 0, 128)); // Semi-transparent red
        g2d.fillRect(10, 10, 30, 30);
        g2d.dispose();

        ImageIO.write(img, "png", originalPng);
        assertTrue(originalPng.exists());

        File cleaned = ImageCleaner.cleanImage(originalPng);
        assertNotNull(cleaned, "Cleaned image file should not be null");
        assertTrue(cleaned.exists(), "Cleaned image should exist on disk");
        assertTrue(cleaned.getName().endsWith("_clean.png"), "Should preserve .png extension");

        BufferedImage cleanedImg = ImageIO.read(cleaned);
        assertNotNull(cleanedImg);
        assertEquals(50, cleanedImg.getWidth());
        assertEquals(50, cleanedImg.getHeight());
        assertTrue(cleanedImg.getColorModel().hasAlpha(), "PNG should retain alpha channel");
    }

    @Test
    public void testZipCleanerStripsTrackersAndNormalizesTimestamps(@TempDir Path tempDir) throws IOException {
        File zipFile = tempDir.resolve("archive.zip").toFile();

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            // Normal file
            ZipEntry doc = new ZipEntry("notes.txt");
            zos.putNextEntry(doc);
            zos.write("Sensitive project notes".getBytes());
            zos.closeEntry();

            // OS Tracker file (macOS)
            ZipEntry macTracker = new ZipEntry(".DS_Store");
            zos.putNextEntry(macTracker);
            zos.write(new byte[]{0x00, 0x01, 0x02});
            zos.closeEntry();
        }

        File cleanedZip = ZipCleaner.cleanZIP(zipFile);
        assertNotNull(cleanedZip);
        assertTrue(cleanedZip.exists());
        assertTrue(cleanedZip.getName().endsWith("_clean.zip"));

        // Verify content of cleaned ZIP
        List<String> entryNames = new ArrayList<>();
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(cleanedZip))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                entryNames.add(entry.getName());
                // Verify timestamp was normalized to epoch (0 millis)
                assertEquals(0, entry.getTime(), "Timestamp should be zeroed to epoch");
                zis.closeEntry();
            }
        }

        assertTrue(entryNames.contains("notes.txt"), "Standard entry must be preserved");
        assertFalse(entryNames.contains(".DS_Store"), "Tracker .DS_Store must be purged");
    }

    @Test
    public void testPdfDeepSanitization(@TempDir Path tempDir) throws IOException {
        File pdfFile = tempDir.resolve("sample.pdf").toFile();

        // 1. Create a PDF with Author, Title, Subject, and an XMP metadata stream
        try (PDDocument doc = new PDDocument()) {
            doc.addPage(new org.apache.pdfbox.pdmodel.PDPage());

            PDDocumentInformation info = doc.getDocumentInformation();
            info.setAuthor("Secret Agent");
            info.setTitle("Classified Brief");
            info.setSubject("Forensics");

            // Attach XMP stream to catalog
            PDMetadata metadata = new PDMetadata(doc);
            metadata.importXMPMetadata("<x:xmpmeta>Author=Secret Agent</x:xmpmeta>".getBytes());
            doc.getDocumentCatalog().setMetadata(metadata);

            doc.save(pdfFile);
        }

        // 2. Scan before cleaning to verify detection
        List<String> scanResults = PdfScanner.scanPDF(pdfFile);
        assertTrue(scanResults.stream().anyMatch(s -> s.contains("Secret Agent")));
        assertTrue(scanResults.stream().anyMatch(s -> s.contains("XMP Catalog Stream")));

        // 3. Clean PDF
        File cleanedPdf = PdfCleaner.cleanPDF(pdfFile);
        assertNotNull(cleanedPdf);
        assertTrue(cleanedPdf.exists());

        // 4. Verify that info dictionary and XMP stream are wiped
        try (PDDocument cleanedDoc = Loader.loadPDF(cleanedPdf)) {
            PDDocumentInformation cleanInfo = cleanedDoc.getDocumentInformation();
            assertNull(cleanInfo.getAuthor(), "Author must be purged");
            assertNull(cleanInfo.getTitle(), "Title must be purged");
            assertNull(cleanInfo.getSubject(), "Subject must be purged");
            assertNull(cleanedDoc.getDocumentCatalog().getMetadata(), "Catalog XMP stream must be completely purged");
        }
    }

    @Test
    public void testOfficeCleanerSanitizesXlsxWorkbook(@TempDir Path tempDir) throws Exception {
        File xlsxFile = tempDir.resolve("financials.xlsx").toFile();

        // 1. Create an Excel workbook with metadata
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            workbook.createSheet("Quarterly Summary");
            POIXMLProperties props = workbook.getProperties();
            props.getCoreProperties().setCreator("Chief Financial Officer");
            props.getExtendedProperties().getUnderlyingProperties().setCompany("Acme Defense Corp");
            props.getCustomProperties().addProperty("Classification", "Confidential");

            try (FileOutputStream fos = new FileOutputStream(xlsxFile)) {
                workbook.write(fos);
            }
        }

        // 2. Scan before cleaning
        List<String> scanned = OfficeScanner.scanOfficeFile(xlsxFile);
        assertTrue(scanned.stream().anyMatch(s -> s.contains("Chief Financial Officer")));
        assertTrue(scanned.stream().anyMatch(s -> s.contains("Acme Defense Corp")));

        // 3. Clean Office file
        File cleanedXlsx = OfficeCleaner.cleanOfficeFile(xlsxFile);
        assertNotNull(cleanedXlsx);
        assertTrue(cleanedXlsx.exists());
        assertTrue(cleanedXlsx.getName().endsWith("_clean.xlsx"));

        // 4. Verify cleaned file has no author, company, or custom tags
        try (OPCPackage pkg = OPCPackage.open(cleanedXlsx, PackageAccess.READ)) {
            POIXMLProperties cleanProps = new POIXMLProperties(pkg);
            assertNull(cleanProps.getCoreProperties().getCreator(), "Excel creator must be purged");
            assertNull(cleanProps.getExtendedProperties().getUnderlyingProperties().getCompany(), "Company must be purged");
            assertEquals(0, cleanProps.getCustomProperties().getUnderlyingProperties().sizeOfPropertyArray(), "Custom tags must be purged");
        }
    }

    @Test
    public void testOfficeCleanerSanitizesPptxPresentation(@TempDir Path tempDir) throws Exception {
        File pptxFile = tempDir.resolve("briefing.pptx").toFile();

        // 1. Create PowerPoint presentation with metadata
        try (XMLSlideShow pptx = new XMLSlideShow()) {
            pptx.createSlide();
            POIXMLProperties props = pptx.getProperties();
            props.getCoreProperties().setCreator("Director of Operations");
            props.getCoreProperties().setTitle("Security Briefing");

            try (FileOutputStream fos = new FileOutputStream(pptxFile)) {
                pptx.write(fos);
            }
        }

        // 2. Clean presentation
        File cleanedPptx = OfficeCleaner.cleanOfficeFile(pptxFile);
        assertNotNull(cleanedPptx);
        assertTrue(cleanedPptx.exists());
        assertTrue(cleanedPptx.getName().endsWith("_clean.pptx"));

        // 3. Verify cleaned presentation
        try (OPCPackage pkg = OPCPackage.open(cleanedPptx, PackageAccess.READ)) {
            POIXMLProperties cleanProps = new POIXMLProperties(pkg);
            assertNull(cleanProps.getCoreProperties().getCreator(), "PowerPoint creator must be purged");
            assertNull(cleanProps.getCoreProperties().getTitle(), "PowerPoint title must be purged");
        }
    }

    @Test
    public void testHistoryManagerSaveAndLoad() {
        ReportSummary sampleReport = new ReportSummary(
                "test_file.png",
                "IMAGE FILE",
                "12 KB",
                "2026-09-13 21:00:00",
                "HASH_12345",
                85,
                "High Risk",
                5,
                3,
                5,
                0,
                List.of("EXIF: GPS Latitude", "EXIF: Camera Model"),
                null
        );

        List<ReportSummary> testList = List.of(sampleReport);
        HistoryManager.saveHistory(testList);

        List<ReportSummary> loaded = HistoryManager.loadHistory();
        assertNotNull(loaded);
        assertFalse(loaded.isEmpty());
        assertEquals("test_file.png", loaded.get(0).fileName());
    }
}
