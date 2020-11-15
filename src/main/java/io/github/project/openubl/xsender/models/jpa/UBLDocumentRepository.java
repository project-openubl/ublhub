/**
 * Copyright 2019 Project OpenUBL, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Eclipse Public License - v 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.project.openubl.xsender.models.jpa;

import io.github.project.openubl.xsender.models.DeliveryStatusType;
import io.github.project.openubl.xsender.models.jpa.entities.UBLDocumentEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class UBLDocumentRepository implements PanacheRepositoryBase<UBLDocumentEntity, String> {

    public List<UBLDocumentEntity> findAllScheduledToDeliver() {
        return list("deliveryStatus", DeliveryStatusType.SCHEDULED_TO_DELIVER);
    }

    public List<UBLDocumentEntity> findAllSheduledToCheckTicket() {
        return list("deliveryStatus", DeliveryStatusType.SCHEDULED_CHECK_TICKET);
    }

}
