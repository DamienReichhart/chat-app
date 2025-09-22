package com.ap4.client.scenes;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ap4.client.config.ApplicationConfig;
import com.ap4.client.db.DatabaseManager;
import com.ap4.client.exceptions.middleware.MiddlewareDoNotPassException;
import com.ap4.client.middlewares.Middleware;
import com.ap4.client.services.SessionService;

import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Manages scenes and scene transitions in the JavaFX application.
 * 
 * This class implements the Singleton pattern to ensure a single instance
 * manages all UI navigation throughout the application. It is responsible for:
 * - Initializing and storing all application scenes
 * - Handling scene switching with appropriate middleware processing
 * - Managing popup window creation and lifecycle
 * - Ensuring proper scene reloading and state management
 * 
 * The SceneManager serves as the central navigation controller for the entire
 * application, providing a consistent interface for all scene transitions.
 */
public class SceneManager {
    private static final Logger logger = LogManager.getLogger(SceneManager.class);
    
    /**
     * Singleton instance of the SceneManager.
     */
    private static SceneManager instance;
    
    /**
     * The primary stage for the application.
     */
    private Stage primaryStage;
    
    /**
     * Collection of all scenes available in the application.
     */
    private final HashMap<String, SuperScene> scenes;
    
    /**
     * Application session containing user and app state.
     */
    private SessionService sessionService;
    
    /**
     * Application configuration containing settings and middleware definitions.
     */
    private ApplicationConfig applicationConfig;
    
    /**
     * Collection of popup stages currently displayed.
     */
    private final HashMap<String, Stage> popUpStages;
    
    /**
     * Database manager for database connections.
     */
    private DatabaseManager dbManager;

    /**
     * Private constructor to enforce the Singleton pattern.
     * Initializes the scene collection.
     */
    private SceneManager() {
        this.scenes = new HashMap<>();
        this.popUpStages = new HashMap<>();
        logger.info("SceneManager created");
    }
    
    /**
     * Initialize the scene manager with required dependencies.
     * This must be called before using the scene manager.
     *
     * @param primaryStage The main application stage
     * @param sessionService The application session instance
     * @param applicationConfig The application configuration
     * @param dbManager The database manager instance
     */
    public void initialize(Stage primaryStage, SessionService sessionService, 
                           ApplicationConfig applicationConfig, DatabaseManager dbManager) {
        this.primaryStage = primaryStage;
        this.sessionService = sessionService;
        this.applicationConfig = applicationConfig;
        this.dbManager = dbManager;
        
        // Set the application title
        String windowTitle = applicationConfig.getProperty("ui.window.title", "Chat Client");
        primaryStage.setTitle(windowTitle);
        
        // Initialize all scenes
        initScenes();
        
        logger.info("SceneManager initialized with {} scenes", scenes.size());
    }

    /**
     * Gets the singleton instance of the SceneManager.
     * 
     * @return The singleton SceneManager instance
     */
    public static synchronized SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    /**
     * Initializes all application scenes.
     * Creates instances of all scene classes and stores them in the scenes map.
     */
    private void initScenes() {
        registerMultipleScenes(List.of(
                LoadingScene.class,
                MainScene.class,
                ChannelAddScene.class,
                ChannelEditScene.class,
                GroupEditScene.class,
                UserEditScene.class,
                LoginScene.class,
                RegisterScene.class,
                ContactAddScene.class,
                GroupAddScene.class,
                ChannelJoinScene.class,
                GroupJoinScene.class,
                MessageScene.class,
                ChannelMembersScene.class
        ));
    }

    /**
     * Register multiple scenes at once in the scene manager.
     *
     * @param sceneClasses List of scene classes to register
     */
    private void registerMultipleScenes(List<Class<? extends SuperScene>> sceneClasses) {
        for (Class<? extends SuperScene> sceneClass : sceneClasses) {
            registerScene(sceneClass);
        }
    }

    /**
     * Register a specific scene in the scene manager.
     *
     * @param sceneClass The class of the scene to register
     */
    private void registerScene(Class<? extends SuperScene> sceneClass) {
        try {
            // Get the constructor that takes Stage and Session parameters
            Constructor<? extends SuperScene> constructor = sceneClass.getConstructor(Stage.class, SessionService.class);

            // Create a new instance using the constructor
            SuperScene sceneInstance = constructor.newInstance(this.primaryStage, this.sessionService);

            // Store the scene with the class name as key
            this.scenes.put(sceneClass.getSimpleName(), sceneInstance);
            logger.debug("Registered scene: {}", sceneClass.getSimpleName());
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            logger.error("Failed to register scene: {}", sceneClass.getSimpleName(), e);
            throw new RuntimeException("Failed to register scene: " + sceneClass.getSimpleName(), e);
        }
    }

    /**
     * Switches the primary stage to display a different scene.
     * Applies middleware processing before switching scenes and reloads
     * the scene data after switching.
     *
     * @param sceneClass The class of the scene to switch to
     */
    public void switchScene(Class<? extends SuperScene> sceneClass) {
        String sceneName = sceneClass.getSimpleName();
        SuperScene scene = this.scenes.get(sceneName);
        
        if (scene != null) {
            logger.debug("Switching to scene: {}", sceneName);
            
            // Apply middleware processing for this scene
            if (!this.callMiddlewares(sceneName, scene)) {
                logger.warn("Middleware blocked transition to scene: {}", sceneName);
                return;
            }
            
            // Reload scene data
            scene.reset();

            // Set the scene on the primary stage and display it
            primaryStage.setScene(scene.getScene());
            primaryStage.show();
            
            logger.info("Switched to scene: {}", sceneName);
        } else {
            logger.error("Cannot switch to scene: {} - Scene not found", sceneName);
        }
    }

    /**
     * Reloads the specified scene's data without switching to it.
     * Useful for refreshing a scene's content in response to data changes.
     *
     * @param sceneClass The class of the scene to reload
     */
    final public void reloadScene(Class sceneClass) {
        SuperScene scene = this.scenes.get(sceneClass.getSimpleName());
        if (scene != null) {
            scene.reload();
        }
    }

    /**
     * Applies all relevant middleware processing to a scene.
     * Combines global middlewares with scene-specific middlewares and
     * calls their handle method in sequence.
     *
     * @param sceneName The name of the scene for middleware lookup
     * @param scene The scene instance to pass to the middleware
     */
    final public boolean callMiddlewares(String sceneName, SuperScene scene) {
        // Get global middleware that applies to all scenes
        Middleware[] globalsMiddlewares = this.applicationConfig.getMiddlewaresConfig().getMiddleware("global");
        
        // Get middleware specific to this scene
        Middleware[] specificMiddlewares = this.applicationConfig.getMiddlewaresConfig().getMiddleware(sceneName);
        
        // Combine global and specific middlewares
        Middleware[] middlewares = ArrayUtils.addAll(globalsMiddlewares, specificMiddlewares);

        // Apply each middleware in sequence
        for (Middleware middleware : middlewares) {
            try {
                middleware.handle(scene);
            } catch (MiddlewareDoNotPassException e) {
                logger.error("Middleware do not pass", e);
                return false;
            }
        }
        return true;
    }

    /**
     * Opens a scene in a modal popup window.
     * The popup blocks interaction with the main window until closed.
     *
     * @param sceneClass The class of the scene to open as a popup
     */
    public void openPopUpScene(Class<? extends SuperScene> sceneClass) {
        SuperScene scene = this.scenes.get(sceneClass.getSimpleName());
        if(scene != null) {
            // Apply middleware processing for this scene
            this.callMiddlewares(sceneClass.getSimpleName(), scene);
            
            // Reset the scene to ensure fresh data loading
            scene.reset();
            
            // Create and configure a new popup stage
            Stage newStage = new Stage();
            newStage.initModality(Modality.APPLICATION_MODAL);
            newStage.initOwner(this.primaryStage);
            
            // Initialize the scene on the new stage
            scene.initScene(newStage);
            newStage.setScene(scene.getScene());

            scene.getController().reload();
            
            // Store the popup stage for later reference
            this.popUpStages.put(sceneClass.getSimpleName(), newStage);
            
            // Display the popup and wait for user interaction
            newStage.showAndWait();
        }
    }

    /**
     * Closes a popup window and removes it from the active popups collection.
     *
     * @param sceneClass The class of the scene to close
     */
    public void closePopUp(Class sceneClass) {
        this.popUpStages.get(sceneClass.getSimpleName()).close();
        this.popUpStages.remove(sceneClass.getSimpleName());
    }

    /**
     * Resets all scenes to their initial state.
     * This method iterates through all registered scenes and calls their reset method.
     */ 
    public void resetAllScenes() {
        for (SuperScene scene : this.scenes.values()) {
            scene.reset();
        }
    }
}
