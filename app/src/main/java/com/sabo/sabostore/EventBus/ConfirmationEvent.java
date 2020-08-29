package com.sabo.sabostore.EventBus;

public class ConfirmationEvent {
    private boolean editShipping, editOrder;

    public ConfirmationEvent(boolean editShipping, boolean editOrder) {
        this.editShipping = editShipping;
        this.editOrder = editOrder;
    }

    public boolean isEditShipping() {
        return editShipping;
    }

    public void setEditShipping(boolean editShipping) {
        this.editShipping = editShipping;
    }

    public boolean isEditOrder() {
        return editOrder;
    }

    public void setEditOrder(boolean editOrder) {
        this.editOrder = editOrder;
    }
}
