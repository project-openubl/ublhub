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
package io.github.project.openubl.xsender.models;

import java.util.Objects;

public class SortBean {

    private final String fieldName;
    private final boolean asc;

    public SortBean(String fieldName, boolean asc) {
        this.fieldName = fieldName;
        this.asc = asc;
    }

    public String getFieldName() {
        return fieldName;
    }

    public boolean isAsc() {
        return asc;
    }

    public String getQuery() {
        return fieldName + ":" + (isAsc() ? "asc" : "desc");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SortBean sortBean = (SortBean) o;
        return Objects.equals(fieldName, sortBean.fieldName) &&
                Objects.equals(asc, sortBean.asc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldName, asc);
    }

    @Override
    public String toString() {
        return "SortBean{" +
                "fieldName='" + fieldName + '\'' +
                ", asc=" + asc +
                '}';
    }
}
