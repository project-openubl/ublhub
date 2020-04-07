package org.openubl.models.jpa;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import org.openubl.models.jpa.entities.FileDeliveryEntity;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FileDeliveryRepository implements PanacheRepository<FileDeliveryEntity> {
}
