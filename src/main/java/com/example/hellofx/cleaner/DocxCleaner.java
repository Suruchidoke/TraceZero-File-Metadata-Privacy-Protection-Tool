package com.example.hellofx.cleaner;

import java.io.File;

public class DocxCleaner {
    public static File cleanDOCX(File originalFile) {
        return OfficeCleaner.cleanOfficeFile(originalFile);
    }
}
