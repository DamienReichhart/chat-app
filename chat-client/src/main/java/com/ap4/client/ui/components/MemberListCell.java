package com.ap4.client.ui.components;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ap4.client.controllers.ChannelMembersController;
import com.ap4.common.enums.Role;
import com.ap4.common.models.ChatParticipant;
import com.ap4.common.models.User;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Custom ListCell implementation for displaying chat members with role selection.
 * Shows user information and a role selection dropdown for each member.
 */
public class MemberListCell extends ListCell<ChatParticipant> {
    private static final Logger logger = LogManager.getLogger(MemberListCell.class);
    
    // UI Constants
    private static final double AVATAR_RADIUS = 20;
    private static final double SPACING = 10;
    private static final double PADDING = 8;
    private static final double NAME_FONT_SIZE = 14;
    private static final double USERNAME_FONT_SIZE = 12;
    
    // Color mapping for roles
    private static final Color OWNER_COLOR = Color.GOLD;
    private static final Color ADMIN_COLOR = Color.CORNFLOWERBLUE;
    private static final Color MEMBER_COLOR = Color.LIGHTGRAY;
    
    // UI Components
    private final HBox container;
    private final Circle avatar;
    private final Label avatarLabel;
    private final Label nameLabel;
    private final Label usernameLabel;
    private final ComboBox<Role> roleComboBox;
    
    // Data
    private final ChannelMembersController controller;
    private final User currentUser;
    private boolean canEditRoles;

    /**
     * Constructs a new MemberListCell.
     * 
     * @param controller The controller managing the list view
     * @param currentUser The currently logged-in user
     * @param canEditRoles Whether the current user has permission to edit roles
     */
    public MemberListCell(ChannelMembersController controller, User currentUser, boolean canEditRoles) {
        this.controller = controller;
        this.currentUser = currentUser;
        this.canEditRoles = canEditRoles;
        
        // Main container for the cell
        container = new HBox();
        container.setAlignment(Pos.CENTER_LEFT);
        container.setSpacing(SPACING);
        container.setPadding(new Insets(PADDING));
        
        // Avatar setup
        avatar = new Circle(AVATAR_RADIUS);
        avatar.setFill(MEMBER_COLOR);
        avatar.setStroke(Color.GRAY);
        avatar.setStrokeWidth(1);
        
        // Avatar text (first letter of username)
        avatarLabel = new Label();
        avatarLabel.setTextFill(Color.WHITE);
        avatarLabel.setFont(Font.font("System", FontWeight.BOLD, NAME_FONT_SIZE));
        avatarLabel.setTranslateX(-6);
        
        // Create a container for the avatar and its label
        StackPane avatarPane = new StackPane();
        avatarPane.getChildren().addAll(avatar, avatarLabel);
        
        // User information container
        VBox userInfo = new VBox(2);
        userInfo.setAlignment(Pos.CENTER_LEFT);
        
        // User display name
        nameLabel = new Label();
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, NAME_FONT_SIZE));
        
        // User email or other identifier
        usernameLabel = new Label();
        usernameLabel.setFont(Font.font("System", USERNAME_FONT_SIZE));
        usernameLabel.setTextFill(Color.GRAY);
        
        userInfo.getChildren().addAll(nameLabel, usernameLabel);
        HBox.setHgrow(userInfo, Priority.ALWAYS);
        
        // Role selection dropdown
        roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll(Role.values());
        roleComboBox.setDisable(!canEditRoles);
        roleComboBox.setOnAction(e -> handleRoleChange());
        
        // Add tooltip to explain role selection
        Tooltip roleTooltip = new Tooltip("Select a role for this member");
        roleComboBox.setTooltip(roleTooltip);
        
        // Spacer to push the combo box to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Add components to container
        container.getChildren().addAll(avatarPane, userInfo, spacer, roleComboBox);
    }
    
    /**
     * Handles role change events from the combo box.
     */
    private void handleRoleChange() {
        ChatParticipant participant = getItem();
        if (participant == null || roleComboBox.getValue() == null) {
            logger.warn("Cannot update role: participant or selected role is null");
            return;
        }
        
        try {
            Role newRole = roleComboBox.getValue();
            logger.debug("Role selection changed to: {}", newRole);
            
            // Always request a valid participant with current chat context from the controller
            // This ensures we're always working with the correct chat reference
            ChatParticipant contextualParticipant = controller.getParticipantWithCurrentChat(participant);
            
            if (contextualParticipant == null) {
                logger.error("Failed to get participant with valid chat reference");
                
                // Reset to original value in case of error
                roleComboBox.setValue(participant.getRole());
                return;
            }
            
            // Use the participant with valid chat context for the update
            controller.updateMemberRole(contextualParticipant, newRole);
        } catch (Exception e) {
            logger.error("Error handling role change: {}", e.getMessage(), e);
            // Reset to original value in case of error
            if (getItem() != null) {
                roleComboBox.setValue(getItem().getRole());
            }
        }
    }

    @Override
    protected void updateItem(ChatParticipant participant, boolean empty) {
        super.updateItem(participant, empty);
        
        try {
            if (empty || participant == null) {
                setText(null);
                setGraphic(null);
                setStyle("");
                return;
            }
            
            User user = participant.getUser();
            if (user == null) {
                setText("Invalid user");
                setGraphic(null);
                return;
            }
            
            // Update user information
            updateUserInfo(user);
            
            // Set role in dropdown
            roleComboBox.setValue(participant.getRole());
            
            // Configure avatar
            configureAvatar(user, participant.getRole());
            
            // Handle role editing permissions
            configureRoleEditing(user, participant.getRole());
            
            // Set the cell's graphic
            setGraphic(container);
        } catch (Exception e) {
            logger.error("Error updating member list cell: {}", e.getMessage(), e);
            setText("Error displaying member");
            setGraphic(null);
        }
    }
    
    /**
     * Updates the user information display.
     * 
     * @param user The user to display
     */
    private void updateUserInfo(User user) {
        // Set display name (username)
        String username = user.getUsername();
        if (username == null || username.isEmpty()) {
            username = "Unknown User";
        }
        nameLabel.setText(username);
        
        // Set email or secondary information
        String email = user.getEmail();
        if (email == null || email.isEmpty()) {
            usernameLabel.setText("No email");
        } else {
            usernameLabel.setText("@" + email);
        }
    }
    
    /**
     * Configures the avatar appearance based on user and role.
     * 
     * @param user The user
     * @param role The user's role
     */
    private void configureAvatar(User user, Role role) {
        // Set avatar color based on role
        Color avatarColor;
        switch (role) {
            case OWNER:
                avatarColor = OWNER_COLOR;
                break;
            case ADMIN:
                avatarColor = ADMIN_COLOR;
                break;
            default:
                avatarColor = MEMBER_COLOR;
        }
        avatar.setFill(avatarColor);
        
        // Set avatar text (first letter of username)
        String username = user.getUsername();
        if (username != null && !username.isEmpty()) {
            avatarLabel.setText(username.substring(0, 1).toUpperCase());
        } else {
            avatarLabel.setText("?");
        }
    }
    
    /**
     * Configures role editing permissions and highlights for the current user.
     * 
     * @param user The user
     * @param role The user's role
     */
    private void configureRoleEditing(User user, Role role) {
        // Check if this cell represents the current user
        boolean isSelf = currentUser != null && user.getId() == currentUser.getId();
        
        // Disable dropdown for self (you can't change your own role)
        roleComboBox.setDisable(!canEditRoles || isSelf);
        
        // Add tooltip explanation for disabled state
        if (isSelf) {
            roleComboBox.setTooltip(new Tooltip("You cannot change your own role"));
        } else if (!canEditRoles) {
            roleComboBox.setTooltip(new Tooltip("You don't have permission to change roles"));
        } else {
            roleComboBox.setTooltip(new Tooltip("Select a role for this member"));
        }
        
        // Highlight the current user's row
        if (isSelf) {
            setStyle("-fx-background-color: #f0f7ff;");
        } else {
            setStyle("");
        }
    }
    
    /**
     * Updates whether this cell can edit roles.
     * 
     * @param canEdit Whether role editing should be enabled
     */
    public void setCanEditRoles(boolean canEdit) {
        this.canEditRoles = canEdit;
        
        if (getItem() != null && getItem().getUser() != null && currentUser != null) {
            boolean isSelf = getItem().getUser().getId() == currentUser.getId();
            roleComboBox.setDisable(!canEdit || isSelf);
            
            // Update tooltip
            if (isSelf) {
                roleComboBox.setTooltip(new Tooltip("You cannot change your own role"));
            } else if (!canEdit) {
                roleComboBox.setTooltip(new Tooltip("You don't have permission to change roles"));
            } else {
                roleComboBox.setTooltip(new Tooltip("Select a role for this member"));
            }
        }
    }
} 