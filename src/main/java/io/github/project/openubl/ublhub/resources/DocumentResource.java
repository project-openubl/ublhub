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

import io.github.project.openubl.ublhub.files.FilesMutiny;
import io.github.project.openubl.ublhub.keys.KeyManager;
import io.github.project.openubl.ublhub.models.DocumentFilterModel;
import io.github.project.openubl.ublhub.models.PageBean;
import io.github.project.openubl.ublhub.models.SortBean;
import io.github.project.openubl.ublhub.models.jpa.ProjectRepository;
import io.github.project.openubl.ublhub.models.jpa.UBLDocumentRepository;
import io.github.project.openubl.ublhub.models.jpa.entities.ProjectEntity;
import io.github.project.openubl.ublhub.models.jpa.entities.UBLDocumentEntity;
import io.github.project.openubl.ublhub.models.utils.EntityToRepresentation;
import io.github.project.openubl.ublhub.resources.utils.ResourceUtils;
import io.github.project.openubl.ublhub.resources.validation.JSONValidatorManager;
import io.github.project.openubl.ublhub.scheduler.SchedulerManager;
import io.github.project.openubl.ublhub.ubl.builder.xmlgenerator.XMLGeneratorManager;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.UniAndGroup2;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.MultipartForm;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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

    @Inject
    ProjectRepository namespaceRepository;

    @Inject
    UBLDocumentRepository documentRepository;

    @Inject
    XMLGeneratorManager xmlGeneratorManager;

    @Inject
    KeyManager keystore;

    @Inject
    JSONValidatorManager jsonManager;

    public Uni<UBLDocumentEntity> createAndScheduleSend(ProjectEntity projectEntity, String fileSavedId) {
        // Wait for file to be saved
        return Uni.createFrom().item(fileSavedId)
                .chain(xmlFileId -> {
                    UBLDocumentEntity documentEntity = new UBLDocumentEntity();
                    documentEntity.id = UUID.randomUUID().toString();
                    documentEntity.xmlFileId = xmlFileId;
                    documentEntity.project = projectEntity;
                    return documentEntity.<UBLDocumentEntity>persist();
                })

                // Schedule send
                .chain(documentEntity -> schedulerManager
                        .sendDocumentToSUNAT(documentEntity)
                        .map(unused -> documentEntity)
                );
    }

//    @POST
//    @Path("/{namespaceId}/documents")
//    public Uni<Response> createDocument(
//            @PathParam("namespaceId") @NotNull String namespaceId,
//            @NotNull JsonObject json
//    ) {
//        return Panache
//                .withTransaction(() -> namespaceRepository.findById(namespaceId)
//                        .onItem().ifNotNull().transformToUni(namespaceEntity -> jsonManager
//                                .getUniInputTemplateFromJSON(json)
//                                .onItem().ifNotNull().transformToUni(input -> xmlGeneratorManager
//                                        .createXMLString(namespaceEntity, input)
//                                        .chain(xmlString -> keystore
//                                                .getActiveKey(namespaceEntity, KeyUse.SIG, input.getSpec().getSignature() != null ? input.getSpec().getSignature().getAlgorithm() : Algorithm.RS256)
//                                                .map(key -> {
//                                                    KeyManager.ActiveRsaKey activeRsaKey = new KeyManager.ActiveRsaKey(key.getKid(), (PrivateKey) key.getPrivateKey(), (PublicKey) key.getPublicKey(), key.getCertificate());
//                                                    try {
//                                                        return XMLSigner.signXML(xmlString, "OPENUBL", activeRsaKey.getCertificate(), activeRsaKey.getPrivateKey());
//                                                    } catch (Exception e) {
//                                                        throw new IllegalStateException(e);
//                                                    }
//                                                })
//
//                                                // Save file
//                                                .chain(xmlDocument -> {
////                                                    try {
////                                                        byte[] bytes = XmlSignatureHelper.getBytesFromDocument(xmlDocument);
////                                                        return filesMutiny.createFile(bytes, true);
////                                                    } catch (Exception e) {
////                                                        throw new IllegalStateException(e);
////                                                    }
//                                                    return Uni.createFrom().item("");
//                                                })
//
//                                                // Link the file Entity
//                                                .chain(fileSavedId -> createAndScheduleSend(namespaceEntity, fileSavedId))
//
//                                                // Response
//                                                .map(documentEntity -> Response
//                                                        .status(Response.Status.CREATED)
//                                                        .entity(EntityToRepresentation.toRepresentation(documentEntity))
//                                                        .build()
//                                                )
//                                        )
//                                )
//                                .onItem().ifNull().continueWith(Response.ok().status(Response.Status.BAD_REQUEST)::build)
//                        )
//                        .onItem().ifNull().continueWith(Response.ok()
//                                .status(Response.Status.NOT_FOUND)::build
//                        )
//                );
//    }

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
                .onItem().ifNotNull().transformToUni(namespaceEntity -> filesMutiny
                        .createFile(formData.file.uploadedFile().toFile(), true)
                        .chain(fileSavedId -> createAndScheduleSend(namespaceEntity, fileSavedId))
                        .map(documentEntity -> Response.ok()
                                .entity(EntityToRepresentation.toRepresentation(documentEntity))
                                .build()
                        )
                )
                .onItem().ifNull().continueWith(Response.status(Response.Status.NOT_FOUND)::build);
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
                .withTransaction(() -> documentRepository.findById(namespaceId, documentId))
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


