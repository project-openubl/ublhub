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
import io.github.project.openubl.xsender.models.jpa.entities.CompanyEntity;
import io.github.project.openubl.xsender.models.jpa.entities.NamespaceEntity;
import io.github.project.openubl.xsender.models.jpa.entities.UBLDocumentEntity;

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

        return rep;
    }

    public static CompanyRepresentation toRepresentation(CompanyEntity entity) {
        CompanyRepresentation rep = new CompanyRepresentation();

        rep.setId(entity.id);
        rep.setRuc(entity.ruc);
        rep.setName(entity.name);
        rep.setDescription(entity.description);

        if (entity.sunatUrls != null) {
            SunatUrlsRepresentation sunatUrlsRep = new SunatUrlsRepresentation();
            rep.setWebServices(sunatUrlsRep);

            sunatUrlsRep.setFactura(entity.sunatUrls.sunatUrlFactura);
            sunatUrlsRep.setGuia(entity.sunatUrls.sunatUrlGuiaRemision);
            sunatUrlsRep.setRetenciones(entity.sunatUrls.sunatUrlPercepcionRetencion);
        }

        if (entity.sunatCredentials != null) {
            SunatCredentialsRepresentation credentialsRep = new SunatCredentialsRepresentation();
            rep.setCredentials(credentialsRep);

            credentialsRep.setUsername(entity.sunatCredentials.sunatUsername);
        }

        return rep;
    }

    public static DocumentRepresentation toRepresentation(UBLDocumentEntity entity) {
        DocumentRepresentation rep = new DocumentRepresentation();

        rep.setId(entity.id);
        rep.setInProgress(entity.inProgress);

        rep.setCreatedOn(entity.createdOn.getTime());
        rep.setError(entity.error != null ? entity.error.getMessage() : null);
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

//    public static ComponentRepresentation toRepresentation(ComponentModel component, boolean internal, ComponentUtil componentUtil) {
//        ComponentRepresentation rep = toRepresentationWithoutConfig(component);
//        if (!internal) {
//            rep = StripSecretsUtils.strip(componentUtil, rep);
//        }
//        return rep;
//    }
//
//    public static ComponentRepresentation toRepresentationWithoutConfig(ComponentModel component) {
//        ComponentRepresentation rep = new ComponentRepresentation();
//        rep.setId(component.getId());
//        rep.setName(component.getName());
//        rep.setProviderId(component.getProviderId());
//        rep.setProviderType(component.getProviderType());
//        rep.setSubType(component.getSubType());
//        rep.setParentId(component.getParentId());
//        rep.setConfig(new MultivaluedHashMap<>(component.getConfig()));
//        return rep;
//    }
//
//    public static List<ConfigPropertyRepresentation> toRepresentation(List<ProviderConfigProperty> configProperties) {
//        List<ConfigPropertyRepresentation> propertiesRep = new LinkedList<>();
//        for (ProviderConfigProperty prop : configProperties) {
//            ConfigPropertyRepresentation propRep = toRepresentation(prop);
//            propertiesRep.add(propRep);
//        }
//        return propertiesRep;
//    }
//
//    public static ConfigPropertyRepresentation toRepresentation(ProviderConfigProperty prop) {
//        ConfigPropertyRepresentation propRep = new ConfigPropertyRepresentation();
//        propRep.setName(prop.getName());
//        propRep.setLabel(prop.getLabel());
//        propRep.setType(prop.getType());
//        propRep.setDefaultValue(prop.getDefaultValue());
//        propRep.setOptions(prop.getOptions());
//        propRep.setHelpText(prop.getHelpText());
//        propRep.setSecret(prop.isSecret());
//        return propRep;
//    }
//
    public static <T, R> PageRepresentation<R> toRepresentation(List<T> pageElements, Long totalElements, Function<T, R> mapper) {
        PageRepresentation<R> rep = new PageRepresentation<>();

        // Meta
        PageRepresentation.Meta repMeta = new PageRepresentation.Meta();
        rep.setMeta(repMeta);

        repMeta.setCount(totalElements);

        // Data
        rep.setData(pageElements.stream()
                .map(mapper)
                .collect(Collectors.toList())
        );

        return rep;
    }

//    private static URIBuilder getURIBuilder(UriInfo uriInfo) throws URISyntaxException {
//        return new URIBuilder(uriInfo.getPath());
//    }
}
