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
package io.github.project.openubl.ublhub.models.utils;

import io.github.project.openubl.ublhub.idm.*;
import io.github.project.openubl.ublhub.keys.component.ComponentModel;
import io.github.project.openubl.ublhub.keys.component.utils.ComponentUtil;
import io.github.project.openubl.ublhub.keys.provider.ProviderConfigProperty;
import io.github.project.openubl.ublhub.keys.utils.StripSecretsUtils;
import io.github.project.openubl.ublhub.models.jpa.entities.CompanyEntity;
import io.github.project.openubl.ublhub.models.jpa.entities.NamespaceEntity;
import io.github.project.openubl.ublhub.models.jpa.entities.SunatEntity;
import io.github.project.openubl.ublhub.models.jpa.entities.UBLDocumentEntity;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.representations.idm.ComponentRepresentation;
import org.keycloak.representations.idm.ConfigPropertyRepresentation;

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

        rep.setId(entity.id);
        rep.setName(entity.name);
        rep.setDescription(entity.description);

        // URLs
        SunatUrlsRepresentation sunatUrlsRep = toRepresentationUrls(entity.sunat);
        rep.setWebServices(sunatUrlsRep);

        // Credentials
        SunatCredentialsRepresentation credentialsRep = toRepresentationCredentials(entity.sunat);
        rep.setCredentials(credentialsRep);

        return rep;
    }

    public static CompanyRepresentation toRepresentation(CompanyEntity entity) {
        CompanyRepresentation rep = new CompanyRepresentation();

        rep.setId(entity.id);
        rep.setRuc(entity.ruc);
        rep.setName(entity.name);
        rep.setDescription(entity.description);

        if (entity.sunat != null) {
            // URLs
            SunatUrlsRepresentation sunatUrlsRep = toRepresentationUrls(entity.sunat);
            rep.setWebServices(sunatUrlsRep);

            // Credentials
            SunatCredentialsRepresentation credentialsRep = toRepresentationCredentials(entity.sunat);
            rep.setCredentials(credentialsRep);
        }

        return rep;
    }

    private static SunatUrlsRepresentation toRepresentationUrls(SunatEntity entity) {
        SunatUrlsRepresentation sunatUrlsRep = new SunatUrlsRepresentation();

        sunatUrlsRep.setFactura(entity.sunatUrlFactura);
        sunatUrlsRep.setGuia(entity.sunatUrlGuiaRemision);
        sunatUrlsRep.setRetenciones(entity.sunatUrlPercepcionRetencion);

        return sunatUrlsRep;
    }

    private static SunatCredentialsRepresentation toRepresentationCredentials(SunatEntity entity) {
        SunatCredentialsRepresentation credentialsRep = new SunatCredentialsRepresentation();
        credentialsRep.setUsername(entity.sunatUsername);
        return credentialsRep;
    }

    public static DocumentRepresentation toRepresentation(UBLDocumentEntity entity) {
        DocumentRepresentation rep = new DocumentRepresentation();

        rep.setId(entity.id);
        rep.setNamespaceId(entity.namespace.id);

        rep.setInProgress(entity.inProgress);

        rep.setCreatedOn(entity.createdOn.getTime());
        rep.setError(entity.error);
        rep.setScheduledDelivery(entity.scheduledDelivery != null ? entity.scheduledDelivery.getTime() : null);
        rep.setRetryCount(entity.retries);

        // File

        rep.setFileContentValid(entity.fileValid);

        rep.setFileContent(new FileContentRepresentation());
        rep.getFileContent().setRuc(entity.ruc);
        rep.getFileContent().setDocumentID(entity.documentID);
        rep.getFileContent().setDocumentType(entity.documentType);

        // Sunat

        rep.setSunat(new SunatStatusRepresentation());

        rep.getSunat().setCode(entity.sunatCode);
        rep.getSunat().setTicket(entity.sunatTicket);
        rep.getSunat().setStatus(entity.sunatStatus);
        rep.getSunat().setDescription(entity.sunatDescription);
        rep.getSunat().setHasCdr(entity.storageCdr != null);

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
        ComponentRepresentation rep = new ComponentRepresentation();
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
        List<ConfigPropertyRepresentation> propertiesRep = new LinkedList<>();
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

    public static <T, R> PageRepresentation<R> toRepresentation(List<T> pageElements, Long totalElements, Function<T, R> mapper) {
        PageRepresentation<R> rep = new PageRepresentation<>();

        // Meta
        PageRepresentation.Meta repMeta = new PageRepresentation.Meta();
        rep.setMeta(repMeta);

        repMeta.setCount(totalElements);

        // Data
        rep.setItems(pageElements.stream()
                .map(mapper)
                .collect(Collectors.toList())
        );

        return rep;
    }

}
