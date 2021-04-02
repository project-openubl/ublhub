package io.github.project.openubl.xsender.kafka.idm;

import java.util.List;

public class BillServiceContentRepresentation {

    private String status;
    private Integer code;
    private String description;
    private String ticket;
    private List<String> notes;

    private String cdrStorageId;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCdrStorageId() {
        return cdrStorageId;
    }

    public void setCdrStorageId(String cdrStorageId) {
        this.cdrStorageId = cdrStorageId;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public List<String> getNotes() {
        return notes;
    }

    public void setNotes(List<String> notes) {
        this.notes = notes;
    }
}
