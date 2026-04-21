package com.trohub.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "upload")
public class UploadProperties {

    /**
     * Max file size in bytes
     */
    private long maxSize = 5242880L;

    /**
     * Allowed content types
     */
    private List<String> allowedTypes = List.of("image/png","image/jpeg","image/jpg","image/gif","application/pdf");

    private String baseDir = "uploads";

    private String incidentsDir = "incidents";

    public long getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(long maxSize) {
        this.maxSize = maxSize;
    }

    public List<String> getAllowedTypes() {
        return allowedTypes;
    }

    public void setAllowedTypes(List<String> allowedTypes) {
        this.allowedTypes = allowedTypes;
    }

    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public String getIncidentsDir() {
        return incidentsDir;
    }

    public void setIncidentsDir(String incidentsDir) {
        this.incidentsDir = incidentsDir;
    }
}

