<@compress single_line=true>
<#setting number_format="computer">
<?xml version="1.0" encoding="ISO-8859-1"?>
<DebitNote xmlns="urn:oasis:names:specification:ubl:schema:xsd:DebitNote-2"
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
    <#include "common/tax-total.ftl">
    <cac:RequestedMonetaryTotal>
    <#include "common/monetary-total.ftl">
    </cac:RequestedMonetaryTotal>
    <#list detalle as item>
    <cac:DebitNoteLine>
        <cbc:ID>${item?index + 1}</cbc:ID>
        <cbc:DebitedQuantity unitCode="${item.unidadMedida}" unitCodeListAgencyName="United Nations Economic Commission for Europe" unitCodeListID="UN/ECE rec 20">${item.cantidad}</cbc:DebitedQuantity>
        <#include "common/document-line.ftl">
    </cac:DebitNoteLine>
    </#list>
</DebitNote>
</@compress>
