package com.ap4.client.exceptions.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Exception thrown when data validation fails.
 * Contains detailed information about validation errors.
 */
public class DataValidationException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final List<ValidationError> validationErrors;
    
    /**
     * Creates a new DataValidationException with a message and list of validation errors.
     * 
     * @param message The general validation error message
     * @param validationErrors The list of detailed validation errors
     */
    public DataValidationException(String message, List<ValidationError> validationErrors) {
        super(message);
        this.validationErrors = new ArrayList<>(validationErrors);
    }
    
    /**
     * Creates a new DataValidationException with a single validation error.
     * 
     * @param message The general validation error message
     * @param field The field that failed validation
     * @param errorMessage The specific error message for the field
     */
    public DataValidationException(String message, String field, String errorMessage) {
        super(message);
        this.validationErrors = new ArrayList<>();
        this.validationErrors.add(new ValidationError(field, errorMessage));
    }
    
    /**
     * Creates a new DataValidationException with only a message.
     * 
     * @param message The validation error message
     */
    public DataValidationException(String message) {
        super(message);
        this.validationErrors = new ArrayList<>();
    }
    
    /**
     * Gets the list of validation errors.
     * 
     * @return An unmodifiable list of validation errors
     */
    public List<ValidationError> getValidationErrors() {
        return Collections.unmodifiableList(validationErrors);
    }
    
    /**
     * Adds a new validation error to the list.
     * 
     * @param field The field that failed validation
     * @param errorMessage The specific error message for the field
     * @return This exception instance for method chaining
     */
    public DataValidationException addValidationError(String field, String errorMessage) {
        this.validationErrors.add(new ValidationError(field, errorMessage));
        return this;
    }
    
    /**
     * Checks if there are any validation errors.
     * 
     * @return true if there are validation errors, false otherwise
     */
    public boolean hasErrors() {
        return !validationErrors.isEmpty();
    }
    
    /**
     * Class representing a specific validation error.
     */
    public static class ValidationError {
        private final String field;
        private final String message;
        
        /**
         * Creates a new ValidationError.
         * 
         * @param field The field that failed validation
         * @param message The validation error message
         */
        public ValidationError(String field, String message) {
            this.field = field;
            this.message = message;
        }
        
        /**
         * Gets the field that failed validation.
         * 
         * @return The field name
         */
        public String getField() {
            return field;
        }
        
        /**
         * Gets the validation error message.
         * 
         * @return The error message
         */
        public String getMessage() {
            return message;
        }
        
        @Override
        public String toString() {
            return field + ": " + message;
        }
    }

    /**
     * Gets a formatted string of all validation errors.
     * 
     * @return A formatted string of all validation errors
     */
    public String getFormattedErrors() {
        return validationErrors.stream()
            .map(ValidationError::toString)
            .collect(Collectors.joining("\n"));
    }
} 