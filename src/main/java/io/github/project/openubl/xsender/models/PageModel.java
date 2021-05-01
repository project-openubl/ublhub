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

import java.util.List;

public class PageModel<T> {
    private final int offset;
    private final int limit;
    private final long totalElements;
    private final List<T> pageElements;

    public PageModel(PageBean pageBean, long totalElements, List<T> pageElements) {
        this.offset = pageBean.getOffset();
        this.limit = pageBean.getLimit();
        this.totalElements = totalElements;
        this.pageElements = pageElements;
    }

    public PageModel(int offset, int limit, long totalElements, List<T> pageElements) {
        this.offset = offset;
        this.limit = limit;
        this.totalElements = totalElements;
        this.pageElements = pageElements;
    }

    public int getOffset() {
        return offset;
    }

    public int getLimit() {
        return limit;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public List<T> getPageElements() {
        return pageElements;
    }
}
