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
package io.github.project.openubl.ublhub.qute;

import io.github.project.openubl.ublhub.keys.component.ComponentOwner;
import io.github.project.openubl.ublhub.models.TemplateType;
import io.github.project.openubl.ublhub.models.jpa.entities.QuteTemplateEntity;
import io.quarkus.panache.common.Parameters;
import io.quarkus.qute.EngineBuilder;
import io.quarkus.qute.TemplateLocator;
import io.quarkus.qute.Variant;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.io.Reader;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.Optional;

@ApplicationScoped
public class DbTemplateLocator implements TemplateLocator {

    private static final Logger LOG = Logger.getLogger(DbTemplateLocator.class);

    public static final String SEPARATOR = "&&";

    public static String encodeTemplateName(ComponentOwner owner, String type, String documentType) {
        return MessageFormat.format("{0}{1}{2}{3}{4}{5}{6}",
                owner.getType(), SEPARATOR,
                owner.getId().toString(), SEPARATOR,
                type, SEPARATOR,
                documentType
        );
    }

    @Override
    public Optional<TemplateLocation> locate(String templateName) {
        String[] templateData = templateName.split(SEPARATOR);
        if (templateData.length != 4) {
            return Optional.empty();
        }

        ComponentOwner owner;
        TemplateType templateType;
        String documentType;
        try {
            owner = ComponentOwner.builder()
                    .type(ComponentOwner.OwnerType.valueOf(templateData[0]))
                    .id(Long.valueOf(templateData[1]))
                    .build();
            templateType = TemplateType.valueOf(templateData[2]);
            documentType = templateData[3];
        } catch (Exception e) {
            return Optional.empty();
        }

        Optional<QuteTemplateEntity> template;
        Parameters qParams = Parameters.with("templateType", templateType)
                .and("documentType", documentType);
        switch (owner.getType()) {
            case project -> {
                qParams = qParams.and("projectId", owner.getId());
                template = QuteTemplateEntity.find("projectId =: projectId and templateType = :templateType and documentType = :documentType", qParams).firstResultOptional();
            }
            case company -> {
                qParams = qParams.and("companyId", owner.getId());
                template = QuteTemplateEntity.find("companyId =: companyId and templateType = :templateType and documentType = :documentType", qParams).firstResultOptional();
            }
            default -> {
                template = Optional.empty();
            }
        }

        if (template.isEmpty()) {
            LOG.tracef("Template with [owner=%s templateType=%s document=%s] not found in the database", owner, templateType, documentType);
            return Optional.empty();
        } else {
            LOG.tracef("Template with [owner=%s templateType=%s document=%s] found in the database", owner, templateType, documentType);
            return Optional.of(buildTemplateLocation(template.get().getContent()));
        }
    }

    @Override
    public int getPriority() {
        return DEFAULT_PRIORITY - 1;
    }

    void configureEngine(@Observes EngineBuilder builder) {
        builder.addLocator(this);
    }

    private TemplateLocation buildTemplateLocation(String templateContent) {
        return new TemplateLocation() {
            @Override
            public Reader read() {
                return new StringReader(templateContent);
            }

            @Override
            public Optional<Variant> getVariant() {
                return Optional.empty();
            }
        };
    }

}
