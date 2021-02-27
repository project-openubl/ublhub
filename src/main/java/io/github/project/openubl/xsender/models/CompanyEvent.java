package io.github.project.openubl.xsender.models;

public interface CompanyEvent {
    interface Created {
        String getId();

        String getOwner();
    }

    interface Updated {
        String getId();

        String getOwner();
    }

    interface Deleted {
        String getId();

        String getOwner();
    }
}
