package io.github.project.openubl.xsender.models.jpa.entities.listeners;

import io.github.project.openubl.xsender.models.jpa.CompanyRepository;
import io.github.project.openubl.xsender.models.jpa.entities.UBLDocumentEntity;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.CDI;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

public class UBLDocumentEntityListener {

    @PostPersist
    @PostUpdate
    @PostRemove
    private void afterAnyUpdate(UBLDocumentEntity document) {
        CompanyRepository companyRepository = CDI.current().select(CompanyRepository.class).get();
    }

}
