package io.github.project.openubl.xsender.resources.health.storage;

import javax.inject.Qualifier;
import java.lang.annotation.*;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE})
@Documented
public @interface StorageProvider {
    Type value();

    enum Type {
        S3
    }
}
