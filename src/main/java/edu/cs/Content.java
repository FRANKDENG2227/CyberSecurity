package edu.cs;

import java.time.LocalDateTime;

public class Content {
    private String fileName;
    private LocalDateTime uploadTime;
    private String formattedUploadTime;

    // Getters and Setters
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public LocalDateTime getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }

    public String getFormattedUploadTime() {
        return formattedUploadTime;
    }

    public void setFormattedUploadTime(String formattedUploadTime) {
        this.formattedUploadTime = formattedUploadTime;
    }
}
