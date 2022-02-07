<@compress single_line=true>
<#setting number_format="computer">
<?xml version="1.0" encoding="ISO-8859-1"?>
<CreditNote xmlns="urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2"
        <#include "common/namespaces.ftl">
>
    <#include "common/ubl-extensions.ftl">
    <#include "common/general-data.ftl">
<#--    <#include "./common/legends.ftl">-->
    <cbc:DocumentCurrencyCode listID="ISO 4217 Alpha" listAgencyName="United Nations Economic Commission for Europe" listName="Currency">${moneda}</cbc:DocumentCurrencyCode>
    <#include "common/note/invoice-reference.ftl">
    <#include "common/despatch-document-reference.ftl">
    <#include "common/additional-document-reference.ftl">
    <#include "../signature.ftl">
    <#include "common/supplier.ftl">
    <#include "common/customer.ftl">
    <#if formaPago.cuotas?has_content>
    <cac:PaymentTerms>
        <cbc:ID>FormaPago</cbc:ID>
        <cbc:PaymentMeansID>${formaPago.tipo}</cbc:PaymentMeansID>
        <cbc:Amount currencyID="${moneda}">${formaPago.montoTotal}</cbc:Amount>
    </cac:PaymentTerms>
    </#if>
    <#list formaPago.cuotas as item>
    <cac:PaymentTerms>
        <cbc:ID>FormaPago</cbc:ID>
        <cbc:PaymentMeansID>Cuota${item.id}</cbc:PaymentMeansID>
        <cbc:Amount currencyID="${moneda}">${item.monto}</cbc:Amount>
        <cbc:PaymentDueDate>${item.fechaPago}</cbc:PaymentDueDate>
    </cac:PaymentTerms>
    </#list>
    <#include "common/tax-total.ftl">
    <cac:LegalMonetaryTotal>
    <#include "common/monetary-total.ftl">
    </cac:LegalMonetaryTotal>
    <#list detalle as item>
    <cac:CreditNoteLine>
        <cbc:ID>${item?index + 1}</cbc:ID>
        <cbc:CreditedQuantity unitCode="${item.unidadMedida}" unitCodeListAgencyName="United Nations Economic Commission for Europe" unitCodeListID="UN/ECE rec 20">${item.cantidad}</cbc:CreditedQuantity>
        <#include "common/document-line.ftl">
    </cac:CreditNoteLine>
    </#list>
</CreditNote>
</@compress>
