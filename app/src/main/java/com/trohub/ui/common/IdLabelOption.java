package com.trohub.ui.common;

public class IdLabelOption {
    private final Long id;
    private final String label;

    public IdLabelOption(Long id, String label) {
        this.id = id;
        this.label = label;
    }

    public Long getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label == null ? "" : label;
    }
}
