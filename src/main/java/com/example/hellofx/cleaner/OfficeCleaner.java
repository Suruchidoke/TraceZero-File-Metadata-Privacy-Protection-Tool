package com.example.hellofx.cleaner;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ooxml.POIXMLProperties;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Universal metadata cleaner for modern Microsoft Office OpenXML documents:
 * Word (.docx), Excel (.xlsx), and PowerPoint (.pptx).
 *
 * Operates directly on the Open Packaging Conventions (OPC) container to purge:
 * 1. Dublin Core properties (Author, Last Modified By, Revision, Dates)
 * 2. Extended properties (Company, Manager, Application, Template)
 * 3. Custom user-defined corporate metadata
 */
public class OfficeCleaner {

    public static File cleanOfficeFile(File originalFile) {
        if (originalFile == null || !originalFile.exists()) return null;

        String cleanPath = getCleanPath(originalFile);
        File cleanCopy = new File(cleanPath);

        try {
            // 1. Create a replica of the original file
            Files.copy(originalFile.toPath(), cleanCopy.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // 2. Open the OPC package in Read/Write mode
            try (OPCPackage pkg = OPCPackage.open(cleanCopy, PackageAccess.READ_WRITE)) {
                POIXMLProperties props = new POIXMLProperties(pkg);

                // 3. Wipe Core (Dublin Core) properties
                POIXMLProperties.CoreProperties core = props.getCoreProperties();
                if (core != null) {
                    core.setCreator(null);
                    core.setTitle(null);
                    core.setDescription(null);
                    core.setSubjectProperty(null);
                    core.setCategory(null);
                    core.setRevision(null);
                    core.setModified((String) null);

                    var underCore = core.getUnderlyingProperties();
                    if (underCore != null) {
                        underCore.setCreatorProperty((String) null);
                        underCore.setLastModifiedByProperty((String) null);
                        underCore.setCreatedProperty(java.util.Optional.empty());
                        underCore.setModifiedProperty(java.util.Optional.empty());
                        underCore.setTitleProperty((String) null);
                        underCore.setSubjectProperty((String) null);
                        underCore.setDescriptionProperty((String) null);
                        underCore.setRevisionProperty((String) null);
                        underCore.setCategoryProperty((String) null);
                    }
                }

                // 4. Wipe Extended properties (app.xml - corporate leak vectors)
                POIXMLProperties.ExtendedProperties ext = props.getExtendedProperties();
                if (ext != null && ext.getUnderlyingProperties() != null) {
                    var underExt = ext.getUnderlyingProperties();
                    if (underExt.isSetCompany()) underExt.unsetCompany();
                    if (underExt.isSetManager()) underExt.unsetManager();
                    if (underExt.isSetTemplate()) underExt.unsetTemplate();
                    if (underExt.isSetApplication()) underExt.unsetApplication();
                    if (underExt.isSetTotalTime()) underExt.unsetTotalTime();
                }

                // 5. Wipe Custom properties (custom.xml)
                POIXMLProperties.CustomProperties custom = props.getCustomProperties();
                if (custom != null && custom.getUnderlyingProperties() != null) {
                    for (int i = custom.getUnderlyingProperties().sizeOfPropertyArray() - 1; i >= 0; i--) {
                        custom.getUnderlyingProperties().removeProperty(i);
                    }
                }

                // 6. Commit changes back to the package parts and flush
                props.commit();
                pkg.flush();
            }

            System.out.println("[✓] Office File Sanitized: " + cleanCopy.getName());
            return cleanCopy;

        } catch (Exception e) {
            System.err.println("Office Clean Error: " + e.getMessage());
            if (cleanCopy.exists()) {
                cleanCopy.delete();
            }
            return null;
        }
    }

    private static String getCleanPath(File file) {
        String path = file.getAbsolutePath();
        int dotIndex = path.lastIndexOf(".");
        return (dotIndex != -1)
                ? path.substring(0, dotIndex) + "_clean" + path.substring(dotIndex)
                : path + "_clean.docx";
    }
}
