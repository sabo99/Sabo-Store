package com.sabo.sabostore.EventBus;

public class HideFabEvent {
    private boolean fabHidden;

    public HideFabEvent(boolean fabHidden) {
        this.fabHidden = fabHidden;
    }

    public boolean isFabHidden() {
        return fabHidden;
    }

    public void setFabHidden(boolean fabHidden) {
        this.fabHidden = fabHidden;
    }
}
