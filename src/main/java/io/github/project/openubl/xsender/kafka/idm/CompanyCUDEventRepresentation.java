package io.github.project.openubl.xsender.kafka.idm;

public class CompanyCUDEventRepresentation {

    private String id;
    private String owner;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
