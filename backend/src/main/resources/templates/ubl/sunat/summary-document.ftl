<@compress single_line=true>
<#setting number_format="computer">
<?xml version="1.0" encoding="ISO-8859-1"?>
<SummaryDocuments xmlns="urn:sunat:names:specification:ubl:peru:schema:xsd:SummaryDocuments-1"
                  xmlns:cac="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2"
                  xmlns:cbc="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2"
                  xmlns:ccts="urn:un:unece:uncefact:documentation:2" xmlns:ds="http://www.w3.org/2000/09/xmldsig#"
                  xmlns:ext="urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2"
                  xmlns:ns11="urn:sunat:names:specification:ubl:peru:schema:xsd:Perception-1"
                  xmlns:qdt="urn:oasis:names:specification:ubl:schema:xsd:QualifiedDatatypes-2"
                  xmlns:sac="urn:sunat:names:specification:ubl:peru:schema:xsd:SunatAggregateComponents-1"
                  xmlns:udt="urn:un:unece:uncefact:data:specification:UnqualifiedDataTypesSchemaModule:2"
                  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <ext:UBLExtensions>
        <ext:UBLExtension>
            <ext:ExtensionContent />
        </ext:UBLExtension>
    </ext:UBLExtensions>
    <cbc:UBLVersionID>2.0</cbc:UBLVersionID>
    <cbc:CustomizationID>1.1</cbc:CustomizationID>
    <cbc:ID>${serieNumero}</cbc:ID>
    <cbc:ReferenceDate>${fechaEmisionDeComprobantesAsociados}</cbc:ReferenceDate>
    <cbc:IssueDate>${fechaEmision}</cbc:IssueDate>
    <#include "../signature.ftl">
    <#include "common/supplier.ftl">
    <#list detalle as item>
    <sac:SummaryDocumentsLine>
        <cbc:LineID>${item?index + 1}</cbc:LineID>
        <cbc:DocumentTypeCode>${item.comprobante.tipo.code}</cbc:DocumentTypeCode>
        <cbc:ID>${item.comprobante.serieNumero}</cbc:ID>
        <cac:AccountingCustomerParty>
            <cbc:CustomerAssignedAccountID>${item.comprobante.cliente.numeroDocumentoIdentidad}</cbc:CustomerAssignedAccountID>
            <cbc:AdditionalAccountID>${item.comprobante.cliente.tipoDocumentoIdentidad.code}</cbc:AdditionalAccountID>
        </cac:AccountingCustomerParty>
        <#if item.comprobanteAfectado??>
        <cac:BillingReference>
            <cac:InvoiceDocumentReference>
                <cbc:ID>${item.comprobanteAfectado.serieNumero}</cbc:ID>
                <cbc:DocumentTypeCode>${item.comprobanteAfectado.tipo.code}</cbc:DocumentTypeCode>
            </cac:InvoiceDocumentReference>
        </cac:BillingReference>
        </#if>
        <cac:Status>
            <cbc:ConditionCode>${item.tipoOperacion.code}</cbc:ConditionCode>
        </cac:Status>
        <sac:TotalAmount currencyID="${moneda}">${item.comprobante.valorVenta.importeTotal}</sac:TotalAmount>
        <#if item.comprobante.valorVenta.gravado??>
        <sac:BillingPayment>
            <cbc:PaidAmount currencyID="${moneda}">${item.comprobante.valorVenta.gravado.importe}</cbc:PaidAmount>
            <cbc:InstructionID>${item.comprobante.valorVenta.gravado.tipo.code}</cbc:InstructionID>
        </sac:BillingPayment>
        </#if>
        <#if item.comprobante.valorVenta.exonerado??>
        <sac:BillingPayment>
            <cbc:PaidAmount currencyID="${moneda}">${item.comprobante.valorVenta.exonerado.importe}</cbc:PaidAmount>
            <cbc:InstructionID>${item.comprobante.valorVenta.exonerado.tipo.code}</cbc:InstructionID>
        </sac:BillingPayment>
        </#if>
        <#if item.comprobante.valorVenta.inafecto??>
        <sac:BillingPayment>
            <cbc:PaidAmount currencyID="${moneda}">${item.comprobante.valorVenta.inafecto.importe}</cbc:PaidAmount>
            <cbc:InstructionID>${item.comprobante.valorVenta.inafecto.tipo.code}</cbc:InstructionID>
        </sac:BillingPayment>
        </#if>
        <#if item.comprobante.valorVenta.gratuito??>
        <sac:BillingPayment>
            <cbc:PaidAmount currencyID="${moneda}">${item.comprobante.valorVenta.gratuito.importe}</cbc:PaidAmount>
            <cbc:InstructionID>${item.comprobante.valorVenta.gratuito.tipo.code}</cbc:InstructionID>
        </sac:BillingPayment>
        </#if>
        <#if item.comprobante.valorVenta.otrosCargos??>
        <cac:AllowanceCharge>
            <cbc:ChargeIndicator>true</cbc:ChargeIndicator>
            <cbc:Amount currencyID="${moneda}">${item.comprobante.valorVenta.otrosCargos}</cbc:Amount>
        </cac:AllowanceCharge>
        </#if>
        <#if item.comprobante.impuestos.igv??>
        <cac:TaxTotal>
            <cbc:TaxAmount currencyID="${moneda}">${item.comprobante.impuestos.igv.importe}</cbc:TaxAmount>
            <cac:TaxSubtotal>
                <cbc:TaxAmount currencyID="${moneda}">${item.comprobante.impuestos.igv.importe}</cbc:TaxAmount>
                <cac:TaxCategory>
                    <cac:TaxScheme>
                        <cbc:ID>${item.comprobante.impuestos.igv.categoria.code}</cbc:ID>
                        <cbc:Name>${item.comprobante.impuestos.igv.categoria.nombre}</cbc:Name>
                        <cbc:TaxTypeCode>${item.comprobante.impuestos.igv.categoria.tipo}</cbc:TaxTypeCode>
                    </cac:TaxScheme>
                </cac:TaxCategory>
            </cac:TaxSubtotal>
        </cac:TaxTotal>
        </#if>
        <#if item.comprobante.impuestos.icb??>
        <cac:TaxTotal>
            <cbc:TaxAmount currencyID="${moneda}">${item.comprobante.impuestos.icb.importe}</cbc:TaxAmount>
            <cac:TaxSubtotal>
                <cbc:TaxAmount currencyID="${moneda}">${item.comprobante.impuestos.icb.importe}</cbc:TaxAmount>
                <cac:TaxCategory>
                    <cac:TaxScheme>
                        <cbc:ID>${item.comprobante.impuestos.icb.categoria.code}</cbc:ID>
                        <cbc:Name>${item.comprobante.impuestos.icb.categoria.nombre}</cbc:Name>
                        <cbc:TaxTypeCode>${item.comprobante.impuestos.icb.categoria.tipo}</cbc:TaxTypeCode>
                    </cac:TaxScheme>
                </cac:TaxCategory>
            </cac:TaxSubtotal>
        </cac:TaxTotal>
        </#if>
    </sac:SummaryDocumentsLine>
    </#list>
</SummaryDocuments>
</@compress>
