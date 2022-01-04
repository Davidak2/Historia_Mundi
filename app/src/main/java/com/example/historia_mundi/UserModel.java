package com.example.historia_mundi;

public class UserModel {

    String email,username,image;
    boolean isNotificationEnabled;

    public UserModel() {
    }

    /**
     * Constructor class need for user info
     * like Email, password etc.
     */

    public UserModel(String email, String username, String image, boolean isNotificationEnabled) {
        this.email = email;
        this.username = username;
        this.image = image;
        this.isNotificationEnabled = isNotificationEnabled;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {return username; }

    public void setUsername(String username) { this.username = username; }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isNotificationEnabled() {
        return isNotificationEnabled;
    }

    public void setNotificationEnabled(boolean notificationEnabled) {
        isNotificationEnabled = notificationEnabled;
    }
}
