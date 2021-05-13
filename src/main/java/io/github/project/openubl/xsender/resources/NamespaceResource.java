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

import io.github.project.openubl.xsender.idm.NamespaceRepresentation;
import io.github.project.openubl.xsender.models.jpa.NamespaceRepository;
import io.github.project.openubl.xsender.models.jpa.entities.NamespaceEntity;
import io.github.project.openubl.xsender.models.utils.EntityToRepresentation;
import io.github.project.openubl.xsender.security.UserIdentity;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;

@Path("/namespaces")
@Produces("application/json")
@Consumes("application/json")
@Transactional
@ApplicationScoped
public class NamespaceResource {

    private static final Logger LOG = Logger.getLogger(NamespaceResource.class);

    @Inject
    UserIdentity userIdentity;

    @Inject
    NamespaceRepository namespaceRepository;

    @GET
    @Path("/{namespaceId}")
    public NamespaceRepresentation getNamespace(@PathParam("namespaceId") @NotNull String namespaceId) {
        NamespaceEntity namespaceEntity = namespaceRepository.findByIdAndOwner(namespaceId, userIdentity.getUsername()).orElseThrow(NotFoundException::new);
        return EntityToRepresentation.toRepresentation(namespaceEntity);
    }

    @PUT
    @Path("/{namespaceId}")
    public NamespaceRepresentation updateNamespace(
            @PathParam("namespaceId") @NotNull String namespaceId,
            @NotNull NamespaceRepresentation rep
    ) {
        NamespaceEntity namespaceEntity = namespaceRepository.findByIdAndOwner(namespaceId, userIdentity.getUsername()).orElseThrow(NotFoundException::new);
        if (rep.getName() != null) {
            namespaceEntity.setName(rep.getName());
        }
        if (rep.getDescription() != null) {
            namespaceEntity.setDescription(rep.getDescription());
        }
        namespaceRepository.persist(namespaceEntity);

        return EntityToRepresentation.toRepresentation(namespaceEntity);
    }

    @DELETE
    @Path("/{namespaceId}")
    public void deleteNamespace(@PathParam("namespaceId") @NotNull String namespaceId) {
        NamespaceEntity namespaceEntity = namespaceRepository.findByIdAndOwner(namespaceId, userIdentity.getUsername()).orElseThrow(NotFoundException::new);
        namespaceRepository.deleteById(namespaceEntity.getId());
    }

}


