package io.github.project.openubl.ublhub.models.jpa.entities;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@MappedSuperclass
public abstract class BaseEntity extends PanacheEntityBase {

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created")
    public Date created;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated")
    public Date updated;

    @Version
    @Column(name = "version")
    public int version;

    @PrePersist
    public void prePersist() {
        created = new Date();
    }

    @PreUpdate
    public void preUpdate() {
        updated = new Date();
    }

}
