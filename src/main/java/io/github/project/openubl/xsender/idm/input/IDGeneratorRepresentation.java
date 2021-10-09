package io.github.project.openubl.xsender.idm.input;

import io.github.project.openubl.xsender.idgenerator.IDGeneratorType;

import javax.validation.constraints.NotNull;
import java.util.Map;

public class IDGeneratorRepresentation {

    @NotNull
    private IDGeneratorType name;

    private Map<String, String> config;

    public IDGeneratorType getName() {
        return name;
    }

    public void setName(IDGeneratorType name) {
        this.name = name;
    }

    public Map<String, String> getConfig() {
        return config;
    }

    public void setConfig(Map<String, String> config) {
        this.config = config;
    }

    public static final class Builder {
        private IDGeneratorType name;
        private Map<String, String> config;

        private Builder() {
        }

        public static Builder anIDGeneratorRepresentation() {
            return new Builder();
        }

        public Builder withName(IDGeneratorType name) {
            this.name = name;
            return this;
        }

        public Builder withConfig(Map<String, String> config) {
            this.config = config;
            return this;
        }

        public IDGeneratorRepresentation build() {
            IDGeneratorRepresentation iDGeneratorRepresentation = new IDGeneratorRepresentation();
            iDGeneratorRepresentation.setName(name);
            iDGeneratorRepresentation.setConfig(config);
            return iDGeneratorRepresentation;
        }
    }
}
