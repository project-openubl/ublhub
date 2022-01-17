/*
 * Copyright 2019 Project OpenUBL, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.project.openubl.ublhub.ubl.builder.xmlgenerator;

import io.github.project.openubl.xmlbuilderlib.clock.SystemClock;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import java.util.Calendar;
import java.util.TimeZone;

@Alternative
@Priority(1)
@ApplicationScoped
public class XMLGeneratorClockMock implements SystemClock {

    private final TimeZone timeZone;
    private final Calendar calendar;

    public XMLGeneratorClockMock() {
        this.timeZone = TimeZone.getTimeZone("America/Lima");

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(timeZone);
        calendar.set(2019, Calendar.DECEMBER, 24, 20, 30, 59);
        this.calendar = calendar;
    }

    @Override
    public TimeZone getTimeZone() {
        return timeZone;
    }

    @Override
    public Calendar getCalendarInstance() {
        return calendar;
    }

}
