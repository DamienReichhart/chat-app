package com.ap4.client.interfaces.controllers;

import com.ap4.client.scenes.SceneManager;
import javafx.fxml.Initializable;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Interface for controllers in the application.
 * <p>
 * This interface defines the basic contract for all controllers,
 * including scene manager initialization and data/view reloading.
 * Controllers should also implement the {@link Initializable#initialize(URL, ResourceBundle)}
 * method as required by the JavaFX lifecycle.
 * </p>
 */
public interface IController extends Initializable {

    /**
     * Initializes the scene manager for this controller.
     *
     * @param sceneManager the scene manager instance to use for scene navigation
     */
    void initSceneManager(SceneManager sceneManager);

    /**
     * Reloads the controller's data and view elements.
     * <p>
     * This method should be implemented by controllers to refresh their content.
     * </p>
     */
    void reload();
}
