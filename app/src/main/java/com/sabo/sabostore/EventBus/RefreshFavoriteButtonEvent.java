package com.sabo.sabostore.EventBus;

public class RefreshFavoriteButtonEvent {
    private boolean refresh;

    public RefreshFavoriteButtonEvent(boolean refresh) {
        this.refresh = refresh;
    }

    public boolean isRefresh() {
        return refresh;
    }

    public void setRefresh(boolean refresh) {
        this.refresh = refresh;
    }
}
