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
import io.quarkus.qute.EngineBuilder;
import io.quarkus.qute.TemplateLocator;
import io.quarkus.qute.Variant;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.io.Reader;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class DbTemplateLocator implements TemplateLocator {

    private static final Logger LOG = Logger.getLogger(DbTemplateLocator.class);

    public static final String SEPARATOR = ":";

    public static String encodeTemplateName(ComponentOwner owner, String type, String documentType) {
        return MessageFormat.format("{0}{1}{2}{3}{4}{5}{6}",
                owner.getProject(), SEPARATOR,
                owner.getRuc(), SEPARATOR,
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
                    .project(templateData[0])
                    .ruc(templateData[1])
                    .build();
            templateType = TemplateType.valueOf(templateData[2]);
            documentType = templateData[3];
        } catch (Exception e) {
            return Optional.empty();
        }

        Optional<QuteTemplateEntity> template;

        Map<String, Object> qParams = new HashMap<>();
        qParams.put("templateType", templateType);
        qParams.put("documentType", documentType);
        qParams.put("project", owner.getProject());

        if (owner.getRuc() != null && !owner.getRuc().isEmpty()) {
            qParams.put("ruc", owner.getRuc());
            template = QuteTemplateEntity.find("project = :project and ruc = :ruc and templateType = :templateType and documentType = :documentType", qParams).firstResultOptional();
        } else {
            template = QuteTemplateEntity.find("project =: project and templateType = :templateType and documentType = :documentType", qParams).firstResultOptional();
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
