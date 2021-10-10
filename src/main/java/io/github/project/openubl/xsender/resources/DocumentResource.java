package io.github.project.openubl.xsender.resources;

/*
 * Copyright 2019 Project OpenUBL, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Eclipse Public License - v 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import io.github.project.openubl.xmlbuilderlib.facade.DocumentManager;
import io.github.project.openubl.xmlbuilderlib.models.input.standard.invoice.InvoiceInputModel;
import io.github.project.openubl.xmlbuilderlib.models.input.standard.note.creditNote.CreditNoteInputModel;
import io.github.project.openubl.xmlbuilderlib.models.input.standard.note.debitNote.DebitNoteInputModel;
import io.github.project.openubl.xmlbuilderlib.xml.XMLSigner;
import io.github.project.openubl.xmlbuilderlib.xml.XmlSignatureHelper;
import io.github.project.openubl.xsender.builder.UblHubXBuilderClock;
import io.github.project.openubl.xsender.builder.UblHubXBuilderConfig;
import io.github.project.openubl.xsender.events.BroadcasterEventManager;
import io.github.project.openubl.xsender.exceptions.NoNamespaceException;
import io.github.project.openubl.xsender.files.FilesMutiny;
import io.github.project.openubl.xsender.idgenerator.IDGenerator;
import io.github.project.openubl.xsender.idgenerator.IGGeneratorManager;
import io.github.project.openubl.xsender.idm.input.*;
import io.github.project.openubl.xsender.keys.KeyManager;
import io.github.project.openubl.xsender.models.DocumentFilterModel;
import io.github.project.openubl.xsender.models.PageBean;
import io.github.project.openubl.xsender.models.SortBean;
import io.github.project.openubl.xsender.models.jpa.NamespaceRepository;
import io.github.project.openubl.xsender.models.jpa.UBLDocumentRepository;
import io.github.project.openubl.xsender.models.jpa.entities.NamespaceEntity;
import io.github.project.openubl.xsender.models.jpa.entities.UBLDocumentEntity;
import io.github.project.openubl.xsender.models.utils.EntityToRepresentation;
import io.github.project.openubl.xsender.resources.utils.ResourceUtils;
import io.github.project.openubl.xsender.scheduler.SchedulerManager;
import io.github.project.openubl.xsender.security.UserIdentity;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.UniAndGroup2;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.MultipartForm;
import org.keycloak.crypto.Algorithm;
import org.keycloak.crypto.KeyUse;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Path("/namespaces")
@Produces("application/json")
@Consumes("application/json")
@ApplicationScoped
public class DocumentResource {

    private static final Logger LOG = Logger.getLogger(DocumentResource.class);

    @Inject
    UserIdentity userIdentity;

    @Inject
    FilesMutiny filesMutiny;

    @Inject
    SchedulerManager schedulerManager;

    // Needed to not remove BroadcasterEventManager at build-time
    // https://github.com/quarkusio/quarkus/issues/6948
    @Inject
    BroadcasterEventManager broadcasterEventManager;

    @Inject
    NamespaceRepository namespaceRepository;

    @Inject
    UBLDocumentRepository documentRepository;

    @Inject
    IGGeneratorManager igGeneratorManager;

    @Inject
    UblHubXBuilderConfig xBuilderConfig;

    @Inject
    UblHubXBuilderClock xBuilderClock;

    @Inject
    KeyManager keystore;

    public String createXMLString(InputTemplateRepresentation inputTemplate) {
        KindRepresentation kind = inputTemplate.getKind();
        SpecRepresentation spec = inputTemplate.getSpec();

        IDGenerator idGenerator = igGeneratorManager.selectIDGenerator(spec.getIdGenerator().getName());
        switch (kind) {
            case Invoice:
                InvoiceInputModel invoice = inputTemplate.getSpec().getDocument().mapTo(InvoiceInputModel.class);
                idGenerator.enrichWithID(invoice, inputTemplate.getSpec().getIdGenerator().getConfig());

                return DocumentManager.createXML(invoice, xBuilderConfig, xBuilderClock).getXml();
            case CreditNote:
                CreditNoteInputModel creditNote = inputTemplate.getSpec().getDocument().mapTo(CreditNoteInputModel.class);
                idGenerator.enrichWithID(creditNote, inputTemplate.getSpec().getIdGenerator().getConfig());

                return DocumentManager.createXML(creditNote, xBuilderConfig, xBuilderClock).getXml();
            case DebitNote:
                DebitNoteInputModel debitNote = inputTemplate.getSpec().getDocument().mapTo(DebitNoteInputModel.class);
                idGenerator.enrichWithID(debitNote, inputTemplate.getSpec().getIdGenerator().getConfig());

                return DocumentManager.createXML(debitNote, xBuilderConfig, xBuilderClock).getXml();
            default:
                throw new IllegalStateException("Kind:" + kind + " not supported");
        }
    }

    public Uni<UBLDocumentEntity> createDocumentFromFileID(NamespaceEntity namespaceEntity, String fileSavedId) {
        // Wait for file to be saved
        return Uni.createFrom().item(fileSavedId)
                .map(fileID -> {
                    UBLDocumentEntity documentEntity = new UBLDocumentEntity();
                    documentEntity.storageFile = fileID;
                    documentEntity.namespace = namespaceEntity;
                    return documentEntity;
                })

                // Save entity
                .chain(documentEntity -> {
                    documentEntity.id = UUID.randomUUID().toString();
                    documentEntity.createdOn = new Date();
                    documentEntity.inProgress = true;
                    return documentEntity.<UBLDocumentEntity>persistAndFlush().map(unused -> documentEntity);
                })
                .chain(documentEntity -> Panache.flush().map(unused -> documentEntity))

                // Events
                .chain(documentEntity -> schedulerManager
                        .sendDocumentToSUNAT(documentEntity.id)
                        .map(unused -> documentEntity)
                );
    }


    @POST
    @Path("/{namespaceId}/documents")
    public Uni<Response> createDocument(
            @PathParam("namespaceId") @NotNull String namespaceId,
            @NotNull @Valid InputTemplateRepresentation inputTemplate
    ) {
        return Panache
                .withTransaction(() -> namespaceRepository.findByIdAndOwner(namespaceId, userIdentity.getUsername())
                        .onItem().ifNotNull().transformToUni(namespaceEntity -> Uni.createFrom()
                                .item(createXMLString(inputTemplate))
                                .chain(xmlString -> keystore
                                        .getActiveKey(namespaceEntity, KeyUse.SIG, inputTemplate.getSpec().getSignature() != null ? inputTemplate.getSpec().getSignature().getAlgorithm() : Algorithm.RS256)
                                        .map(key -> {
                                            KeyManager.ActiveRsaKey activeRsaKey = new KeyManager.ActiveRsaKey(key.getKid(), (PrivateKey) key.getPrivateKey(), (PublicKey) key.getPublicKey(), key.getCertificate());
                                            try {
                                                return XMLSigner.signXML(xmlString, "OPENUBL", activeRsaKey.getCertificate(), activeRsaKey.getPrivateKey());
                                            } catch (Exception e) {
                                                throw new IllegalStateException(e);
                                            }
                                        })
                                )

                                // Save file
                                .chain(document -> {
                                    try {
                                        byte[] bytes = XmlSignatureHelper.getBytesFromDocument(document);
                                        return filesMutiny.createFile(bytes, true);
                                    } catch (Exception e) {
                                        throw new IllegalStateException(e);
                                    }
                                })

                                // Link the file Entity
                                .chain(fileSavedId -> createDocumentFromFileID(namespaceEntity, fileSavedId))

                                // Response
                                .map(documentEntity -> {
                                            System.out.println(documentEntity);
                                            return Response
                                                    .status(Response.Status.CREATED)
                                                    .entity(EntityToRepresentation.toRepresentation(documentEntity))
                                                    .build();
                                        }
                                )
                        )

                        .onItem().ifNull().continueWith(Response.ok()
                                .status(Response.Status.NOT_FOUND)::build
                        )
                );
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/{namespaceId}/documents/upload")
    public Uni<Response> uploadXML(
            @PathParam("namespaceId") @NotNull String namespaceId,
            @MultipartForm FormData formData
    ) {
        return Panache
                // Verify namespace
                .withTransaction(() -> namespaceRepository.findByIdAndOwner(namespaceId, userIdentity.getUsername()))
                .onItem().ifNull().failWith(NoNamespaceException::new)

                .chain(namespaceEntity -> filesMutiny
                        // Save file
                        .createFile(formData.file.uploadedFile().toFile(), true)

                        // Link the file Entity
                        .chain(fileSavedId -> createDocumentFromFileID(namespaceEntity, fileSavedId))
                )

                .map(documentEntity -> Response.ok()
                        .entity(EntityToRepresentation.toRepresentation(documentEntity))
                        .build()
                )
                .onFailure(throwable -> throwable instanceof NoNamespaceException).recoverWithItem(Response.status(Response.Status.NOT_FOUND).build())
                .onFailure(throwable -> !(throwable instanceof NoNamespaceException)).recoverWithItem(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
    }

    @GET
    @Path("/{namespaceId}/documents")
    public Uni<Response> getDocuments(
            @PathParam("namespaceId") @NotNull String namespaceId,
            @QueryParam("ruc") List<String> ruc,
            @QueryParam("documentType") List<String> documentType,
            @QueryParam("filterText") String filterText,
            @QueryParam("offset") @DefaultValue("0") Integer offset,
            @QueryParam("limit") @DefaultValue("10") Integer limit,
            @QueryParam("sort_by") @DefaultValue("createdOn:desc") List<String> sortBy
    ) {
        PageBean pageBean = ResourceUtils.getPageBean(offset, limit);
        List<SortBean> sortBeans = ResourceUtils.getSortBeans(sortBy, UBLDocumentRepository.SORT_BY_FIELDS);

        DocumentFilterModel filters = DocumentFilterModel.DocumentFilterModelBuilder.aDocumentFilterModel()
                .withRuc(ruc)
                .withDocumentType(documentType)
                .build();

        return Panache
                .withTransaction(() -> namespaceRepository.findByIdAndOwner(namespaceId, userIdentity.getUsername())
                        .onItem().ifNotNull().transformToUni(namespaceEntity -> {
                            UniAndGroup2<List<UBLDocumentEntity>, Long> searchResult;
                            if (filterText != null && !filterText.trim().isEmpty()) {
                                searchResult = documentRepository.list(namespaceEntity, filterText, filters, pageBean, sortBeans);
                            } else {
                                searchResult = documentRepository.list(namespaceEntity, filters, pageBean, sortBeans);
                            }
                            return searchResult.asTuple();
                        })
                )
                .onItem().ifNotNull().transform(tuple2 -> Response.ok()
                        .entity(EntityToRepresentation.toRepresentation(
                                tuple2.getItem1(),
                                tuple2.getItem2(),
                                EntityToRepresentation::toRepresentation
                        ))
                        .build()
                )
                .onItem().ifNull().continueWith(Response.status(Response.Status.NOT_FOUND)::build);
    }

    @GET
    @Path("/{namespaceId}/documents/{documentId}")
    public Uni<Response> getDocument(
            @PathParam("namespaceId") @NotNull String namespaceId,
            @PathParam("documentId") @NotNull String documentId
    ) {
        return Panache
                .withTransaction(() -> namespaceRepository.findByIdAndOwner(namespaceId, userIdentity.getUsername())
                        .onItem().ifNotNull().transformToUni(namespaceEntity -> documentRepository.findById(namespaceEntity, documentId))
                )
                .onItem().ifNotNull().transform(documentEntity -> Response.ok()
                        .entity(EntityToRepresentation.toRepresentation(documentEntity))
                        .build()
                )
                .onItem().ifNull().continueWith(Response.status(Response.Status.NOT_FOUND)::build);
    }

//    @Transactional(Transactional.TxType.NOT_SUPPORTED)
//    @POST
//    @Path("/{documentId}/retry-send")
//    public Response resend(
//            @PathParam("namespaceId") @NotNull String namespaceId,
//            @PathParam("documentId") @NotNull String documentId
//    ) {
//        try {
//            transaction.begin();
//
//            NamespaceEntity namespaceEntity = namespaceRepository.findByIdAndOwner(namespaceId, userIdentity.getUsername()).orElseThrow(NotFoundException::new);
//            UBLDocumentEntity documentEntity = documentRepository.findById(namespaceEntity, documentId).orElseThrow(NotFoundException::new);
//
//            documentEntity.setInProgress(true);
//            documentEntity.setError(null);
//            documentEntity.setScheduledDelivery(null);
//
//            // Commit transaction
//            transaction.commit();
//        } catch (NotSupportedException | SystemException | HeuristicRollbackException | HeuristicMixedException | RollbackException e) {
//            LOG.error(e);
//            try {
//                transaction.rollback();
//                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
//            } catch (SystemException systemException) {
//                LOG.error(systemException);
//                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
//            }
//        }
//
//        // Event: new document has been created
//        documentEventManager.fire(new DocumentEvent(documentId, namespaceId));
//
//        // Send file
//        Message<String> message = Message.of(documentId)
//                .withNack(throwable -> messageSenderManager.handleDocumentMessageError(documentId, throwable));
//        messageSenderManager.sendToDocumentQueue(message);
//
//        return Response.status(Response.Status.OK)
//                .build();
//    }

//    @GET
//    @Path("/{documentId}/file")
//    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_OCTET_STREAM})
//    public Response getDocumentFile(
//            @PathParam("namespaceId") @NotNull String namespaceId,
//            @PathParam("documentId") @NotNull String documentId
//    ) {
//        NamespaceEntity namespaceEntity = namespaceRepository.findByIdAndOwner(namespaceId, userIdentity.getUsername()).orElseThrow(NotFoundException::new);
//        UBLDocumentEntity documentEntity = documentRepository.findById(namespaceEntity, documentId).orElseThrow(NotFoundException::new);
//
//        byte[] file = filesManager.getFileAsBytesAfterUnzip(documentEntity.getStorageFile());
//        return Response.ok(file, MediaType.APPLICATION_XML)
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + documentEntity.getDocumentID() + ".xml" + "\"")
//                .build();
//    }
//
//    @GET
//    @Path("/{documentId}/file-link")
//    @Produces(MediaType.TEXT_PLAIN)
//    public Response getDocumentFileLink(
//            @PathParam("namespaceId") @NotNull String namespaceId,
//            @PathParam("documentId") @NotNull String documentId
//    ) {
//        NamespaceEntity namespaceEntity = namespaceRepository.findByIdAndOwner(namespaceId, userIdentity.getUsername()).orElseThrow(NotFoundException::new);
//        UBLDocumentEntity documentEntity = documentRepository.findById(namespaceEntity, documentId).orElseThrow(NotFoundException::new);
//
//        String fileLink = filesManager.getFileLink(documentEntity.getStorageFile());
//        return Response.ok(fileLink)
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + documentEntity.getDocumentID() + ".xml" + "\"")
//                .build();
//    }
//
//    @GET
//    @Path("/{documentId}/cdr")
//    @Produces(MediaType.APPLICATION_OCTET_STREAM)
//    public Response getDocumentCdr(
//            @PathParam("namespaceId") @NotNull String namespaceId,
//            @PathParam("documentId") @NotNull String documentId
//    ) {
//        NamespaceEntity namespaceEntity = namespaceRepository.findByIdAndOwner(namespaceId, userIdentity.getUsername()).orElseThrow(NotFoundException::new);
//        UBLDocumentEntity documentEntity = documentRepository.findById(namespaceEntity, documentId).orElseThrow(NotFoundException::new);
//
//        if (documentEntity.getStorageCdr() == null) {
//            return Response.status(Response.Status.NOT_FOUND).build();
//        }
//
//        byte[] file = filesManager.getFileAsBytesWithoutUnzipping(documentEntity.getStorageCdr());
//        return Response.ok(file, "application/zip")
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + documentEntity.getDocumentID() + ".zip" + "\"")
//                .build();
//    }
//
//    @GET
//    @Path("/{documentId}/cdr-link")
//    @Produces(MediaType.TEXT_PLAIN)
//    public Response getDocumentCdrLink(
//            @PathParam("namespaceId") @NotNull String namespaceId,
//            @PathParam("documentId") @NotNull String documentId
//    ) {
//        NamespaceEntity namespaceEntity = namespaceRepository.findByIdAndOwner(namespaceId, userIdentity.getUsername()).orElseThrow(NotFoundException::new);
//        UBLDocumentEntity documentEntity = documentRepository.findById(namespaceEntity, documentId).orElseThrow(NotFoundException::new);
//
//        if (documentEntity.getStorageCdr() == null) {
//            return Response.status(Response.Status.NOT_FOUND).build();
//        }
//
//        String fileLink = filesManager.getFileLink(documentEntity.getStorageCdr());
//        return Response.ok(fileLink)
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + documentEntity.getDocumentID() + ".zip" + "\"")
//                .build();
//    }

}


