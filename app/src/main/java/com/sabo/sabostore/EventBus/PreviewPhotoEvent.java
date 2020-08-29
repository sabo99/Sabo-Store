package com.sabo.sabostore.EventBus;

public class PreviewPhotoEvent {
    private boolean preview, edit;
    private String urlPhoto;

    public PreviewPhotoEvent(boolean preview, boolean edit, String urlPhoto) {
        this.preview = preview;
        this.edit = edit;
        this.urlPhoto = urlPhoto;
    }

    public boolean isPreview() {
        return preview;
    }

    public void setPreview(boolean preview) {
        this.preview = preview;
    }

    public boolean isEdit() {
        return edit;
    }

    public void setEdit(boolean edit) {
        this.edit = edit;
    }

    public String getUrlPhoto() {
        return urlPhoto;
    }

    public void setUrlPhoto(String urlPhoto) {
        this.urlPhoto = urlPhoto;
    }
}
