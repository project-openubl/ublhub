<@compress single_line=true>
<#setting number_format="computer">
<?xml version="1.0" encoding="ISO-8859-1"?>
<Perception xmlns="urn:sunat:names:specification:ubl:peru:schema:xsd:Perception-1"
            xmlns:cac="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2"
            xmlns:cbc="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2"
            xmlns:ccts="urn:un:unece:uncefact:documentation:2"
            xmlns:ds="http://www.w3.org/2000/09/xmldsig#"
            xmlns:ext="urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2"
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
    <cbc:CustomizationID>1.0</cbc:CustomizationID>
    <#include "../signature.ftl">
    <cbc:ID>${serieNumero}</cbc:ID>
    <cbc:IssueDate>${fechaEmision}</cbc:IssueDate>
    <#include "common/agent-party.ftl">
    <#include "common/receiver-party.ftl">
    <sac:SUNATPerceptionSystemCode>${regimen.code}</sac:SUNATPerceptionSystemCode>
    <sac:SUNATPerceptionPercent>${regimen.percent}</sac:SUNATPerceptionPercent>
    <#if observacion??>
    <cbc:Note><![CDATA[${observacion}]]></cbc:Note>
    </#if>
    <cbc:TotalInvoiceAmount currencyID="${moneda}">${importeTotalPercibidoRetenido}</cbc:TotalInvoiceAmount>
    <sac:SUNATTotalCashed currencyID="${moneda}">${importeTotalCobradoPagado}</sac:SUNATTotalCashed>
    <#list detalle as item>
    <sac:SUNATPerceptionDocumentReference>
        <cbc:ID schemeID="${item.tipoComprobante.code}">${item.serieNumeroComprobante}</cbc:ID>
        <cbc:IssueDate>${item.fechaEmisionComprobante}</cbc:IssueDate>
        <cbc:TotalInvoiceAmount currencyID="${item.monedaComprobante}">${item.importeTotalComprobante}</cbc:TotalInvoiceAmount>
        <cac:Payment>
            <cbc:ID>${item.numeroCobroPago}</cbc:ID>
            <cbc:PaidAmount currencyID="${item.monedaComprobante}">${item.importeCobroPago}</cbc:PaidAmount>
            <cbc:PaidDate>${item.fechaCobroPago}</cbc:PaidDate>
        </cac:Payment>
        <sac:SUNATPerceptionInformation>
            <sac:SUNATPerceptionAmount currencyID="${moneda}">${item.importePercibidoRetenido}</sac:SUNATPerceptionAmount>
            <sac:SUNATPerceptionDate>${fechaEmision}</sac:SUNATPerceptionDate>
            <sac:SUNATNetTotalCashed currencyID="${moneda}">${item.importeTotalCobradoPagado}</sac:SUNATNetTotalCashed>
        </sac:SUNATPerceptionInformation>
    </sac:SUNATPerceptionDocumentReference>
    </#list>
</Perception>
</@compress>
