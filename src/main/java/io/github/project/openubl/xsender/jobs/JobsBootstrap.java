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
package io.github.project.openubl.xsender.jobs;

import io.github.project.openubl.xsender.events.EventProvider;
import io.github.project.openubl.xsender.models.jpa.UBLDocumentRepository;
import io.github.project.openubl.xsender.models.jpa.entities.UBLDocumentEntity;
import io.github.project.openubl.xsender.ws.WSSunatClient;
import io.quarkus.arc.Arc;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.quartz.*;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class JobsBootstrap {

    @ConfigProperty(name = "openubl.event-manager.type")
    EventProvider.Type eventManager;

    @ConfigProperty(name = "openubl.event-manager.basic.retry-delay")
    Long retryDelay;

    @Inject
    Scheduler quartz;

    @Inject
    UBLDocumentRepository documentRepository;

    @Inject
    WSSunatClient wsSunatClient;

    void onStart(@Observes StartupEvent ev) throws SchedulerException {
        if (eventManager.equals(EventProvider.Type.basic)) {
            JobDetail job = JobBuilder.newJob(RetryJob.class)
                    .withIdentity("retryJob", "retryGroup")
                    .build();
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("retryJob", "retryGroup")
                    .startNow()
                    .withSchedule(
                            SimpleScheduleBuilder.simpleSchedule()
                                    .withIntervalInMilliseconds(retryDelay)
                                    .repeatForever())
                    .build();
            quartz.scheduleJob(job, trigger);
        }
    }


    // Just to force Quartz to start
    @Scheduled(cron = "0 0 0 * * ?")
    void schedule() {
    }

    @Transactional
    void performTask() {
        // Resend documents
        List<UBLDocumentEntity> documentScheduled = documentRepository.findAllScheduledToDeliver();
        for (UBLDocumentEntity documentEntity : documentScheduled) {
            wsSunatClient.sendDocument(documentEntity.getId());
        }

        // Resend tickets
        List<UBLDocumentEntity> documentsWithUncheckedTickets = documentRepository.findAllSheduledToCheckTicket();

        for (UBLDocumentEntity documentEntity : documentsWithUncheckedTickets) {
            wsSunatClient.checkDocumentTicket(documentEntity.getId());
        }
    }

    public static class RetryJob implements Job {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            Arc.container().instance(JobsBootstrap.class).get().performTask();
        }
    }

}
