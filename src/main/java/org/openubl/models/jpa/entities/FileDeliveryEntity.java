package org.openubl.models.jpa.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import org.openubl.models.FileDeliveryStatusType;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Cacheable
public class FileDeliveryEntity extends PanacheEntity {

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    public FileDeliveryStatusType status;

    public static final class Builder {
        public FileDeliveryStatusType status;

        private Builder() {
        }

        public static Builder aFileDeliveryEntity() {
            return new Builder();
        }

        public Builder withStatus(FileDeliveryStatusType status) {
            this.status = status;
            return this;
        }

        public FileDeliveryEntity build() {
            FileDeliveryEntity fileDeliveryEntity = new FileDeliveryEntity();
            fileDeliveryEntity.status = this.status;
            return fileDeliveryEntity;
        }
    }
}
