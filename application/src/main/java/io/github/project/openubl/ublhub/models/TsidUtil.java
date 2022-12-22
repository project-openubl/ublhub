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
package io.github.project.openubl.ublhub.models;

import com.github.f4b6a3.tsid.TsidFactory;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.util.concurrent.ThreadLocalRandom;

public class TsidUtil {

    @ConfigProperty(name = "openubl.ublhub.tsid.bytes")
    int tsidBytes;

    @Produces
    @ApplicationScoped
    public TsidFactory provideTsidFactory() {
        int nodeBits = (int) (Math.log(tsidBytes) / Math.log(2));

        return TsidFactory.builder()
                .withRandomFunction(length -> {
                    final byte[] bytes = new byte[length];
                    ThreadLocalRandom.current().nextBytes(bytes);
                    return bytes;
                })
                .withNodeBits(nodeBits)
                .build();
    }

}
