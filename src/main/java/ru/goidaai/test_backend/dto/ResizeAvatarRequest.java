package ru.goidaai.test_backend.dto;

public class ResizeAvatarRequest {

    private Integer width;
    private Integer height;
    private Boolean maintainAspectRatio;

    public ResizeAvatarRequest() {
    }

    public ResizeAvatarRequest(Integer width, Integer height, Boolean maintainAspectRatio) {
        this.width = width;
        this.height = height;
        this.maintainAspectRatio = maintainAspectRatio;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Boolean getMaintainAspectRatio() {
        return maintainAspectRatio;
    }

    public void setMaintainAspectRatio(Boolean maintainAspectRatio) {
        this.maintainAspectRatio = maintainAspectRatio;
    }
}
