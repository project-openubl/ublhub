/*
 * Copyright 2019 Project OpenUBL, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.project.openubl.ublhub.resources;

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

import io.github.project.openubl.ublhub.builder.XMLBuilderManager;
import io.github.project.openubl.ublhub.events.BroadcasterEventManager;
import io.github.project.openubl.ublhub.exceptions.NoNamespaceException;
import io.github.project.openubl.ublhub.files.FilesMutiny;
import io.github.project.openubl.ublhub.idm.input.InputTemplateRepresentation;
import io.github.project.openubl.ublhub.keys.KeyManager;
import io.github.project.openubl.ublhub.models.DocumentFilterModel;
import io.github.project.openubl.ublhub.models.PageBean;
import io.github.project.openubl.ublhub.models.SortBean;
import io.github.project.openubl.ublhub.models.jpa.NamespaceRepository;
import io.github.project.openubl.ublhub.models.jpa.UBLDocumentRepository;
import io.github.project.openubl.ublhub.models.jpa.entities.NamespaceEntity;
import io.github.project.openubl.ublhub.models.jpa.entities.UBLDocumentEntity;
import io.github.project.openubl.ublhub.models.utils.EntityToRepresentation;
import io.github.project.openubl.ublhub.resources.utils.ResourceUtils;
import io.github.project.openubl.ublhub.scheduler.SchedulerManager;
import io.github.project.openubl.xmlbuilderlib.xml.XMLSigner;
import io.github.project.openubl.xmlbuilderlib.xml.XmlSignatureHelper;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.UniAndGroup2;
import io.vertx.core.json.JsonObject;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.MultipartForm;
import org.keycloak.crypto.Algorithm;
import org.keycloak.crypto.KeyUse;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
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
    XMLBuilderManager xmlBuilderManager;

    @Inject
    KeyManager keystore;

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
                .chain(documentEntity -> Panache
                        .withTransaction(() -> {
                            documentEntity.id = UUID.randomUUID().toString();
                            documentEntity.created = new Date();
                            documentEntity.inProgress = true;
                            return documentEntity.<UBLDocumentEntity>persist().map(unused -> documentEntity);
                        })
                )

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
            @NotNull JsonObject jsonObject
    ) {
        InputTemplateRepresentation inputTemplate = jsonObject.mapTo(InputTemplateRepresentation.class);
        JsonObject documentJsonObject = jsonObject.getJsonObject("spec").getJsonObject("document");
        return Panache
                .withTransaction(() -> namespaceRepository.findById(namespaceId)
                        .onItem().ifNotNull().transformToUni(namespaceEntity -> xmlBuilderManager.createXMLString(namespaceEntity, inputTemplate, documentJsonObject, false)
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
                                .map(documentEntity -> Response
                                        .status(Response.Status.CREATED)
                                        .entity(EntityToRepresentation.toRepresentation(documentEntity))
                                        .build()
                                )
                        )

                        .onItem().ifNull().continueWith(Response.ok()
                                .status(Response.Status.NOT_FOUND)::build
                        )

                        .onFailure(throwable -> throwable instanceof ConstraintViolationException).recoverWithItem(throwable -> Response
                                .status(Response.Status.BAD_REQUEST)
                                .entity(throwable.getMessage()).build()
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
                .withTransaction(() -> namespaceRepository.findById(namespaceId))
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
            @QueryParam("sort_by") @DefaultValue("created:desc") List<String> sortBy
    ) {
        PageBean pageBean = ResourceUtils.getPageBean(offset, limit);
        List<SortBean> sortBeans = ResourceUtils.getSortBeans(sortBy, UBLDocumentRepository.SORT_BY_FIELDS);

        DocumentFilterModel filters = DocumentFilterModel.DocumentFilterModelBuilder.aDocumentFilterModel()
                .withRuc(ruc)
                .withDocumentType(documentType)
                .build();

        return Panache
                .withTransaction(() -> namespaceRepository.findById(namespaceId)
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
                .withTransaction(() -> namespaceRepository.findById(namespaceId)
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

}


