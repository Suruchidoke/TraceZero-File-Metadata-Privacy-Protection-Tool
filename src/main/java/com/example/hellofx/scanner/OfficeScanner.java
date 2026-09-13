package com.example.hellofx.scanner;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ooxml.POIXMLProperties;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Universal metadata scanner for modern Microsoft Office OpenXML documents:
 * Word (.docx), Excel (.xlsx), and PowerPoint (.pptx).
 */
public class OfficeScanner {

    public static List<String> scanOfficeFile(File file) {
        List<String> exposedData = new ArrayList<>();
        if (file == null || !file.exists()) {
            exposedData.add("Error: File does not exist.");
            return exposedData;
        }

        String tagPrefix = getPrefix(file);

        try (OPCPackage pkg = OPCPackage.open(file, PackageAccess.READ)) {
            POIXMLProperties props = new POIXMLProperties(pkg);

            // 1. Core Properties
            POIXMLProperties.CoreProperties core = props.getCoreProperties();
            if (core != null) {
                if (core.getCreator() != null && !core.getCreator().isBlank()) {
                    exposedData.add("[" + tagPrefix + "] Original Author: " + core.getCreator());
                }
                String lastModifiedBy = core.getUnderlyingProperties().getLastModifiedByProperty().orElse(null);
                if (lastModifiedBy != null && !lastModifiedBy.isBlank()) {
                    exposedData.add("[" + tagPrefix + "] Last Modified By: " + lastModifiedBy);
                }
                if (core.getRevision() != null && !core.getRevision().isBlank()) {
                    exposedData.add("[" + tagPrefix + "] Revision Number: " + core.getRevision());
                }
                if (core.getTitle() != null && !core.getTitle().isBlank()) {
                    exposedData.add("[" + tagPrefix + "] Document Title: " + core.getTitle());
                }
                if (core.getSubject() != null && !core.getSubject().isBlank()) {
                    exposedData.add("[" + tagPrefix + "] Subject: " + core.getSubject());
                }
                if (core.getCategory() != null && !core.getCategory().isBlank()) {
                    exposedData.add("[" + tagPrefix + "] Category: " + core.getCategory());
                }
                if (core.getCreated() != null) {
                    exposedData.add("[" + tagPrefix + "] Date Created: " + core.getCreated());
                }
                if (core.getModified() != null) {
                    exposedData.add("[" + tagPrefix + "] Date Last Modified: " + core.getModified());
                }
            }

            // 2. Extended Properties (Corporate Leak Vectors)
            POIXMLProperties.ExtendedProperties ext = props.getExtendedProperties();
            if (ext != null && ext.getUnderlyingProperties() != null) {
                var underlying = ext.getUnderlyingProperties();
                if (underlying.getCompany() != null && !underlying.getCompany().isBlank()) {
                    exposedData.add("[" + tagPrefix + "] 🏢 Corporate Organization: " + underlying.getCompany());
                }
                if (underlying.getManager() != null && !underlying.getManager().isBlank()) {
                    exposedData.add("[" + tagPrefix + "] Manager: " + underlying.getManager());
                }
                if (underlying.getApplication() != null && !underlying.getApplication().isBlank()) {
                    exposedData.add("[" + tagPrefix + "] Authoring Software: " + underlying.getApplication());
                }
                if (underlying.getTemplate() != null && !underlying.getTemplate().isBlank()) {
                    exposedData.add("[" + tagPrefix + "] Template: " + underlying.getTemplate());
                }
                if (underlying.getTotalTime() > 0) {
                    exposedData.add("[" + tagPrefix + "] Total Editing Time: " + underlying.getTotalTime() + " mins");
                }
            }

            // 3. Custom Properties
            POIXMLProperties.CustomProperties custom = props.getCustomProperties();
            if (custom != null && custom.getUnderlyingProperties() != null) {
                var propsList = custom.getUnderlyingProperties().getPropertyList();
                if (propsList != null && !propsList.isEmpty()) {
                    for (var prop : propsList) {
                        exposedData.add("[" + tagPrefix + "] Custom Tag: " + prop.getName());
                    }
                }
            }

        } catch (Exception e) {
            exposedData.add("Error scanning Office file: " + e.getMessage());
        }

        return exposedData;
    }

    private static String getPrefix(File file) {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".xlsx") || name.endsWith(".xls")) return "XLSX";
        if (name.endsWith(".pptx") || name.endsWith(".ppt")) return "PPTX";
        return "DOCX";
    }
}
