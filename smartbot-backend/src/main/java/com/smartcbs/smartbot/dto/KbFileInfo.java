package com.smartcbs.smartbot.dto;

public class KbFileInfo {
    private String filename;
    private long size;
    private long lastModified;

    public KbFileInfo() {}

    public KbFileInfo(String filename, long size, long lastModified) {
        this.filename = filename;
        this.size = size;
        this.lastModified = lastModified;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }
}
