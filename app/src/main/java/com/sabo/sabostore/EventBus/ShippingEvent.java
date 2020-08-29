package com.sabo.sabostore.EventBus;

public class ShippingEvent {
    private boolean checked;
    private int state;

    public ShippingEvent(boolean checked, int state) {
        this.checked = checked;
        this.state = state;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
