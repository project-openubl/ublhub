package io.github.project.openubl.xsender.models;

public interface DocumentEvent {
    interface Created {
        String getId();

        String getCompanyId();
    }

    interface Updated {
        String getId();

        String getCompanyId();
    }

    interface Deleted {
        String getId();

        String getCompanyId();
    }
}
