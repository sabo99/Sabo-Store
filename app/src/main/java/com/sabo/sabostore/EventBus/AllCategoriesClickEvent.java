package com.sabo.sabostore.EventBus;

public class AllCategoriesClickEvent {
    private boolean clicked;

    public AllCategoriesClickEvent(boolean clicked) {
        this.clicked = clicked;
    }

    public boolean isClicked() {
        return clicked;
    }

    public void setClicked(boolean clicked) {
        this.clicked = clicked;
    }
}
