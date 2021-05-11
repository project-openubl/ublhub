package io.github.project.openubl.xsender.resources.health.storage;

import javax.enterprise.util.AnnotationLiteral;

public class StorageProviderLiteral extends AnnotationLiteral<StorageProvider> implements StorageProvider {

    private final StorageProvider.Type providerType;

    public StorageProviderLiteral(Type providerType) {
        this.providerType = providerType;
    }

    @Override
    public Type value() {
        return providerType;
    }
}
