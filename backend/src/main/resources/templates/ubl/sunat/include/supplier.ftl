    <cac:AccountingSupplierParty>
        <cbc:CustomerAssignedAccountID>{proveedor.ruc}</cbc:CustomerAssignedAccountID>
        <cbc:AdditionalAccountID>6</cbc:AdditionalAccountID>
        <cac:Party>
            <#if proveedor.nombreComercial??>
                <cac:PartyName>
                    <cbc:Name><![CDATA[{proveedor.nombreComercial}]]></cbc:Name>
                </cac:PartyName>
            </#if>
            <#if proveedor.codigoPostal??>
                <cac:PostalAddress>
                    <cbc:ID>{proveedor.codigoPostal}</cbc:ID>
                </cac:PostalAddress>
            </#if>
            <cac:PartyLegalEntity>
                <cbc:RegistrationName><![CDATA[{proveedor.razonSocial}]]></cbc:RegistrationName>
            </cac:PartyLegalEntity>
        </cac:Party>
    </cac:AccountingSupplierParty>
