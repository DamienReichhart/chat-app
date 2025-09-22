package com.ap4.client.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.scene.paint.Color;

/**
 * Configuration class for UI components.
 * Provides centralized access to UI-related settings and theme information.
 */
public class UIConfig {
    private static final Logger logger = LogManager.getLogger(UIConfig.class);
    
    // Theme configuration values
    private static final Color DEFAULT_PRIMARY_COLOR = Color.web("#3498db");
    private static final Color DEFAULT_SECONDARY_COLOR = Color.web("#2ecc71");
    private static final Color DEFAULT_BACKGROUND_COLOR = Color.web("#f8f9fa");
    private static final Color DEFAULT_TEXT_COLOR = Color.web("#212529");
    private static final String DEFAULT_FONT_FAMILY = "System";
    
    // UI dimensions
    private static final double DEFAULT_WINDOW_WIDTH = 1024;
    private static final double DEFAULT_WINDOW_HEIGHT = 768;
    private static final double DEFAULT_MIN_WINDOW_WIDTH = 800;
    private static final double DEFAULT_MIN_WINDOW_HEIGHT = 600;
    
    // Current theme settings
    private Color primaryColor;
    private Color secondaryColor;
    private Color backgroundColor;
    private Color textColor;
    private String fontFamily;
    private double fontSize;
    
    // Window settings
    private double windowWidth;
    private double windowHeight;
    private double minWindowWidth;
    private double minWindowHeight;
    
    /**
     * Creates a new UIConfig with default settings.
     */
    public UIConfig() {
        // Initialize with default theme settings
        this.primaryColor = DEFAULT_PRIMARY_COLOR;
        this.secondaryColor = DEFAULT_SECONDARY_COLOR;
        this.backgroundColor = DEFAULT_BACKGROUND_COLOR;
        this.textColor = DEFAULT_TEXT_COLOR;
        this.fontFamily = DEFAULT_FONT_FAMILY;
        this.fontSize = 12.0;
        
        // Initialize with default window settings
        this.windowWidth = DEFAULT_WINDOW_WIDTH;
        this.windowHeight = DEFAULT_WINDOW_HEIGHT;
        this.minWindowWidth = DEFAULT_MIN_WINDOW_WIDTH;
        this.minWindowHeight = DEFAULT_MIN_WINDOW_HEIGHT;
        
        logger.debug("UI configuration initialized with default settings");
    }
    
    /**
     * Gets the primary theme color.
     * 
     * @return The current primary color
     */
    public Color getPrimaryColor() {
        return primaryColor;
    }
    
    /**
     * Sets the primary theme color.
     * 
     * @param primaryColor The new primary color
     */
    public void setPrimaryColor(Color primaryColor) {
        this.primaryColor = primaryColor;
    }
    
    /**
     * Gets the secondary theme color.
     * 
     * @return The current secondary color
     */
    public Color getSecondaryColor() {
        return secondaryColor;
    }
    
    /**
     * Sets the secondary theme color.
     * 
     * @param secondaryColor The new secondary color
     */
    public void setSecondaryColor(Color secondaryColor) {
        this.secondaryColor = secondaryColor;
    }
    
    /**
     * Gets the background color.
     * 
     * @return The current background color
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }
    
    /**
     * Sets the background color.
     * 
     * @param backgroundColor The new background color
     */
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
    
    /**
     * Gets the text color.
     * 
     * @return The current text color
     */
    public Color getTextColor() {
        return textColor;
    }
    
    /**
     * Sets the text color.
     * 
     * @param textColor The new text color
     */
    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }
    
    /**
     * Gets the font family.
     * 
     * @return The current font family
     */
    public String getFontFamily() {
        return fontFamily;
    }
    
    /**
     * Sets the font family.
     * 
     * @param fontFamily The new font family
     */
    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }
    
    /**
     * Gets the font size.
     * 
     * @return The current font size
     */
    public double getFontSize() {
        return fontSize;
    }
    
    /**
     * Sets the font size.
     * 
     * @param fontSize The new font size
     */
    public void setFontSize(double fontSize) {
        this.fontSize = fontSize;
    }
    
    /**
     * Gets the window width.
     * 
     * @return The current window width
     */
    public double getWindowWidth() {
        return windowWidth;
    }
    
    /**
     * Sets the window width.
     * 
     * @param windowWidth The new window width
     */
    public void setWindowWidth(double windowWidth) {
        this.windowWidth = windowWidth;
    }
    
    /**
     * Gets the window height.
     * 
     * @return The current window height
     */
    public double getWindowHeight() {
        return windowHeight;
    }
    
    /**
     * Sets the window height.
     * 
     * @param windowHeight The new window height
     */
    public void setWindowHeight(double windowHeight) {
        this.windowHeight = windowHeight;
    }
    
    /**
     * Gets the minimum window width.
     * 
     * @return The current minimum window width
     */
    public double getMinWindowWidth() {
        return minWindowWidth;
    }
    
    /**
     * Sets the minimum window width.
     * 
     * @param minWindowWidth The new minimum window width
     */
    public void setMinWindowWidth(double minWindowWidth) {
        this.minWindowWidth = minWindowWidth;
    }
    
    /**
     * Gets the minimum window height.
     * 
     * @return The current minimum window height
     */
    public double getMinWindowHeight() {
        return minWindowHeight;
    }
    
    /**
     * Sets the minimum window height.
     * 
     * @param minWindowHeight The new minimum window height
     */
    public void setMinWindowHeight(double minWindowHeight) {
        this.minWindowHeight = minWindowHeight;
    }
    
    /**
     * Gets CSS style string for applying the theme to UI components.
     * 
     * @return CSS style string
     */
    public String getThemeStylesheet() {
        return String.format(
            ".root { -fx-font-family: '%s'; -fx-font-size: %.1fpx; } " +
            ".background { -fx-background-color: %s; } " +
            ".text { -fx-fill: %s; } " +
            ".primary { -fx-background-color: %s; } " +
            ".secondary { -fx-background-color: %s; }",
            fontFamily, fontSize,
            toRgbCode(backgroundColor),
            toRgbCode(textColor),
            toRgbCode(primaryColor),
            toRgbCode(secondaryColor)
        );
    }
    
    /**
     * Converts a JavaFX Color to RGB code string.
     * 
     * @param color The color to convert
     * @return The RGB code string
     */
    private String toRgbCode(Color color) {
        return String.format(
            "rgb(%d, %d, %d)",
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255)
        );
    }
} 