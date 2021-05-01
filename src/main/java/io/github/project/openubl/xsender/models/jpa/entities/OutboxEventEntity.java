/*
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
package io.github.project.openubl.xsender.models.jpa.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Parameters;

import javax.persistence.*;
import java.util.Date;
import java.util.Map;

@Entity
@Table(name = "outboxevent")
public class OutboxEventEntity extends PanacheEntityBase {

    @Id
    @Column(name = "id")
    @Access(AccessType.PROPERTY)
    public String id;

    @Column(name = "aggregatetype")
    public String aggregateType;

    @Column(name = "aggregateid")
    public String aggregateId;

    @Column(name = "type")
    public String type;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "timestamp")
    public Date timestamp;

    @Column(name = "payload")
    public String payload;

    public static OutboxEventEntity findByParams(String aggregateType, String aggregateId, String type) {
        Map<String, Object> params = Parameters.with("aggregateType", aggregateType)
                .and("aggregateId", aggregateId)
                .and("type", type)
                .map();

        return OutboxEventEntity.find(
                "aggregateType = :aggregateType and aggregateId = :aggregateId and type = :type",
                params
        ).firstResult();
    }
}
