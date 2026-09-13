package com.example.hellofx.cleaner;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageCleaner {

    public static File cleanImage(File original) {
        if (original == null || !original.exists()) return null;

        try {
            // 1. Read the image into RAM (Drops all EXIF, GPS, IPTC, and XMP metadata headers)
            BufferedImage bufferedImage = ImageIO.read(original);
            if (bufferedImage == null) return null;

            // 2. Identify the original extension to preserve format and transparency
            String fileName = original.getName();
            int dotIndex = fileName.lastIndexOf(".");
            String baseName = (dotIndex != -1) ? fileName.substring(0, dotIndex) : fileName;
            String ext = (dotIndex != -1) ? fileName.substring(dotIndex + 1).toLowerCase() : "jpg";

            String format;
            String outExt;
            if (ext.equals("png")) {
                format = "png";
                outExt = "png";
            } else if (ext.equals("bmp")) {
                format = "bmp";
                outExt = "bmp";
            } else if (ext.equals("gif")) {
                format = "gif";
                outExt = "gif";
            } else {
                format = "jpg";
                outExt = "jpg";
            }

            // 3. Handle alpha transparency safely
            // JPEG cannot encode an alpha channel; if saving to JPEG, flatten onto a white canvas
            if (format.equals("jpg") && bufferedImage.getColorModel().hasAlpha()) {
                BufferedImage rgbImage = new BufferedImage(
                        bufferedImage.getWidth(),
                        bufferedImage.getHeight(),
                        BufferedImage.TYPE_INT_RGB
                );
                Graphics2D g2d = rgbImage.createGraphics();
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, rgbImage.getWidth(), rgbImage.getHeight());
                g2d.drawImage(bufferedImage, 0, 0, null);
                g2d.dispose();
                bufferedImage = rgbImage;
            }

            // 4. Prepare the clean output file with matching extension
            String newName = baseName + "_clean." + outExt;
            File cleanedFile = new File(original.getParent(), newName);

            // 5. Write only pure pixel matrix
            boolean success = ImageIO.write(bufferedImage, format, cleanedFile);

            if (success && cleanedFile.exists()) {
                System.out.println("[✓] Hard-Cleaned (" + format.toUpperCase() + "): " + cleanedFile.getName());
                return cleanedFile;
            }
        } catch (IOException e) {
            System.err.println("Error cleaning image: " + e.getMessage());
        }
        return null;
    }
}