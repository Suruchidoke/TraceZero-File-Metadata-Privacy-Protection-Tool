package com.example.hellofx.utils;

import java.io.File;

public class FileTypeDetector {

    // Categories understood by the TraceZero engine
    public enum FileType {
        IMAGE,
        PDF,
        DOCX,
        XLSX,
        PPTX,
        ZIP,
        UNSUPPORTED
    }

    /**
     * Inspects a file name and returns the corresponding FileType category.
     */
    public static FileType detect(File file) {
        if (file == null || !file.exists()) {
            return FileType.UNSUPPORTED;
        }

        String fileName = file.getName().toLowerCase();

        // 1. Images
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png") ||
                fileName.endsWith(".bmp") || fileName.endsWith(".gif")) {
            return FileType.IMAGE;
        }
        // 2. PDFs
        else if (fileName.endsWith(".pdf")) {
            return FileType.PDF;
        }
        // 3. Microsoft Word
        else if (fileName.endsWith(".docx") || fileName.endsWith(".doc")) {
            return FileType.DOCX;
        }
        // 4. Microsoft Excel
        else if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
            return FileType.XLSX;
        }
        // 5. Microsoft PowerPoint
        else if (fileName.endsWith(".pptx") || fileName.endsWith(".ppt")) {
            return FileType.PPTX;
        }
        // 6. ZIP Archives
        else if (fileName.endsWith(".zip")) {
            return FileType.ZIP;
        }

        return FileType.UNSUPPORTED;
    }
}