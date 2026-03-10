package com.example.hellofx.watcher;

import com.example.hellofx.cleaner.DocxCleaner;
import com.example.hellofx.cleaner.ImageCleaner;
import com.example.hellofx.cleaner.PdfCleaner;
import com.example.hellofx.core.LogManager;
import com.example.hellofx.core.StatisticsManager;
import com.example.hellofx.utils.FileTypeDetector;
import com.example.hellofx.utils.SettingsManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FolderWatcher {

    private static final FolderWatcher instance = new FolderWatcher();
    public static FolderWatcher getInstance() { return instance; }

    private WatchService watchService;
    private final Map<WatchKey, Path> watchKeys = new HashMap<>();
    private ScheduledExecutorService executorService;
    private volatile boolean isRunning = false;

    private final Map<String, Long> recentlyProcessed = new HashMap<>();

    private FolderWatcher() {}

    public boolean isRunning() { return isRunning; }

    public void startWatching() {
        if (isRunning || !SettingsManager.folderMonitor) return;

        try {
            watchService = FileSystems.getDefault().newWatchService();
            isRunning = true;

            // 🚀 Now reads folders from the persistent SettingsManager list
            for (String pathStr : SettingsManager.watchedFolders) {
                registerFolderInternal(Paths.get(pathStr));
            }

            executorService = Executors.newScheduledThreadPool(2);
            executorService.submit(this::processLiveEvents);
            executorService.scheduleAtFixedRate(this::cronModifiedSweep, 1, 1, TimeUnit.MINUTES);

            System.out.println("[Watcher] Background Sentinel Active.");
        } catch (IOException e) {
            System.err.println("[Watcher] Failed to start WatchService: " + e.getMessage());
        }
    }

    public void stopWatching() {
        isRunning = false;
        try {
            if (watchService != null) watchService.close();
            if (executorService != null) executorService.shutdownNow();
            watchKeys.clear();
            System.out.println("[Watcher] Background Sentinel Offline.");
        } catch (IOException e) {
            System.err.println("[Watcher] Error stopping WatchService: " + e.getMessage());
        }
    }

    public void addWatchedFolder(String path) {
        if (!SettingsManager.watchedFolders.contains(path)) {
            SettingsManager.watchedFolders.add(path);
            SettingsManager.save(); // 🔥 Persist the new folder immediately
            if (isRunning) registerFolderInternal(Paths.get(path));
        }
    }

    public void removeWatchedFolder(String path) {
        SettingsManager.watchedFolders.remove(path);
        SettingsManager.save(); // 🔥 Persist the removal immediately

        WatchKey targetKey = null;
        for (Map.Entry<WatchKey, Path> entry : watchKeys.entrySet()) {
            if (entry.getValue().toString().equals(path)) {
                targetKey = entry.getKey();
                break;
            }
        }
        if (targetKey != null) {
            targetKey.cancel();
            watchKeys.remove(targetKey);
        }
    }

    private void registerFolderInternal(Path folder) {
        try {
            if (!Files.exists(folder) || !Files.isDirectory(folder)) return;
            WatchKey key = folder.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
            watchKeys.put(key, folder);
            System.out.println("[Watcher] Watching: " + folder);
        } catch (IOException e) {
            System.err.println("[Watcher] Failed to register: " + e.getMessage());
        }
    }

    private void processLiveEvents() {
        while (isRunning) {
            WatchKey key;
            try { key = watchService.take(); }
            catch (InterruptedException | ClosedWatchServiceException x) { return; }

            Path dir = watchKeys.get(key);
            if (dir == null) continue;

            for (WatchEvent<?> event : key.pollEvents()) {
                Path child = dir.resolve(((WatchEvent<Path>) event).context());
                if (Files.isRegularFile(child)) {
                    File newFile = child.toFile();
                    if (!newFile.getName().toLowerCase().contains("_clean")) {
                        handleFileSafely(newFile);
                    }
                }
            }
            if (!key.reset()) watchKeys.remove(key);
        }
    }

    private void cronModifiedSweep() {
        if (!SettingsManager.folderMonitor) return;
        Instant oneMinuteAgo = Instant.now().minus(1, ChronoUnit.MINUTES);

        for (Path dir : watchKeys.values()) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                for (Path file : stream) {
                    if (Files.isRegularFile(file) && !file.getFileName().toString().toLowerCase().contains("_clean")) {
                        BasicFileAttributes attr = Files.readAttributes(file, BasicFileAttributes.class);
                        if (attr.lastModifiedTime().toInstant().isAfter(oneMinuteAgo)) {
                            File f = file.toFile();
                            Long lastProcessed = recentlyProcessed.getOrDefault(f.getAbsolutePath(), 0L);
                            if (System.currentTimeMillis() - lastProcessed > 10000) {
                                handleFileSafely(f);
                            }
                        }
                    }
                }
            } catch (IOException ignored) {}
        }
    }

    private void handleFileSafely(File file) {
        String path = file.getAbsolutePath();
        long now = System.currentTimeMillis();

        // 🚀 THE LOOP KILLER: If we processed this EXACT file in the last 3 seconds, ignore it.
        if (recentlyProcessed.containsKey(path)) {
            long lastTime = recentlyProcessed.get(path);
            if (now - lastTime < 3000) {
                return;
            }
        }

        // Update the cache immediately so other events for this file are blocked
        recentlyProcessed.put(path, now);

        try { Thread.sleep(800); } catch (InterruptedException ignored) {}
        if (!file.canWrite()) return;

        System.out.println("[Watcher] Intercepted target: " + file.getName());
        StatisticsManager.getInstance().incrementFilesDetected();

        FileTypeDetector.FileType type = FileTypeDetector.detect(file);

        new Thread(() -> {
            long startTime = System.currentTimeMillis();
            try {
                File cleanedFile = null;
                if (SettingsManager.autoClean) {
                    switch (type) {
                        case IMAGE -> cleanedFile = ImageCleaner.cleanImage(file);
                        case PDF   -> cleanedFile = PdfCleaner.cleanPDF(file);
                        case DOCX  -> cleanedFile = DocxCleaner.cleanDOCX(file);
                    }
                }

                if (cleanedFile != null && cleanedFile.exists()) {
                    // If overwriting, the Watcher will trigger again,
                    // but our 3-second cache check above will block the loop!
                    if (!SettingsManager.backupOriginals) {
                        String originalPath = file.getAbsolutePath();
                        if (file.delete()) {
                            cleanedFile.renameTo(new File(originalPath));
                        }
                    }

                    long duration = System.currentTimeMillis() - startTime;
                    StatisticsManager.getInstance().recordProcessingTime(duration);
                    StatisticsManager.getInstance().incrementFilesCleaned();
                    LogManager.getInstance().log("[✓] Auto Cleaned: " + file.getName());
                }
            } catch (Exception e) {
                LogManager.getInstance().log("[X] Failed: " + file.getName());
            }
        }).start();
    }
}