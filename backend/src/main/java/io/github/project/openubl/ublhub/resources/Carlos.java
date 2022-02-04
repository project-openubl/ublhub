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
package io.github.project.openubl.ublhub.resources;

import io.github.project.openubl.xcontent.models.standard.general.BoletaFactura;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Path("/find-approved")
public class Carlos {

//    @Inject
//    KieRuntimeBuilder kieRuntimeBuilder;
//
//    @POST()
//    @Produces(MediaType.APPLICATION_JSON)
//    @Consumes(MediaType.APPLICATION_JSON)
//    public List<BoletaFactura> executeQuery(BoletaFactura representation) {
//        KieSession session = kContainer.newKieSession();
//        List<BoletaFactura> approvedApplications = new ArrayList<>();
//
//        session.setGlobal("approvedApplications", approvedApplications);
//
//        session.insert(representation);
//
//        session.fireAllRules();
//        session.dispose();
//        return approvedApplications;
//    }

}
