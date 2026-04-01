package ru.goidaai.test_backend.dto;

import java.math.BigDecimal;

public class AvatarInfoDTO {

    private String id;
    private String url;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private Integer width;
    private Integer height;
    private Boolean isActive;
    private BigDecimal uploadedAt;

    public AvatarInfoDTO() {
    }

    public AvatarInfoDTO(String id, String url, String fileName, String contentType, Long fileSize, Integer width, Integer height, Boolean isActive, BigDecimal uploadedAt) {
        this.id = id;
        this.url = url;
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.width = width;
        this.height = height;
        this.isActive = isActive;
        this.uploadedAt = uploadedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public BigDecimal getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(BigDecimal uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
