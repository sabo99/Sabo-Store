package com.sabo.sabostore.EventBus;

public class ActionProfilePhotoEvent {
    private boolean remove, change;

    public ActionProfilePhotoEvent(boolean remove, boolean change) {
        this.remove = remove;
        this.change = change;
    }

    public boolean isRemove() {
        return remove;
    }

    public void setRemove(boolean remove) {
        this.remove = remove;
    }

    public boolean isChange() {
        return change;
    }

    public void setChange(boolean change) {
        this.change = change;
    }
}
