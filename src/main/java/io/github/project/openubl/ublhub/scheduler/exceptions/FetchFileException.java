package io.github.project.openubl.ublhub.scheduler.exceptions;

import io.github.project.openubl.ublhub.models.JobPhaseType;

public class FetchFileException extends Exception {

    private final JobPhaseType phase;

    public FetchFileException(JobPhaseType phase) {
        this.phase = phase;
    }

    public FetchFileException(JobPhaseType phase, Throwable e) {
        super(e);
        this.phase = phase;
    }

    public JobPhaseType getPhase() {
        return phase;
    }
}
