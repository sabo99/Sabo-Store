package com.sabo.sabostore.EventBus;

public class UpdateStatusUserEvent {
    private boolean on, off;

    public UpdateStatusUserEvent(boolean on, boolean off) {
        this.on = on;
        this.off = off;
    }

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    public boolean isOff() {
        return off;
    }

    public void setOff(boolean off) {
        this.off = off;
    }
}
