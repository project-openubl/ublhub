package io.github.project.openubl.xsender.sendstream;

import io.github.project.openubl.xsender.models.jpa.entities.UBLDocumentEntity;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import io.vertx.core.eventbus.EventBus;

import javax.inject.Inject;
import javax.transaction.Transactional;

public class MutinySendStream implements SendStream {

    @Inject
    EventBus eventBus;

    @Override
    public void send(UBLDocumentEntity documentEntity) {
        eventBus.send("process-bulk-assessment-creation", "bulkNew.id");
    }

    @Transactional
    @ConsumeEvent("process-bulk-assessment-creation")
    @Blocking
    public void processApplicationAssessmentCreationAsync(Long bulkId) {
        Uni.createFrom().item("hello")
                .onItem().transform(item -> item + " mutiny")
                .onItem().transform(String::toUpperCase)
                .subscribe().with(success -> {
            System.out.println(">> " + success);
        }, failure -> {

        });

        Uni.createFrom().item("").fai

Uni
    }
}
