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
package io.github.project.openubl.xcontent.queries;

import org.kie.kogito.rules.DataSource;
import org.kie.kogito.rules.DataStore;
import org.kie.kogito.rules.RuleUnitData;

public class BoletaFacturaUnit implements RuleUnitData {

    private int maxAmount;
    private DataStore<BoletaFacturaUnit> loanApplications;

    public BoletaFacturaUnit() {
        this(DataSource.createStore(), 0);
    }

    public BoletaFacturaUnit(DataStore<BoletaFacturaUnit> loanApplications, int maxAmount) {
        this.loanApplications = loanApplications;
        this.maxAmount = maxAmount;
    }

    public DataStore<BoletaFacturaUnit> getLoanApplications() {
        return loanApplications;
    }

    public void setLoanApplications(DataStore<BoletaFacturaUnit> loanApplications) {
        this.loanApplications = loanApplications;
    }

    public int getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(int maxAmount) {
        this.maxAmount = maxAmount;
    }

}
