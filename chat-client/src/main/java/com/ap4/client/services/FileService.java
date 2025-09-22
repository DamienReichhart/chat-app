package com.ap4.client.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.ap4.common.models.Message;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class for file-related operations.
 * Provides methods for checking file types, getting file extensions,
 * and reading file data.
 */
public class FileService {
    // Logger for this class
    private static final Logger logger = LogManager.getLogger(FileService.class);
    
    // File type constants
    private static final Set<String> IMAGE_EXTENSIONS = new HashSet<>(
            Arrays.asList("jpg", "jpeg", "png", "gif", "bmp"));
    
    private static final Set<String> VIDEO_EXTENSIONS = new HashSet<>(
            Arrays.asList("mp4", "avi", "mov", "wmv", "mkv", "flv"));
    
    /**
     * Gets the file extension from a filename.
     * 
     * @param fileName The filename to extract extension from
     * @return The file extension (without the dot) or an empty string if no extension found
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null) return "";
        
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }
    
    /**
     * Checks if a file is an image based on its extension.
     * 
     * @param extension The file extension (without the dot)
     * @return true if the file is a supported image type, false otherwise
     */
    public static boolean isImageFile(String extension) {
        if (extension == null) return false;
        return IMAGE_EXTENSIONS.contains(extension.toLowerCase());
    }
    
    /**
     * Checks if a file is a video based on its extension.
     * 
     * @param extension The file extension (without the dot)
     * @return true if the file is a supported video type, false otherwise
     */
    public static boolean isVideoFile(String extension) {
        if (extension == null) return false;
        return VIDEO_EXTENSIONS.contains(extension.toLowerCase());
    }
    
    /**
     * Reads a file into a byte array.
     * 
     * @param file The file to read
     * @return The file contents as a byte array
     * @throws IOException If an I/O error occurs
     */
    public static byte[] readFileBytes(File file) throws IOException {
        if (file == null || !file.exists()) {
            throw new IOException("File does not exist or is null");
        }
        return Files.readAllBytes(file.toPath());
    }
    
    /**
     * Creates directories for file uploads if they don't exist.
     * 
     * @param directoryPath Path to the directory to create
     * @return The created directory path
     * @throws IOException If directory creation fails
     */
    public static Path ensureDirectoryExists(String directoryPath) throws IOException {
        Path path = Paths.get(directoryPath);
        if (!Files.exists(path)) {
            return Files.createDirectories(path);
        }
        return path;
    }
    
    /**
     * Downloads file data from a message to a user-selected location.
     * Shows a file chooser dialog for the user to select the save location.
     *
     * @param message The message containing the file data to download
     * @param stage The parent stage for the file chooser dialog
     * @param onSuccess Callback for successful download
     * @param onError Callback for error during download
     * @param onProgress Callback for download progress updates (if needed)
     */
    public static void downloadFile(Message message, Stage stage, 
                                    Runnable onSuccess, 
                                    Consumer<Exception> onError,
                                    Runnable onProgress) {
        // Create a file chooser dialog
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File");
        fileChooser.setInitialFileName(message.getFileName() != null ? message.getFileName() : "download");
        
        // Show the dialog and get the selected save location
        File saveFile = fileChooser.showSaveDialog(stage);
        
        // Process the file save if a location was selected
        if (saveFile != null) {
            // Use a background thread to avoid UI freeze during file loading
            CompletableFuture.runAsync(() -> {
                try {
                    // Get file data
                    byte[] fileData = message.getFileData();
                    
                    // If file data wasn't loaded in the message, show an error
                    if (fileData == null) {
                        if (onError != null) {
                            javafx.application.Platform.runLater(() -> 
                                onError.accept(new IOException("File data not available"))
                            );
                        }
                        return;
                    }
                    
                    // Call the progress callback if provided
                    if (onProgress != null) {
                        javafx.application.Platform.runLater(onProgress);
                    }
                    
                    // Write the file data to disk
                    try (FileOutputStream outputStream = new FileOutputStream(saveFile)) {
                        outputStream.write(fileData);
                    }
                    
                    // Update UI on the JavaFX thread with success
                    if (onSuccess != null) {
                        javafx.application.Platform.runLater(onSuccess);
                    }
                } catch (Exception e) {
                    logger.error("Failed to download file", e);
                    // Handle errors and update UI on the JavaFX thread
                    if (onError != null) {
                        javafx.application.Platform.runLater(() -> onError.accept(e));
                    }
                }
            });
        }
    }
    
    /**
     * Creates a temporary file from message data for immediate viewing.
     * This method is used primarily for opening images and other files
     * directly without requiring the user to explicitly save them first.
     * The temporary file is marked for deletion when the JVM exits.
     * 
     * @param message Message containing the file data to create a temp file from
     * @return The created temporary file
     * @throws IOException If an error occurs while creating or writing to the file
     */
    public static File createTempFile(Message message) throws IOException {
        // Determine filename - use original name or provide a generic alternative
        String fileName = message.getFileName() != null ? message.getFileName() : "tempfile";
        File tempFile = File.createTempFile("chat_", "_" + fileName);
        tempFile.deleteOnExit(); // File will be automatically deleted when JVM exits
        
        // Get file data - should be directly in the Message object
        byte[] fileData = message.getFileData();
        
        // For images, the data should already be loaded
        // For other file types, we show an error if not available
        if (fileData == null && message.getId() > 0) {
            throw new IOException("No file data available for this message");
        }
        
        // Write the file data to the temp file
        try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            outputStream.write(fileData);
        }
        
        return tempFile;
    }
    
    /**
     * Opens a file with the system's default application.
     * 
     * @param file The file to open
     * @throws IOException If the file can't be opened
     */
    public static void openFileWithDefaultProgram(File file) throws IOException {
        if (file != null && file.exists()) {
            java.awt.Desktop.getDesktop().open(file);
        } else {
            throw new IOException("File does not exist");
        }
    }
}
