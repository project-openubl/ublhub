package org.openubl.models;

public enum FileType {
    XML("xml"),
    ZIP("zip");

    private String extension;

    FileType(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }

}
