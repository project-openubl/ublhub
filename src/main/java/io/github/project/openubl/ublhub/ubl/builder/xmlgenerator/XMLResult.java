package io.github.project.openubl.ublhub.ubl.builder.xmlgenerator;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class XMLResult {
    private String ruc;
    private String xml;
}
