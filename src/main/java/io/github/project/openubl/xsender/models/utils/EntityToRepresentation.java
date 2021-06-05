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
package io.github.project.openubl.xsender.models.utils;

import io.github.project.openubl.xsender.idm.*;
import io.github.project.openubl.xsender.keys.component.ComponentModel;
import io.github.project.openubl.xsender.keys.component.utils.ComponentUtil;
import io.github.project.openubl.xsender.keys.provider.ProviderConfigProperty;
import io.github.project.openubl.xsender.keys.utils.StripSecretsUtils;
import io.github.project.openubl.xsender.models.PageModel;
import io.github.project.openubl.xsender.models.jpa.entities.CompanyEntity;
import io.github.project.openubl.xsender.models.jpa.entities.NamespaceEntity;
import io.github.project.openubl.xsender.models.jpa.entities.UBLDocumentEntity;
import org.apache.http.client.utils.URIBuilder;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.representations.idm.ComponentRepresentation;
import org.keycloak.representations.idm.ConfigPropertyRepresentation;

import javax.ws.rs.core.UriInfo;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EntityToRepresentation {
    private EntityToRepresentation() {
        // Just static methods
    }

    public static NamespaceRepresentation toRepresentation(NamespaceEntity entity) {
        NamespaceRepresentation rep = new NamespaceRepresentation();

        rep.setId(entity.getId());
        rep.setName(entity.getName());
        rep.setDescription(entity.getDescription());

        return rep;
    }

    public static CompanyRepresentation toRepresentation(CompanyEntity entity) {
        CompanyRepresentation rep = new CompanyRepresentation();

        rep.setId(entity.getId());
        rep.setRuc(entity.getRuc());
        rep.setName(entity.getName());
        rep.setDescription(entity.getDescription());

        if (entity.getSunatUrls() != null) {
            SunatUrlsRepresentation sunatUrlsRep = new SunatUrlsRepresentation();
            rep.setWebServices(sunatUrlsRep);

            sunatUrlsRep.setFactura(entity.getSunatUrls().getSunatUrlFactura());
            sunatUrlsRep.setGuia(entity.getSunatUrls().getSunatUrlGuiaRemision());
            sunatUrlsRep.setRetenciones(entity.getSunatUrls().getSunatUrlPercepcionRetencion());
        }

        if (entity.getSunatCredentials() != null) {
            SunatCredentialsRepresentation credentialsRep = new SunatCredentialsRepresentation();
            rep.setCredentials(credentialsRep);

            credentialsRep.setUsername(entity.getSunatCredentials().getSunatUsername());
        }

        return rep;
    }

    public static DocumentRepresentation toRepresentation(UBLDocumentEntity entity) {
        DocumentRepresentation rep = new DocumentRepresentation();

        rep.setId(entity.getId());
        rep.setInProgress(entity.isInProgress());

        rep.setCreatedOn(entity.getCreatedOn().getTime());
        rep.setError(entity.getError() != null ? entity.getError().getMessage() : null);
        rep.setScheduledDelivery(entity.getScheduledDelivery() != null ? entity.getScheduledDelivery().getTime() : null);
        rep.setRetryCount(entity.getRetries());

        // File

        rep.setFileContentValid(entity.getFileValid());

        rep.setFileContent(new FileContentRepresentation());
        rep.getFileContent().setRuc(entity.getRuc());
        rep.getFileContent().setDocumentID(entity.getDocumentID());
        rep.getFileContent().setDocumentType(entity.getDocumentType());

        // Sunat

        rep.setSunat(new SunatStatusRepresentation());

        rep.getSunat().setCode(entity.getSunatCode());
        rep.getSunat().setTicket(entity.getSunatTicket());
        rep.getSunat().setStatus(entity.getSunatStatus());
        rep.getSunat().setDescription(entity.getSunatDescription());
        rep.getSunat().setHasCdr(entity.getStorageCdr() != null);

        // Events

//        List<DocumentSunatEventRepresentation> eventsRepresentation = entity.getSunatEvents().stream().map(f -> {
//            DocumentSunatEventRepresentation e = new DocumentSunatEventRepresentation();
//            e.setDescription(f.getDescription());
//            e.setStatus(f.getStatus().toString());
//            e.setCreatedOn(f.getCreatedOn().getTime());
//            return e;
//        }).collect(Collectors.toList());
//
//        rep.setSunatEvents(eventsRepresentation);

        return rep;
    }

    public static ComponentRepresentation toRepresentation(ComponentModel component, boolean internal, ComponentUtil componentUtil) {
        ComponentRepresentation rep = toRepresentationWithoutConfig(component);
        if (!internal) {
            rep = StripSecretsUtils.strip(componentUtil, rep);
        }
        return rep;
    }

    public static ComponentRepresentation toRepresentationWithoutConfig(ComponentModel component) {
        org.keycloak.representations.idm.ComponentRepresentation rep = new org.keycloak.representations.idm.ComponentRepresentation();
        rep.setId(component.getId());
        rep.setName(component.getName());
        rep.setProviderId(component.getProviderId());
        rep.setProviderType(component.getProviderType());
        rep.setSubType(component.getSubType());
        rep.setParentId(component.getParentId());
        rep.setConfig(new MultivaluedHashMap<>(component.getConfig()));
        return rep;
    }

    public static List<ConfigPropertyRepresentation> toRepresentation(List<ProviderConfigProperty> configProperties) {
        List<org.keycloak.representations.idm.ConfigPropertyRepresentation> propertiesRep = new LinkedList<>();
        for (ProviderConfigProperty prop : configProperties) {
            ConfigPropertyRepresentation propRep = toRepresentation(prop);
            propertiesRep.add(propRep);
        }
        return propertiesRep;
    }

    public static ConfigPropertyRepresentation toRepresentation(ProviderConfigProperty prop) {
        ConfigPropertyRepresentation propRep = new ConfigPropertyRepresentation();
        propRep.setName(prop.getName());
        propRep.setLabel(prop.getLabel());
        propRep.setType(prop.getType());
        propRep.setDefaultValue(prop.getDefaultValue());
        propRep.setOptions(prop.getOptions());
        propRep.setHelpText(prop.getHelpText());
        propRep.setSecret(prop.isSecret());
        return propRep;
    }

    public static <T, R> PageRepresentation<R> toRepresentation(PageModel<T> model, Function<T, R> mapper) {
        PageRepresentation<R> rep = new PageRepresentation<>();

        // Meta
        PageRepresentation.Meta repMeta = new PageRepresentation.Meta();
        rep.setMeta(repMeta);

        repMeta.setCount(model.getTotalElements());

        // Data
        rep.setData(model.getPageElements().stream()
                .map(mapper)
                .collect(Collectors.toList())
        );

        return rep;
    }

    private static URIBuilder getURIBuilder(UriInfo uriInfo) throws URISyntaxException {
        return new URIBuilder(uriInfo.getPath());
    }
}
