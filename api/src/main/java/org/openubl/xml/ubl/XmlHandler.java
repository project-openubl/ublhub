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
package org.openubl.xml.ubl;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XmlHandler extends DefaultHandler {

    private static final String CBC = "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2";
    private static final String CAC = "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2";

    private static final String ID = "ID";
    private static final String ACCOUNTING_SUPPLIER_PARTY = "AccountingSupplierParty";
    private static final String PARTY = "Party";
    private static final String PARTY_IDENTIFICATION = "PartyIdentification";
    private static final String CUSTOMER_ASSIGNED_ACCOUNT_ID = "CustomerAssignedAccountID";


    private String documentType;
    private String documentID;
    private String ruc;

    private boolean isAccountingSupplierPartyBeingRead;
    private boolean isPartyBeingRead;
    private boolean isPartyIdentificationBeingRead;

    private String currentElement;
    private StringBuilder currentText;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attr) throws SAXException {
        //
        currentElement = localName;

        // Root element
        if (documentType == null) {
            documentType = currentElement;
        }

        //
        if (currentElement.equals(ID) && uri.equals(CBC)) {
            currentText = new StringBuilder();
        } else if (currentElement.equals(ACCOUNTING_SUPPLIER_PARTY) && uri.equals(CAC)) {
            isAccountingSupplierPartyBeingRead = true;
        } else if (currentElement.equals(PARTY) && uri.equals(CAC)) {
            isPartyBeingRead = true;
        } else if (currentElement.equals(PARTY_IDENTIFICATION) && uri.equals(CAC)) {
            isPartyIdentificationBeingRead = true;
        } else if (currentElement.equals(CUSTOMER_ASSIGNED_ACCOUNT_ID) && uri.equals(CBC)) {
            currentText = new StringBuilder();
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (currentText != null) {
            String content = currentText.toString().trim();

            if (currentElement.equals(ID) && uri.equals(CBC)) {
                if (documentID == null) {
                    documentID = content;
                } else if (isAccountingSupplierPartyBeingRead && isPartyBeingRead && isPartyIdentificationBeingRead) {
                    // invoice, credit-note, debit-note
                    ruc = content;
                }
            } else if (currentElement.equals(CUSTOMER_ASSIGNED_ACCOUNT_ID) && uri.equals(CBC)) {
                // voided-document, summary-document
                if (ruc == null) {
                    ruc = content;
                }
            }

            currentText = null;
        }

        // set 'beingRead' var to false
        if (localName.equals(ACCOUNTING_SUPPLIER_PARTY) && uri.equals(CAC)) {
            isAccountingSupplierPartyBeingRead = false;
        } else if (localName.equals(PARTY) && uri.equals(CAC)) {
            isPartyBeingRead = false;
        } else if (localName.equals(PARTY_IDENTIFICATION) && uri.equals(CAC)) {
            isPartyIdentificationBeingRead = false;
        }

        currentElement = "";
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (currentText != null) {
            currentText.append(ch, start, length);
        }
    }

    public XmlContentModel getModel() {
        return XmlContentModel.Builder.aSunatDocumentModel()
                .withDocumentType(documentType)
                .withDocumentID(documentID)
                .withRuc(ruc)
                .build();
    }

}
