package io.github.project.openubl.xsender.builder;

import io.github.project.openubl.xmlbuilderlib.clock.SystemClock;

import java.util.Calendar;
import java.util.TimeZone;

public class UblHubXBuilderClock implements SystemClock {

    private TimeZone timeZone;

    @Override
    public TimeZone getTimeZone() {
        return timeZone;
    }

    @Override
    public Calendar getCalendarInstance() {
        return Calendar.getInstance();
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

}
