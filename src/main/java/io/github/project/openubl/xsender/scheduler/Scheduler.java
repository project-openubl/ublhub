package io.github.project.openubl.xsender.scheduler;

import io.github.project.openubl.xsender.models.jpa.entities.UBLDocumentEntity;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import org.quartz.*;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@ApplicationScoped
public class Scheduler {

    @Inject
    org.quartz.Scheduler quartz;

    @Scheduled(cron = "0 0 0 * * ?")
    void schedule() {
    }

    public Date scheduleSend(UBLDocumentEntity documentEntity, int retry) throws SchedulerException {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 10);
//        calendar.add(Calendar.MINUTE, (int) Math.pow(5, retry));

        // Job config
        JobKey jobKey = new JobKey(documentEntity.getId(), "document");

        // Remove all previous jobs associated with document
        quartz.deleteJob(jobKey);

        // Schedule next job
        JobDetail job = JobBuilder.newJob(DocumentJob.class)
                .withIdentity(jobKey)
                .usingJobData("id", documentEntity.getId())
                .usingJobData("storageId", documentEntity.getStorageFile())
                .usingJobData("namespaceName", documentEntity.getNamespace().getName())
                .build();
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(UUID.randomUUID().toString())
                .startAt(calendar.getTime())
                .build();

        return quartz.scheduleJob(job, trigger);
    }

}
