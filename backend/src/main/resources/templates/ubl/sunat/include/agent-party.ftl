    <cac:AgentParty>
        <cac:PartyIdentification>
            <cbc:ID schemeID="6">${proveedor.ruc}</cbc:ID>
        </cac:PartyIdentification>
        <#if proveedor.nombreComercial??>
        <cac:PartyName>
            <cbc:Name><![CDATA[${proveedor.nombreComercial}]]></cbc:Name>
        </cac:PartyName>
        </#if>
        <#if proveedor.codigoPostal??>
        <cac:PostalAddress>
            <cbc:ID>${proveedor.codigoPostal}</cbc:ID>
        </cac:PostalAddress>
        </#if>
        <cac:PartyLegalEntity>
            <cbc:RegistrationName><![CDATA[${proveedor.razonSocial}]]></cbc:RegistrationName>
        </cac:PartyLegalEntity>
    </cac:AgentParty>
