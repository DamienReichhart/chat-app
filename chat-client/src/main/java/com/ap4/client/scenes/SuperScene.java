package com.ap4.client.scenes;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ap4.client.controllers.Controller;
import com.ap4.client.services.FXMLLoaderService;
import com.ap4.client.services.FXMLLoaderService.LoadResult;
import com.ap4.client.services.SessionService;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * Abstract base class for all scenes in the application.
 * Provides common functionality for initializing, managing, and resetting scenes.
 * 
 * This class follows the Template Method pattern to define the skeleton
 * of scene initialization process while allowing subclasses to override
 * specific steps without changing the overall structure.
 * 
 * All scenes in the application should extend this class to ensure
 * consistent behavior and access to shared resources.
 */
public abstract class SuperScene {
    /**
     * Logger instance for all scenes.
     */
    private static Logger logger = LogManager.getLogger(SuperScene.class);
    
    /**
     * Default scene width in pixels.
     */
    private int SCENE_WIDTH = 500;
    
    /**
     * Default scene height in pixels.
     */
    private int SCENE_HEIGHT = 300;
    
    /**
     * The JavaFX Scene instance for this scene.
     */
    private Scene scene;
    
    /**
     * The FXML file associated with this scene.
     */
    private final String fxmlFile;
    
    /**
     * The JavaFX Stage on which this scene will be displayed.
     */
    private final Stage stage;
    
    /**
     * The session instance for accessing application state.
     */
    protected SessionService sessionService;
    
    /**
     * The controller associated with this scene.
     */
    protected Controller controller;

    /**
     * Creates a new scene with the specified FXML file, stage, and session.
     * 
     * @param fxmlFile The path to the FXML file that defines the scene layout
     * @param stage The stage on which this scene will be displayed
     * @param sessionService The application session instance
     */
    public SuperScene(String fxmlFile, Stage stage, SessionService sessionService) {
        this.fxmlFile = fxmlFile;
        this.stage = stage;
        this.sessionService = sessionService;
    }

    /**
     * Initializes the scene by loading the FXML file and setting up the controller.
     * If the scene is already initialized, this method does nothing.
     * 
     * @param usedStage The stage to initialize the scene on (Could be different from the default stage)
     */
    public void initScene(Stage usedStage) {
        try {
            if (this.scene == null) {
                logger.debug("Loading FXML file: " + this.fxmlFile);
                // Load the FXML file and get the root node and controller
                LoadResult<Pane> loadResult = FXMLLoaderService.loadFXMLWithLoader(this.fxmlFile);
                Pane rootNode = loadResult.getRoot();
                this.controller = loadResult.getController();
                
                // Initialize the controller with the scene manager
                SceneManager sceneManager = SceneManager.getInstance();
                this.controller.initSceneManager(sceneManager);
                
                // Create the JavaFX scene with the root node
                this.scene = new Scene(rootNode, SCENE_WIDTH, SCENE_HEIGHT);
                
                // Apply the application's CSS stylesheet
                this.scene.getStylesheets().add(this.getClass().getClassLoader().getResource("css/telegram-style.css").toExternalForm());
            }
        } catch (IOException e) {
            this.logger.error("Failed to load FXML file: " + this.fxmlFile, e);
            throw new RuntimeException("Failed to load FXML file: " + this.fxmlFile, e);
        }
    }
    
    /**
     * Initializes the scene using the default stage.
     * Convenience method that delegates to initScene(Stage).
     */
    public void initScene() {
        this.initScene(this.stage);
    }

    /**
     * Sets the scene width.
     * Should be called before initializing the scene.
     * 
     * @param sceneWidth The width in pixels
     */
    protected void setSceneWidth(int sceneWidth) {
        this.SCENE_WIDTH = sceneWidth;
    }

    /**
     * Sets the scene height.
     * Should be called before initializing the scene.
     * 
     * @param sceneHeight The height in pixels
     */
    protected void setSceneHeight(int sceneHeight) {
        this.SCENE_HEIGHT = sceneHeight;
    }

    /**
     * Gets the JavaFX Scene instance.
     * Initializes the scene if it hasn't been initialized yet.
     * 
     * @return The JavaFX Scene instance
     */
    public Scene getScene() {
        if (this.scene == null) {
            this.initScene();
        }
        return this.scene;
    }

    /**
     * Resets the scene by reinitializing it.
     * Useful when the scene needs to be refreshed with updated data.
     */
    public void resetScene() {
        this.initScene();
    }

    /**
     * Resets both the scene and controller state.
     * This ensures fresh data is loaded when the scene is reopened.
     */
    public void reset() {
        // Reset the scene itself
        this.resetScene();
        
        // If controller is initializable, force it to reload its data
        if (this.controller != null && this.controller instanceof Controller) {
            ((Controller) this.controller).reload();
        }
    }

    /**
     * Reloads the controller associated with this scene.
     * This refreshes the data displayed in the scene without reinitializing the entire scene.
     */
    public void reload() {
        this.reset();
    }

    /**
     * Gets the controller for this scene.
     * 
     * @return The controller instance
     */
    public Controller getController() {
        return this.controller;
    }
}
