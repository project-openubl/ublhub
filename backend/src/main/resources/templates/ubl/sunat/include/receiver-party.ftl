    <cac:ReceiverParty>
        <cac:PartyIdentification>
            <cbc:ID schemeID="${cliente.tipoDocumentoIdentidad.code}">${cliente.numeroDocumentoIdentidad}</cbc:ID>
        </cac:PartyIdentification>
<#--        <cac:PartyName>-->
<#--            <cbc:Name>nombreComercial</cbc:Name>-->
<#--        </cac:PartyName>-->
        <cac:PartyLegalEntity>
            <cbc:RegistrationName><![CDATA[${cliente.nombre}]]></cbc:RegistrationName>
        </cac:PartyLegalEntity>
        <cac:Contact/>
    </cac:ReceiverParty>
