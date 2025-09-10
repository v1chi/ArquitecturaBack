package com.team.socialnetwork.dto;

public class RelationshipResponse {
    private boolean following;
    private boolean followsYou;
    private boolean requested;
    private boolean blocked;

    public RelationshipResponse() {}

    public RelationshipResponse(boolean following, boolean followsYou, boolean requested, boolean blocked) {
        this.following = following;
        this.followsYou = followsYou;
        this.requested = requested;
        this.blocked = blocked;
    }

    public boolean isFollowing() { return following; }
    public void setFollowing(boolean following) { this.following = following; }
    public boolean isFollowsYou() { return followsYou; }
    public void setFollowsYou(boolean followsYou) { this.followsYou = followsYou; }
    public boolean isRequested() { return requested; }
    public void setRequested(boolean requested) { this.requested = requested; }
    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
}

