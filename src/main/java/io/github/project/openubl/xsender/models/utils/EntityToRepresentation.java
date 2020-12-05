/**
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

import io.github.project.openubl.xsender.idm.CompanyRepresentation;
import io.github.project.openubl.xsender.idm.DocumentRepresentation;
import io.github.project.openubl.xsender.idm.PageRepresentation;
import io.github.project.openubl.xsender.idm.SunatUrlsRepresentation;
import io.github.project.openubl.xsender.models.PageModel;
import io.github.project.openubl.xsender.models.jpa.entities.CompanyEntity;
import io.github.project.openubl.xsender.models.jpa.entities.UBLDocumentEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;

import javax.ws.rs.core.UriInfo;
import java.net.URISyntaxException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EntityToRepresentation {
    private EntityToRepresentation() {
        // Just static methods
    }

    public static CompanyRepresentation toRepresentation(CompanyEntity entity) {
        CompanyRepresentation rep = new CompanyRepresentation();

        rep.setId(entity.getId());
        rep.setName(entity.getName());

        if (entity.getSunatUrls() != null) {
            SunatUrlsRepresentation sunatUrlsRep = new SunatUrlsRepresentation();
            rep.setWebServices(sunatUrlsRep);

            sunatUrlsRep.setFactura(entity.getSunatUrls().getSunatUrlFactura());
            sunatUrlsRep.setGuia(entity.getSunatUrls().getSunatUrlGuiaRemision());
            sunatUrlsRep.setRetenciones(entity.getSunatUrls().getSunatUrlPercepcionRetencion());
        }

        return rep;
    }

    public static DocumentRepresentation toRepresentation(UBLDocumentEntity entity) {
        DocumentRepresentation rep = new DocumentRepresentation();

        rep.setId(entity.getId());
        rep.setCdrID(entity.getStorageCdr());
        rep.setFileID(entity.getStorageFile());
        rep.setDeliveryStatus(entity.getDeliveryStatus().toString());

        //

        DocumentRepresentation.FileInfoRepresentation fileInfoRep = new DocumentRepresentation.FileInfoRepresentation();
        rep.setFileInfo(fileInfoRep);

        fileInfoRep.setRuc(entity.getRuc());
        fileInfoRep.setDocumentID(entity.getDocumentID());
        fileInfoRep.setDocumentType(entity.getDocumentType().getType());
        fileInfoRep.setFilename(entity.getFilename());

        //

        DocumentRepresentation.SunatSecurityCredentialsRepresentation sunatCredentialsRep = new DocumentRepresentation.SunatSecurityCredentialsRepresentation();
        rep.setSunatCredentials(sunatCredentialsRep);

        //

        DocumentRepresentation.SunatStatusRepresentation sunatStatus = new DocumentRepresentation.SunatStatusRepresentation();
        rep.setSunatStatus(sunatStatus);

        sunatStatus.setCode(entity.getSunatCode());
        sunatStatus.setTicket(entity.getSunatTicket());
        sunatStatus.setStatus(entity.getSunatStatus());
        sunatStatus.setDescription(entity.getSunatDescription());

        return rep;
    }

    public static <T, R> PageRepresentation<R> toRepresentation(
            PageModel<T> model,
            Function<T, R> mapper,
            UriInfo serverUriInfo,
            List<NameValuePair> queryParameters
    ) throws URISyntaxException {
        queryParameters.removeIf(f -> f.getName().equals("offset")); // The offset will be set in the current method

        PageRepresentation<R> rep = new PageRepresentation<>();

        // Meta
        PageRepresentation.Meta repMeta = new PageRepresentation.Meta();
        rep.setMeta(repMeta);

        repMeta.setCount(model.getTotalElements());
        repMeta.setOffset(model.getOffset());
        repMeta.setLimit(model.getLimit());

        // Data
        rep.setData(model.getPageElements().stream()
                .map(mapper)
                .collect(Collectors.toList())
        );

        // Links
        queryParameters.add(new BasicNameValuePair("limit", String.valueOf(model.getLimit()))); // all links have same 'limit'

        PageRepresentation.Links repLinks = new PageRepresentation.Links();
        rep.setLinks(repLinks);

        // Links first
        URIBuilder uriBuilder = getURIBuilder(serverUriInfo);
        uriBuilder.addParameter("offset", String.valueOf(0));
        uriBuilder.addParameters(queryParameters);
        repLinks.setFirst(uriBuilder.build().toString());

        // Links last
        long offsetLast;
        long numberOfPages = model.getTotalElements() / model.getLimit();
        offsetLast = numberOfPages * model.getLimit();
        if (offsetLast == model.getTotalElements()) {
            offsetLast = offsetLast - model.getLimit();
        }

        uriBuilder = getURIBuilder(serverUriInfo);
        uriBuilder.addParameter("offset", String.valueOf(offsetLast));
        uriBuilder.addParameters(queryParameters);
        repLinks.setLast(uriBuilder.build().toString());

        // Links previous
        if (model.getOffset() != 0) {
            long offsetPrevious = model.getOffset() - model.getLimit();
            if (offsetPrevious < 0) {
                offsetPrevious = 0;
            }

            uriBuilder = getURIBuilder(serverUriInfo);
            uriBuilder.addParameter("offset", String.valueOf(offsetPrevious));
            uriBuilder.addParameters(queryParameters);
            repLinks.setPrevious(uriBuilder.build().toString());
        }

        // Links next
        if (model.getOffset() / model.getLimit() < model.getTotalElements() / model.getLimit()) {
            long offsetNext = model.getOffset() + model.getLimit();

            uriBuilder = getURIBuilder(serverUriInfo);
            uriBuilder.addParameter("offset", String.valueOf(offsetNext));
            uriBuilder.addParameters(queryParameters);
            repLinks.setNext(uriBuilder.build().toString());
        }

        return rep;
    }

    private static URIBuilder getURIBuilder(UriInfo uriInfo) throws URISyntaxException {
        return new URIBuilder(uriInfo.getPath());
    }
}
