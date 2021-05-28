package io.github.project.openubl.xsender.sender.vertx;

import io.github.project.openubl.xsender.sender.XSenderManager;
import io.github.project.openubl.xsender.sender.SenderManager;
import io.github.project.openubl.xsender.sender.SenderProvider;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.common.annotation.Blocking;
import io.vertx.core.eventbus.EventBus;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

@Transactional
@ApplicationScoped
@SenderProvider(SenderProvider.Type.VERTX)
public class VertxSenderManager implements SenderManager {

    @Inject
    XSenderManager fileSenderUtils;

    @Inject
    EventBus eventBus;

    @Override
    public void fireSendDocument(String id) {
        eventBus.send("process-ubl-document", id);
    }

    @Blocking
    @ConsumeEvent("process-ubl-document")
    public void processUBLDocument(String id) {
//        boolean shouldSendFile = fileSenderUtils.validateFile(id);
//        if (shouldSendFile) {
//            boolean shouldVerifyTicket = fileSenderUtils.sendFile(id);
//            if (shouldVerifyTicket) {
//                fileSenderUtils.checkTicket(id);
//            }
//        }
    }
}
