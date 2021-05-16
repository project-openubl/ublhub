package io.github.project.openubl.xsender.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.debezium.outbox.quarkus.ExportedEvent;
import io.github.project.openubl.xsender.kafka.idm.UBLDocumentSunatEventRepresentation;
import io.github.project.openubl.xsender.kafka.producers.UBLDocumentCreatedEventProducer;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.transaction.Transactional;

@ApplicationScoped
public class DocumentJob implements Job{

    @Inject
    Event<ExportedEvent<?, ?>> event;

    @Inject
    ObjectMapper objectMapper;

    @Transactional
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobData = context.getJobDetail().getJobDataMap();

        UBLDocumentSunatEventRepresentation eventRep = new UBLDocumentSunatEventRepresentation();
        eventRep.setId(jobData.getString("id"));
        eventRep.setStorageFile(jobData.getString("storageId"));
        eventRep.setNamespace(jobData.getString("namespaceName"));

        String eventPayload = null;
        try {
            eventPayload = objectMapper.writeValueAsString(eventRep);
            event.fire(new UBLDocumentCreatedEventProducer(jobData.getString("id"), eventPayload));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

}
