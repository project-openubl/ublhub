package io.github.project.openubl.ublhub.documents;

import io.github.project.openubl.xbuilder.content.catalogs.Catalog1;
import io.github.project.openubl.xbuilder.content.catalogs.Catalog18;
import io.github.project.openubl.xbuilder.content.catalogs.Catalog20;
import io.github.project.openubl.xbuilder.content.catalogs.Catalog6;
import io.github.project.openubl.xbuilder.content.models.standard.guia.DespatchAdvice;
import io.github.project.openubl.xbuilder.content.models.standard.guia.DespatchAdviceItem;
import io.github.project.openubl.xbuilder.content.models.standard.guia.Destinatario;
import io.github.project.openubl.xbuilder.content.models.standard.guia.Destino;
import io.github.project.openubl.xbuilder.content.models.standard.guia.Envio;
import io.github.project.openubl.xbuilder.content.models.standard.guia.Partida;
import io.github.project.openubl.xbuilder.content.models.standard.guia.Remitente;
import io.github.project.openubl.xbuilder.enricher.ContentEnricher;
import io.github.project.openubl.xbuilder.enricher.config.Defaults;
import io.github.project.openubl.xbuilder.renderer.TemplateProducer;
import io.github.project.openubl.xbuilder.signature.XmlSignatureHelper;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import javax.json.Json;
import javax.json.JsonObject;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GreDespatchAdviceXmlTest {

    @Test
    void rendersLinesShipmentWeightPackagesAndSeals() throws Exception {
        DespatchAdvice input = validDespatchAdvice(null);
        String rendered = render(input);
        List<GreShipmentXmlEnhancer.PackageData> packages = List.of(
                new GreShipmentXmlEnhancer.PackageData("CONT0000001", "PRECINTO-001"),
                new GreShipmentXmlEnhancer.PackageData("CONT0000002", "PRECINTO-002")
        );

        Document xml = XmlSignatureHelper.convertStringToXMLDocument(
                GreShipmentXmlEnhancer.enhance(rendered, packages)
        );

        assertXPath(xml, "count(/da:DespatchAdvice/cac:DespatchLine)", "2");
        assertXPath(xml, "/da:DespatchAdvice/cac:DespatchLine[1]/cbc:DeliveredQuantity", "4");
        assertXPath(xml, "/da:DespatchAdvice/cac:DespatchLine[1]/cbc:DeliveredQuantity/@unitCode", "NIU");
        assertXPath(xml, "/da:DespatchAdvice/cac:DespatchLine[1]/cac:Item/cbc:Description", "Caja de suministros");
        assertXPath(xml, "count(/da:DespatchAdvice/cac:DespatchLine/cac:Item/cbc:Name)", "0");
        assertXPath(xml, "/da:DespatchAdvice/cac:DespatchLine[1]/cac:Item/cac:SellersItemIdentification/cbc:ID", "CAJA-01");
        assertXPath(xml, "/da:DespatchAdvice/cac:DespatchLine[1]/cac:Item/cac:CommodityClassification/cbc:ItemClassificationCode", "24112404");
        assertXPath(xml, "/da:DespatchAdvice/cac:Shipment/cbc:ID", "SUNAT_Envio");
        assertXPath(xml, "/da:DespatchAdvice/cac:Shipment/cbc:GrossWeightMeasure", "125.500");
        assertXPath(xml, "/da:DespatchAdvice/cac:Shipment/cbc:GrossWeightMeasure/@unitCode", "KGM");
        assertXPath(xml, "/da:DespatchAdvice/cac:Shipment/cbc:TotalTransportHandlingUnitQuantity", "4");
        assertXPath(xml, "count(/da:DespatchAdvice/cac:Shipment/cac:TransportHandlingUnit/cac:Package)", "2");
        assertXPath(xml, "/da:DespatchAdvice/cac:Shipment/cac:TransportHandlingUnit[1]/cac:Package/cbc:ID", "CONT0000001");
        assertXPath(xml, "/da:DespatchAdvice/cac:Shipment/cac:TransportHandlingUnit[1]/cac:Package/cbc:TraceID", "PRECINTO-001");
        assertXPath(xml, "/da:DespatchAdvice/cac:Shipment/cac:TransportHandlingUnit[2]/cac:Package/cbc:ID", "CONT0000002");
        assertXPath(xml, "/da:DespatchAdvice/cac:Shipment/cac:TransportHandlingUnit[2]/cac:Package/cbc:TraceID", "PRECINTO-002");
        assertXPath(xml, "count(/da:DespatchAdvice/cac:Shipment/cac:TransportHandlingUnit/cac:TransportEquipment)", "0");
    }

    @Test
    void keepsLegacyContainerInputButUsesCurrentSunatPackagePath() throws Exception {
        String rendered = render(validDespatchAdvice("CONT-LEGACY"));
        Document xml = XmlSignatureHelper.convertStringToXMLDocument(
                GreShipmentXmlEnhancer.enhance(rendered, List.of())
        );

        assertXPath(xml, "/da:DespatchAdvice/cac:Shipment/cac:TransportHandlingUnit/cac:Package/cbc:ID", "CONT-LEGACY");
        assertXPath(xml, "count(/da:DespatchAdvice/cac:Shipment/cac:TransportHandlingUnit/cac:TransportEquipment)", "0");
    }

    @Test
    void readsOptionalContainersFromGreJson() {
        JsonObject document = Json.createObjectBuilder()
                .add("envio", Json.createObjectBuilder()
                        .add("contenedores", Json.createArrayBuilder()
                                .add(Json.createObjectBuilder()
                                        .add("numero", " CONT0000001 ")
                                        .add("precinto", " PRECINTO-001 "))
                                .add(Json.createObjectBuilder()
                                        .add("numero", "CONT0000002"))))
                .build();

        assertEquals(List.of(
                new GreShipmentXmlEnhancer.PackageData("CONT0000001", "PRECINTO-001"),
                new GreShipmentXmlEnhancer.PackageData("CONT0000002", null)
        ), GreShipmentXmlEnhancer.packagesFrom(document));
        assertEquals(false, GreShipmentXmlEnhancer.withoutPackages(document)
                .getJsonObject("envio")
                .containsKey("contenedores"));
    }

    private static DespatchAdvice validDespatchAdvice(String legacyContainer) {
        return DespatchAdvice.builder()
                .serie("T001")
                .numero(1)
                .tipoComprobante(Catalog1.GUIA_REMISION_REMITENTE.getCode())
                .remitente(Remitente.builder()
                        .ruc("20100066603")
                        .razonSocial("Remitente S.A.C.")
                        .build())
                .destinatario(Destinatario.builder()
                        .tipoDocumentoIdentidad(Catalog6.RUC.getCode())
                        .numeroDocumentoIdentidad("20555555551")
                        .nombre("Destinatario S.A.C.")
                        .build())
                .envio(Envio.builder()
                        .tipoTraslado(Catalog20.VENTA.getCode())
                        .pesoTotal(new BigDecimal("125.5"))
                        .pesoTotalUnidadMedida("KGM")
                        .numeroDeBultos(4)
                        .transbordoProgramado(false)
                        .tipoModalidadTraslado(Catalog18.TRANSPORTE_PRIVADO.getCode())
                        .fechaTraslado(LocalDate.of(2026, 8, 31))
                        .numeroDeContenedor(legacyContainer)
                        .partida(Partida.builder().direccion("Origen").ubigeo("150101").build())
                        .destino(Destino.builder().direccion("Destino").ubigeo("150122").build())
                        .build())
                .detalle(DespatchAdviceItem.builder()
                        .cantidad(new BigDecimal("4"))
                        .unidadMedida("NIU")
                        .descripcion("Caja de suministros")
                        .codigo("CAJA-01")
                        .codigoSunat("24112404")
                        .build())
                .detalle(DespatchAdviceItem.builder()
                        .cantidad(new BigDecimal("2"))
                        .unidadMedida("NIU")
                        .descripcion("Pallet de insumos")
                        .codigo("PALLET-02")
                        .build())
                .build();
    }

    private static String render(DespatchAdvice input) {
        Defaults defaults = Defaults.builder()
                .icbTasa(new BigDecimal("0.2"))
                .igvTasa(new BigDecimal("0.18"))
                .ivapTasa(new BigDecimal("0.04"))
                .build();
        new ContentEnricher(defaults, () -> LocalDate.of(2026, 8, 31)).enrich(input);
        return TemplateProducer.getInstance().getDespatchAdvice().data(input).render();
    }

    private static void assertXPath(Document document, String expression, String expected) throws Exception {
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(new UblNamespaces());
        String actual = (String) xpath.evaluate(expression, document, XPathConstants.STRING);
        assertEquals(expected, actual);
    }

    private static final class UblNamespaces implements NamespaceContext {
        @Override
        public String getNamespaceURI(String prefix) {
            return switch (prefix) {
                case "da" -> "urn:oasis:names:specification:ubl:schema:xsd:DespatchAdvice-2";
                case "cac" -> GreShipmentXmlEnhancer.CAC;
                case "cbc" -> GreShipmentXmlEnhancer.CBC;
                default -> "";
            };
        }

        @Override
        public String getPrefix(String namespaceURI) {
            return null;
        }

        @Override
        public Iterator<String> getPrefixes(String namespaceURI) {
            return List.<String>of().iterator();
        }
    }
}
