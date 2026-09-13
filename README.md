# TraceZero: File Metadata Privacy Protection Tool

[![Build & Release TraceZero](https://github.com/Suruchidoke/TraceZero-File-Metadata-Privacy-Protection-Tool/actions/workflows/build-and-release.yml/badge.svg)](https://github.com/Suruchidoke/TraceZero-File-Metadata-Privacy-Protection-Tool/actions/workflows/build-and-release.yml)
[![Latest Release](https://img.shields.io/github/v/release/Suruchidoke/TraceZero-File-Metadata-Privacy-Protection-Tool?label=Latest%20Release)](https://github.com/Suruchidoke/TraceZero-File-Metadata-Privacy-Protection-Tool/releases/latest)
[![Java 17](https://img.shields.io/badge/Java-17+-blue.svg)](https://adoptium.net/)

> 📦 **Quick Download**: Download the ready-to-run release package from [GitHub Releases](https://github.com/Suruchidoke/TraceZero-File-Metadata-Privacy-Protection-Tool/releases/latest) (`TraceZero-Windows.zip` or `TraceZero.jar`).

TraceZero is a modern, privacy-focused desktop application and CLI tool designed to protect your privacy by scanning, analyzing, and cleaning hidden metadata (such as EXIF, authors, creator details, location info, company names, and timestamps) from various file formats.

The application features a premium user interface with a custom dark/light theme (built on top of the AtlantaFX framework) as well as a background directory sentinel (Folder Watcher) to automatically scrub files in real time.

---

## Key Features

1. **Multi-Format Scanning & Cleaning**:
   - **Images (PNG, JPG, BMP, GIF)**: Cleans EXIF tags, GPS/location data, camera details, and software identifiers while preserving PNG alpha transparency.
   - **PDFs**: Deep sanitization stripping both Document Information dictionaries (Author, Creator, Title, Producer, Subject) and embedded Catalog XMP XML metadata streams.
   - **Microsoft Office (DOCX, XLSX, PPTX)**: Universal Open Packaging Conventions (OPC) sanitization stripping Core properties, Extended properties (`app.xml` Company/Manager), and Custom properties.
   - **ZIP Archives**: Normalizes internal DOS/POSIX file timestamps and removes internal metadata headers.
2. **Interactive GUI**: Built with JavaFX 21:
   - File drag-and-drop support.
   - Live metadata extraction and inspection.
   - Real-time cleaning status dashboard.
   - Exportable structured JSON audit reports.
3. **Folder Watcher (Sentinel)**: Monitors a specified local directory and automatically sanitizes any newly added files.
4. **CLI Mode**: Allows quick terminal execution and automation without opening the GUI.
5. **Automated Testing Suite**: Built-in JUnit 5 tests verifying sanitization across all supported formats.

---

## Tech Stack & Libraries

* **Core**: Java 17
* **Desktop UI**: JavaFX 21 (Controls, FXML)
* **Themes & Styles**: AtlantaFX (Modern CSS / Theme system)
* **Iconography**: Ikonli (FontAwesome 5 pack)
* **Animations**: AnimateFX (Micro-animations and transitions)
* **Metadata & Document Libraries**:
  * **Metadata Extractor** (Drew Noakes) - EXIF/IPTC image metadata extraction
  * **Apache PDFBox** - PDF information and Catalog XMP stream scrubbing
  * **Apache POI (OOXML)** - Universal Office document scrubbing (`.docx`, `.xlsx`, `.pptx`)
  * **Google Gson** - JSON report serialization and history persistence
* **Testing**: JUnit 5 & Maven Surefire Plugin

---

## Prerequisites

Before running the project, make sure you have:
* **Java JDK 17 or higher** installed.

---

## Step-by-Step Execution Guide

### Step 1: Verify / Install Java 17+

1. Open your terminal (Command Prompt or PowerShell) and check your current Java version:
   ```cmd
   java -version
   ```
2. **If Java is not installed or the version is lower than 17**:
   * **Using Winget (Recommended for Windows)**:
     ```cmd
     winget install EclipseAdoptium.Temurin.17.JDK
     ```
   * **Manual Install**: Download the Windows x64 MSI installer from [Adoptium Temurin 17](https://adoptium.net/temurin/releases/?version=17) and install it.
3. Restart your terminal window to apply the updates and verify again:
   ```cmd
   java -version
   ```

---

### Step 2: Run the Application

#### Option A: Download Pre-Built Release (Instant Run)
If you don't want to build from source:
1. Download `TraceZero-Windows.zip` from [GitHub Releases](https://github.com/Suruchidoke/TraceZero-File-Metadata-Privacy-Protection-Tool/releases/latest).
2. Extract the ZIP and double-click `run-tracezero.bat` (or run `java -jar TraceZero.jar`).

---

#### Option B: One-Click Windows Launcher from Source
Open a terminal in the project directory (`cd d:\project\TraceZero`) and run:
```powershell
.\run-tracezero.bat
```
*(This script automatically verifies Java 17+, builds the executable if needed, and launches the desktop GUI.)*

#### Option C: Launch GUI Mode via Maven
To launch the full interactive desktop application using the Maven wrapper:
```powershell
.\mvnw clean javafx:run
```
*(On first startup, this will download dependencies, compile the code, and launch the interface.)*

#### Option D: Launch CLI Mode (Direct File Processing)
To sanitize a file directly from your terminal without opening the GUI:
```powershell
.\mvnw compile exec:java -D"exec.mainClass"="com.example.hellofx.Main" -D"exec.args"="C:\path\to\your\file.ext"
```
*(Replace `C:\path\to\your\file.ext` with the path of the file you want to clean.)*

#### Option E: Build and Run Standalone JAR
You can package the application into a standalone runnable `.jar` file:

1. **Build the package:**
   ```powershell
   .\mvnw clean package
   ```
2. **Run GUI Mode:**
   ```powershell
   java -jar target/TraceZero-1.0-SNAPSHOT.jar
   ```
3. **Run CLI Mode:**
   ```powershell
   java -jar target/TraceZero-1.0-SNAPSHOT.jar "C:\path\to\your\file.ext"
   ```

---

### Step 3: Running Automated Tests

To run the full suite of unit tests verifying PDF deep sanitization, Office suite cleaning, image transparency, and history persistence:
```powershell
.\mvnw test
```

---

## Project Structure

Overview of the main components in `src/main/java/com/example/hellofx`:

* **`Main.java`**: Application entry point that routes between CLI and GUI modes based on arguments.
* **`HelloApplication.java`**: Handles JavaFX application lifecycle, stage setup, and AtlantaFX theme initialization.
* **`MainController.java`**: Orchestrates navigation and view switching across the GUI.
* **`cleaner/`**: Metadata sanitizers (`ImageCleaner`, `PdfCleaner`, `OfficeCleaner`, `ZipCleaner`).
* **`scanner/`**: Metadata inspection modules (`ImageScanner`, `PdfScanner`, `OfficeScanner`, `ZipScanner`, `FileTypeDetector`).
* **`watcher/`**: Directory sentinel service (`FolderWatcher.java`) for background real-time folder monitoring.
* **`persistence/`**: Data layer (`HistoryManager.java`) managing audit logs and session history.
* **`controllers/`**: UI controllers (Dashboard, Scanner, Cleaner, Reports, Settings).
* **`views/`**: FXML layout templates and UI styling.

