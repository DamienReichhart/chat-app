package com.ap4.client.services;

import javafx.fxml.FXMLLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;

/**
 * Utility class for FXML loading operations.
 * Provides centralized access to FXML resources and loading functionality.
 */
public class FXMLLoaderService {
    private static final Logger logger = LogManager.getLogger(FXMLLoaderService.class);
    private static final String FXML_BASE_PATH = "fxml/";
    private static final String CSS_BASE_PATH = "css/";
    
    /**
     * Loads an FXML file and returns the loader
     * 
     * @param fxmlFileName The name of the FXML file (without path)
     * @return FXMLLoader instance with the loaded FXML
     * @throws IOException If the FXML file cannot be found or loaded
     */
    public static FXMLLoader getLoader(String fxmlFileName) throws IOException {
        String fullPath = FXML_BASE_PATH + fxmlFileName;
        logger.debug("Loading FXML file: " + fullPath);
        
        URL resource = FXMLLoaderService.class.getClassLoader().getResource(fullPath);
        if (resource == null) {
            logger.error("Failed to find FXML resource: " + fullPath);
            throw new IOException("Resource not found: " + fullPath);
        }
        
        return new FXMLLoader(resource);
    }
    
    /**
     * Loads an FXML file and returns the root object
     * 
     * @param <T> The type of the root object
     * @param fxmlFileName The name of the FXML file (without path)
     * @return The root object loaded from the FXML
     * @throws IOException If the FXML file cannot be found or loaded
     */
    public static <T> T loadFXML(String fxmlFileName) throws IOException {
        FXMLLoader loader = getLoader(fxmlFileName);
        return loader.load();
    }
    
    /**
     * Loads an FXML file and returns both the root object and the loader
     * 
     * @param <T> The type of the root object
     * @param fxmlFileName The name of the FXML file (without path)
     * @return LoadResult containing both the root object and the loader
     * @throws IOException If the FXML file cannot be found or loaded
     */
    public static <T> LoadResult<T> loadFXMLWithLoader(String fxmlFileName) throws IOException {
        FXMLLoader loader = getLoader(fxmlFileName);
        T root = loader.load();
        return new LoadResult<>(root, loader);
    }
    
    /**
     * Class to hold the result of loading an FXML file
     * 
     * @param <T> The type of the root object
     */
    public static class LoadResult<T> {
        private final T root;
        private final FXMLLoader loader;
        
        public LoadResult(T root, FXMLLoader loader) {
            this.root = root;
            this.loader = loader;
        }
        
        public T getRoot() {
            return root;
        }
        
        public FXMLLoader getLoader() {
            return loader;
        }
        
        public <C> C getController() {
            return loader.getController();
        }
    }
}
