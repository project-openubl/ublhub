package io.github.project.openubl.ublhub.documents;

import com.github.f4b6a3.tsid.TsidFactory;
import io.github.project.openubl.quarkus.xbuilder.XBuilder;
import io.github.project.openubl.ublhub.documents.exceptions.ProjectNotFoundException;
import io.github.project.openubl.ublhub.keys.KeyManager;
import io.github.project.openubl.ublhub.keys.component.ComponentOwner;
import io.github.project.openubl.ublhub.models.jpa.CompanyRepository;
import io.github.project.openubl.ublhub.models.jpa.ProjectRepository;
import io.github.project.openubl.ublhub.models.jpa.UBLDocumentRepository;
import io.github.project.openubl.ublhub.models.jpa.entities.*;
import io.github.project.openubl.ublhub.documents.exceptions.NoCertificateToSignFoundException;
import io.github.project.openubl.xbuilder.content.models.standard.general.CreditNote;
import io.github.project.openubl.xbuilder.content.models.standard.general.DebitNote;
import io.github.project.openubl.xbuilder.content.models.standard.general.Invoice;
import io.github.project.openubl.xbuilder.content.models.standard.guia.DespatchAdvice;
import io.github.project.openubl.xbuilder.content.models.sunat.baja.VoidedDocuments;
import io.github.project.openubl.xbuilder.content.models.sunat.percepcionretencion.Perception;
import io.github.project.openubl.xbuilder.content.models.sunat.percepcionretencion.Retention;
import io.github.project.openubl.xbuilder.content.models.sunat.resumen.SummaryDocuments;
import io.github.project.openubl.xbuilder.enricher.ContentEnricher;
import io.github.project.openubl.xbuilder.signature.XMLSigner;
import io.github.project.openubl.xsender.files.xml.XmlContent;
import io.github.project.openubl.xsender.files.xml.XmlContentProvider;
import io.github.project.openubl.xsender.models.SunatResponse;
import io.quarkus.qute.Template;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.common.annotation.Blocking;
import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.keycloak.crypto.Algorithm;
import org.keycloak.crypto.KeyUse;
import org.keycloak.crypto.KeyWrapper;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;

@ApplicationScoped
@Named("documentBean")
@RegisterForReflection
public class DocumentBean {

    @Inject
    TsidFactory tsidFactory;

    @Inject
    XBuilder xBuilder;

    @Inject
    KeyManager keystore;

    @Inject
    ProjectRepository projectRepository;

    @Inject
    CompanyRepository companyRepository;

    @Inject
    UBLDocumentRepository documentRepository;

    public void validateProject(
            @Header(DocumentRoute.DOCUMENT_PROJECT) Long projectId,
            Exchange exchange
    ) throws ProjectNotFoundException {
        ProjectEntity projectEntity = projectRepository.findById(projectId);
        if (projectEntity == null) {
            throw new ProjectNotFoundException(projectId);
        }
    }

    public void enrich(Object input) {
        ContentEnricher enricher = new ContentEnricher(xBuilder.getDefaults(), LocalDate::now);
        if (input instanceof Invoice) {
            enricher.enrich((Invoice) input);
        } else if (input instanceof CreditNote) {
            enricher.enrich((CreditNote) input);
        } else if (input instanceof DebitNote) {
            enricher.enrich((DebitNote) input);
        } else if (input instanceof VoidedDocuments) {
            enricher.enrich((VoidedDocuments) input);
        } else if (input instanceof SummaryDocuments) {
            enricher.enrich((SummaryDocuments) input);
        } else if (input instanceof Perception) {
            enricher.enrich((Perception) input);
        } else if (input instanceof Retention) {
            enricher.enrich((Retention) input);
        } else if (input instanceof DespatchAdvice) {
            enricher.enrich((DespatchAdvice) input);
        } else {
            throw new IllegalStateException("Not supported class for enriching");
        }
    }

    public String render(Object input) {
        Template template = null;
        if (input instanceof Invoice) {
            template = xBuilder.getTemplate(XBuilder.Type.INVOICE);
        } else if (input instanceof CreditNote) {
            template = xBuilder.getTemplate(XBuilder.Type.CREDIT_NOTE);
        } else if (input instanceof DebitNote) {
            template = xBuilder.getTemplate(XBuilder.Type.DEBIT_NOTE);
        } else if (input instanceof VoidedDocuments) {
            template = xBuilder.getTemplate(XBuilder.Type.VOIDED_DOCUMENTS);
        } else if (input instanceof SummaryDocuments) {
            template = xBuilder.getTemplate(XBuilder.Type.SUMMARY_DOCUMENTS);
        } else if (input instanceof Perception) {
            template = xBuilder.getTemplate(XBuilder.Type.PERCEPTION);
        } else if (input instanceof Retention) {
            template = xBuilder.getTemplate(XBuilder.Type.RETENTION);
        } else if (input instanceof DespatchAdvice) {
            template = xBuilder.getTemplate(XBuilder.Type.DESPATCH_ADVICE);
        } else {
            throw new IllegalStateException("Not supported class for rendering");
        }

        return template.data(input).render();
    }

    public void sign(
            @Header(DocumentRoute.DOCUMENT_PROJECT) Long projectId,
            @Header(DocumentRoute.DOCUMENT_RUC) String ruc,
            @Body String body,
            Exchange exchange
    ) throws NoCertificateToSignFoundException, MarshalException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IOException, ParserConfigurationException, XMLSignatureException, SAXException {
        String algorithm = Algorithm.RS256;

        KeyWrapper keyWrapper = null;

        CompanyEntity companyEntity = companyRepository.findByRuc(projectId, ruc);
        if (companyEntity != null) {
            ComponentOwner companyOwner = ComponentOwner.builder()
                    .id(companyEntity.getId())
                    .type(ComponentOwner.OwnerType.company)
                    .build();
            keyWrapper = keystore.getActiveKeyWithoutFallback(companyOwner, KeyUse.SIG, algorithm);
        }

        if (keyWrapper == null) {
            ComponentOwner projectOwner = ComponentOwner.builder()
                    .id(projectId)
                    .type(ComponentOwner.OwnerType.project)
                    .build();
            keyWrapper = keystore.getActiveKeyWithoutFallback(projectOwner, KeyUse.SIG, algorithm);
        }

        if (keyWrapper == null) {
            throw new NoCertificateToSignFoundException();
        }

        KeyManager.ActiveRsaKey rsaKey = KeyManager.ActiveRsaKey.builder()
                .kid(keyWrapper.getKid())
                .privateKey((PrivateKey) keyWrapper.getPrivateKey())
                .publicKey((PublicKey) keyWrapper.getPublicKey())
                .certificate(keyWrapper.getCertificate())
                .build();

        Document signedDocument = XMLSigner.signXML(body, "OPENUBL", rsaKey.getCertificate(), rsaKey.getPrivateKey());
        exchange.getIn().setBody(signedDocument);
    }

    @Transactional
    public void create(
            @Header(DocumentRoute.DOCUMENT_PROJECT) Long projectId,
            @Header(DocumentRoute.DOCUMENT_FILE_ID) String documentFileId,
            Exchange exchange
    ) {
        UBLDocumentEntity documentEntity = new UBLDocumentEntity();
        documentEntity.setId(tsidFactory.create().toLong());
        documentEntity.setXmlFileId(documentFileId);
        documentEntity.setProjectId(projectId);
        documentEntity.setJobInProgress(true);
        documentEntity.persist();

        exchange.getIn().setHeader(DocumentRoute.DOCUMENT_ID, documentEntity.getId());
    }

    @Blocking
    @Transactional
    public void fetchDocument(
            @Header(DocumentRoute.DOCUMENT_ID) Long documentId,
            Exchange exchange
    ) {
        UBLDocumentEntity documentEntity = documentRepository.findById(documentId);
        exchange.getIn().setHeader(DocumentRoute.DOCUMENT_PROJECT, documentEntity.getProjectId());
        exchange.getIn().setHeader(DocumentRoute.DOCUMENT_FILE_ID, documentEntity.getXmlFileId());
        exchange.getIn().setHeader(DocumentRoute.DOCUMENT_XML_DATA, documentEntity.getXmlData());

        String ticket = Optional.ofNullable(documentEntity.getSunatResponse())
                .map(SUNATResponseEntity::getTicket)
                .orElse(null);
        exchange.getIn().setHeader(DocumentRoute.SUNAT_TICKET, ticket);
    }

    @Transactional
    public void generateXmlData(
            @Header(DocumentRoute.DOCUMENT_ID) Long documentId,
            @Header(DocumentRoute.DOCUMENT_FILE) byte[] documentFile,
            Exchange exchange
    ) throws ParserConfigurationException, IOException, SAXException {
        XmlContent xmlContent = XmlContentProvider.getSunatDocument(new ByteArrayInputStream(documentFile));

        XMLDataEntity xmlDataEntity = new XMLDataEntity();
        xmlDataEntity.setRuc(xmlContent.getRuc());
        xmlDataEntity.setTipoDocumento(xmlContent.getDocumentType());
        xmlDataEntity.setSerieNumero(xmlContent.getDocumentID());
        xmlDataEntity.setBajaCodigoTipoDocumento(xmlContent.getVoidedLineDocumentTypeCode());

        UBLDocumentEntity documentEntity = documentRepository.findById(documentId);
        documentEntity.setXmlData(xmlDataEntity);
        documentEntity.persist();

        exchange.getIn().setHeader(DocumentRoute.DOCUMENT_XML_DATA, documentEntity.getXmlData());
    }

    @Transactional
    public void getSunatData(
            @Header(DocumentRoute.DOCUMENT_PROJECT) Long projectId,
            @Header(DocumentRoute.DOCUMENT_XML_DATA) XMLDataEntity xmlData,
            Exchange exchange
    ) {
        CompanyEntity companyEntity = companyRepository.findByRuc(projectId, xmlData.getRuc());

        SunatEntity sunatEntity = null;
        if (companyEntity != null) {
            sunatEntity = companyEntity.getSunat();
        }
        if (sunatEntity == null) {
            sunatEntity = projectRepository.findById(projectId).getSunat();
        }

        exchange.getIn().setHeader(DocumentRoute.DOCUMENT_SUNAT_DATA, sunatEntity);
    }

    @Transactional
    public void saveSunatResponse(
            @Header(DocumentRoute.DOCUMENT_ID) Long documentId,
            @Header(DocumentRoute.SUNAT_RESPONSE) SunatResponse sunatResponse,
            Exchange exchange
    ) {
        UBLDocumentEntity documentEntity = documentRepository.findById(documentId);

        if (documentEntity.getSunatResponse() == null) {
            documentEntity.setSunatResponse(new SUNATResponseEntity());
        }

        SUNATResponseEntity sunatResponseEntity = documentEntity.getSunatResponse();
        sunatResponseEntity.setStatus(sunatResponse.getStatus() != null ? sunatResponse.getStatus().toString() : null);
        sunatResponseEntity.setTicket(sunatResponse.getSunat() != null ? sunatResponse.getSunat().getTicket() : null);
        Optional.ofNullable(sunatResponse.getMetadata()).ifPresent(metadata -> {
            sunatResponseEntity.setCode(metadata.getResponseCode());
            sunatResponseEntity.setDescription(metadata.getDescription());
            sunatResponseEntity.setNotes(metadata.getNotes() != null ? new HashSet<>(metadata.getNotes()) : null);
        });

        boolean shouldVerifyTicket = documentEntity.getSunatResponse() != null && documentEntity.getSunatResponse().getTicket() != null;
        documentEntity.setJobInProgress(shouldVerifyTicket);

        documentEntity.persist();
    }

    @Transactional
    public void saveCdr(
            @Header(DocumentRoute.DOCUMENT_ID) Long documentId,
            @Body String cdrFileId,
            Exchange exchange
    ) {
        UBLDocumentEntity documentEntity = documentRepository.findById(documentId);
        documentEntity.setCdrFileId(cdrFileId);
        documentEntity.persist();
    }
}
