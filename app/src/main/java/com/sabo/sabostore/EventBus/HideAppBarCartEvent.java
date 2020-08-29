package com.sabo.sabostore.EventBus;

public class HideAppBarCartEvent {
    private boolean cartHidden;

    public HideAppBarCartEvent(boolean cartHidden) {
        this.cartHidden = cartHidden;
    }

    public boolean isCartHidden() {
        return cartHidden;
    }

    public void setCartHidden(boolean cartHidden) {
        this.cartHidden = cartHidden;
    }
}
