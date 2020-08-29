package com.sabo.sabostore.EventBus;

import com.sabo.sabostore.RoomDB.Favorite.FavoriteItem;

public class BottomSheetDialogEvent {
    private boolean clicked;

    public BottomSheetDialogEvent(boolean clicked) {
        this.clicked = clicked;
    }

    public boolean isClicked() {
        return clicked;
    }

    public void setClicked(boolean clicked) {
        this.clicked = clicked;
    }
}
