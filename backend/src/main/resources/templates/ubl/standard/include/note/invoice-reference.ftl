    <cac:DiscrepancyResponse>
        <cbc:ReferenceID>${serieNumeroComprobanteAfectado}</cbc:ReferenceID>
        <cbc:ResponseCode>${tipoDocumentoComprobanteAfectado.code}</cbc:ResponseCode>
        <cbc:Description><![CDATA[${descripcionSustentoDeNota}]]></cbc:Description>
    </cac:DiscrepancyResponse>
<#--    <#if orderCompra??>-->
<#--        <cac:OrderReference>-->
<#--            <cbc:ID>${orderCompra}</cbc:ID>-->
<#--        </cac:OrderReference>-->
<#--    </#if>-->
    <cac:BillingReference>
        <cac:InvoiceDocumentReference>
            <cbc:ID>${serieNumeroComprobanteAfectado}</cbc:ID>
            <cbc:DocumentTypeCode listAgencyName="PE:SUNAT" listName="Tipo de Documento" listURI="urn:pe:gob:sunat:cpe:see:gem:catalogos:catalogo01">${tipoDocumentoComprobanteAfectado.code}</cbc:DocumentTypeCode>
        </cac:InvoiceDocumentReference>
    </cac:BillingReference>
