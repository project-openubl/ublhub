package io.github.project.openubl.ublhub.documents;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DocumentImportResult {
    private Long documentId;
    private String errorMessage;
}
