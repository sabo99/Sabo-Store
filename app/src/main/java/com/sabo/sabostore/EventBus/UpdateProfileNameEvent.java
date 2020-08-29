package com.sabo.sabostore.EventBus;

public class UpdateProfileNameEvent {
    private boolean updated;
    private String name;

    public UpdateProfileNameEvent(boolean updated, String name) {
        this.updated = updated;
        this.name = name;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
