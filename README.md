# TraceZero: File Metadata Privacy Protection Tool

TraceZero is a modern, privacy-focused desktop application and CLI tool designed to protect your privacy by scanning, analyzing, and cleaning hidden metadata (such as EXIF, authors, creator details, location info, and creation timestamps) from various file formats.

The application features a premium user interface with a custom dark/light theme (built on top of the AtlantaFX framework) as well as a background directory sentinel (Folder Watcher) to automatically scrub files in real time.

---

## Key Features

1. **Multi-Format Scanning & Cleaning**:
   - **Images**: Cleans EXIF tags, GPS/location data, camera details, etc.
   - **PDFs**: Scrubs document information (Author, Creator, Title, Producer, Subject).
   - **DOCX**: Removes Office-specific metadata properties.
   - **ZIP**: Cleans internal file headers and archives.
2. **Interactive GUI**: Built using JavaFX and features:
   - File drag-and-drop.
   - Live metadata extraction and preview.
   - Dynamic UI animations and status dashboard.
3. **Folder Watcher (Sentinel)**: Monitors a specified local directory and automatically sanitizes any newly added files.
4. **CLI Mode**: Allows quick terminal execution and automation without opening the GUI.

---

## Tech Stack & Libraries

* **Core**: Java 17
* **Desktop UI**: JavaFX 21 (Controls, FXML)
* **Themes & Styles**: AtlantaFX (Modern CSS/Theme system)
* **Iconography**: Ikonli (FontAwesome 5 pack)
* **Animations**: AnimateFX (Micro-animations and transitions)
* **Metadata & Document Libraries**:
  * **Metadata Extractor** (by Drew Noakes) - EXIF/IPTC image metadata extraction
  * **Apache PDFBox** - PDF information scrubbing
  * **Apache POI (ooxml)** - Microsoft Word DOCX metadata scrubbing
  * **Google Gson** - JSON report serialization

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

Navigate to the project root directory (`C:\Users\Suruchi\OneDrive\Desktop\project\TraceZero`) in your terminal. You can run the application in the following ways:

#### Option A: Launch GUI Mode (Default Desktop Window)
To launch the full interactive desktop application, run the Maven wrapper command:
```powershell
.\mvnw clean javafx:run
```
*(On first startup, this will download all dependencies, compile the code, and launch the interface. This may take 1-2 minutes depending on your internet connection.)*

#### Option B: Launch CLI Mode (Command Line Interface)
To sanitize a file directly from your shell without launching the GUI:
```powershell
.\mvnw compile exec:java -D"exec.mainClass"="com.example.hellofx.Main" -D"exec.args"="C:\path\to\your\file.ext"
```
*(Replace `C:\path\to\your\file.ext` with the absolute path of the file you want to clean.)*

#### Option C: Build and Run as a Executable JAR
You can pack the application into a standalone runnable `.jar` file:

1. **Build the package:**
   ```powershell
   .\mvnw clean package
   ```
2. **Run GUI Mode:**
   ```powershell
   java -jar target/Hellofx-1.0-SNAPSHOT.jar
   ```
3. **Run CLI Mode (Process File):**
   ```powershell
   java -jar target/Hellofx-1.0-SNAPSHOT.jar "C:\path\to\your\file.ext"
   ```

---

## Project Structure

A brief overview of the main components located inside `src/main/java/com/example/hellofx`:

* **`Main.java`**: The app entry point that switches between CLI and GUI modes based on arguments.
* **`HelloApplication.java`**: Handles the JavaFX stage, window initialization, styling, and UI display.
* **`MainController.java`**: Orchestrates side-navigation and dynamic page-switching inside the GUI.
* **`cleaner/`**: Contains core cleaning algorithms for images, PDFs, Word documents, and ZIP archives.
* **`watcher/`**: Implements the directory watchdog service (`FolderWatcher.java`) to monitor local folders for automated cleaning.
* **`scanner/`**: Handles the metadata extraction and reading.
* **`controllers/`**: Controller code for the individual views (Dashboard, Scanner, Cleaner, Reports, Settings).
* **`views/`**: FXML layouts located in resources defining the interface design.
