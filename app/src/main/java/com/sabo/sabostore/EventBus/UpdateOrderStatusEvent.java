package com.sabo.sabostore.EventBus;

public class UpdateOrderStatusEvent {
    private boolean updated;

    public UpdateOrderStatusEvent(boolean updated) {
        this.updated = updated;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }
}
