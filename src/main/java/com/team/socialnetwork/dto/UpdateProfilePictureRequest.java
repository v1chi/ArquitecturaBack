package com.team.socialnetwork.dto;

import jakarta.validation.constraints.Size;

public class UpdateProfilePictureRequest {
    
    // Allow images up to ~10MB in base64 (approx. 13MB string length)
    @Size(max = 13_000_000, message = "Profile picture is too large (max 10MB)")
    private String profilePicture; // Base64 payload or URL/path

    public UpdateProfilePictureRequest() {}

    public UpdateProfilePictureRequest(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getProfilePicture() { 
        return profilePicture; 
    }
    
    public void setProfilePicture(String profilePicture) { 
        this.profilePicture = profilePicture; 
    }
}