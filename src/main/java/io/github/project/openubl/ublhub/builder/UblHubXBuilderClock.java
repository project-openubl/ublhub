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
package io.github.project.openubl.ublhub.builder;

import io.github.project.openubl.xmlbuilderlib.clock.SystemClock;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import java.util.Calendar;
import java.util.TimeZone;

@Default
@ApplicationScoped
public class UblHubXBuilderClock implements SystemClock {

    @ConfigProperty(name = "openubl.xbuilder.timezone")
    String timezone;

    @Override
    public TimeZone getTimeZone() {
        return TimeZone.getTimeZone(timezone);
    }

    @Override
    public Calendar getCalendarInstance() {
        return Calendar.getInstance();
    }

}
