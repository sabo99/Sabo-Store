package com.sabo.sabostore.EventBus;

public class NavCartEvent {
    private boolean clicked;

    public NavCartEvent(boolean clicked) {
        this.clicked = clicked;
    }

    public boolean isClicked() {
        return clicked;
    }

    public void setClicked(boolean clicked) {
        this.clicked = clicked;
    }
}
