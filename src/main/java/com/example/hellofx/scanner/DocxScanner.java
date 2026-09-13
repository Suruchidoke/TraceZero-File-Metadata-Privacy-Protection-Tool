package com.example.hellofx.scanner;

import java.io.File;
import java.util.List;

public class DocxScanner {
    public static List<String> scanDOCX(File docxFile) {
        return OfficeScanner.scanOfficeFile(docxFile);
    }
}
