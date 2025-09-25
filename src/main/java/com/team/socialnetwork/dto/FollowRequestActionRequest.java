package com.team.socialnetwork.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class FollowRequestActionRequest {
    @NotBlank(message = "Action is required")
    @Pattern(regexp = "accept|reject", message = "Action must be 'accept' or 'reject'")
    private String action;

    public FollowRequestActionRequest() {}

    public FollowRequestActionRequest(String action) {
        this.action = action;
    }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
}