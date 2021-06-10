package io.github.project.openubl.xsender.xbuilder;

import io.github.project.openubl.xmlbuilderlib.clock.SystemClock;

import java.util.Calendar;
import java.util.TimeZone;

public class XBuilderSystemClock implements SystemClock {

    private final TimeZone timeZone = TimeZone.getTimeZone("America/Lima");

    @Override
    public TimeZone getTimeZone() {
        return timeZone;
    }

    @Override
    public Calendar getCalendarInstance() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(timeZone);
        return calendar;
    }
}
