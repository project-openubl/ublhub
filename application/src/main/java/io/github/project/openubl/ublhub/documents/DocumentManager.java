package io.github.project.openubl.ublhub.documents;

import io.github.project.openubl.ublhub.documents.exceptions.CouldNotCreateXMLException;
import io.github.project.openubl.ublhub.documents.exceptions.ProjectNotFoundException;
import io.github.project.openubl.ublhub.dto.DocumentInputDto;
import io.github.project.openubl.ublhub.models.jpa.ProjectRepository;
import io.github.project.openubl.ublhub.models.jpa.entities.ProjectEntity;
import io.github.project.openubl.ublhub.models.jpa.entities.UBLDocumentEntity;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.apache.camel.ProducerTemplate;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;

@Named("documentManager")
@Transactional
@ApplicationScoped
@RegisterForReflection
public class DocumentManager {

    @Inject
    ProjectRepository projectRepository;

    @Inject
    ProducerTemplate producerTemplate;

    public UBLDocumentEntity createDocument(DocumentInputDto input)
            throws ProjectNotFoundException, CouldNotCreateXMLException {
        Long projectId = input.getMetadata().getProject();
        ProjectEntity projectEntity = projectRepository.findById(projectId);
        if (projectEntity == null) {
            throw new ProjectNotFoundException(projectId);
        }

//        XMLResult xml = (XMLResult) producerTemplate.requestBody("direct:unmarshalUBLJson", inpublhubut.getSpec().getDocument());
        return null;
    }

}
