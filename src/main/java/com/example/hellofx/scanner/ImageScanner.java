package com.example.hellofx.scanner;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.example.hellofx.core.StatisticsManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageScanner {

    public static List<String> scanImage(File imageFile) {
        List<String> dataList = new ArrayList<>();
        boolean gpsFound = false;

        try {
            Metadata metadata = ImageMetadataReader.readMetadata(imageFile);

            for (Directory directory : metadata.getDirectories()) {
                String dirName = directory.getName();

                // Ignore generic format structural markers
                if (dirName.contains("JPEG") || dirName.contains("JFIF") ||
                        dirName.contains("Huffman") || dirName.equalsIgnoreCase("File Type") ||
                        dirName.equalsIgnoreCase("File")) {
                    continue;
                }

                boolean isGps = dirName.toLowerCase().contains("gps");
                if (isGps && !gpsFound) {
                    gpsFound = true;
                    StatisticsManager.getInstance().incrementGpsDetected();
                }

                for (Tag tag : directory.getTags()) {
                    String prefix = isGps ? "[GPS LOCATION ⚠️]" : "[" + dirName + "]";
                    dataList.add(prefix + " " + tag.getTagName() + ": " + tag.getDescription());
                }
            }
        } catch (Exception e) {
            dataList.add("Error reading image metadata: " + e.getMessage());
        }
        return dataList;
    }
}