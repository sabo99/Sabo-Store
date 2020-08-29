package com.sabo.sabostore.EventBus;

public class HideAppBarProfileEvent {
    private boolean profileHidden;

    public HideAppBarProfileEvent(boolean profileHidden) {
        this.profileHidden = profileHidden;
    }

    public boolean isProfileHidden() {
        return profileHidden;
    }

    public void setProfileHidden(boolean profileHidden) {
        this.profileHidden = profileHidden;
    }
}
