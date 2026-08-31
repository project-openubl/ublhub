/*
 * Copyright 2019 Project OpenUBL, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package io.github.project.openubl.ublhub.documents;

import io.github.project.openubl.xbuilder.signature.XmlSignatureHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.json.JsonArray;
import javax.json.Json;
import javax.json.JsonObject;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xml.sax.SAXException;

final class GreShipmentXmlEnhancer {

    static final String CAC = "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2";
    static final String CBC = "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2";

    private GreShipmentXmlEnhancer() {
    }

    record PackageData(String numero, String precinto) {
    }

    static List<PackageData> packagesFrom(JsonObject document) {
        if (document == null) {
            return Collections.emptyList();
        }
        JsonObject envio = document.getJsonObject("envio");
        if (envio == null) {
            return Collections.emptyList();
        }
        JsonArray containers = envio.getJsonArray("contenedores");
        if (containers == null) {
            return Collections.emptyList();
        }

        List<PackageData> result = new ArrayList<>();
        for (int index = 0; index < containers.size(); index++) {
            JsonObject container = containers.getJsonObject(index);
            String numero = trimmed(container.getString("numero", null));
            String precinto = trimmed(container.getString("precinto", null));
            if (numero != null || precinto != null) {
                result.add(new PackageData(numero, precinto));
            }
        }
        return List.copyOf(result);
    }

    static JsonObject withoutPackages(JsonObject document) {
        JsonObject envio = document.getJsonObject("envio");
        if (envio == null || !envio.containsKey("contenedores")) {
            return document;
        }
        JsonObject envioWithoutPackages = Json.createObjectBuilder(envio)
                .remove("contenedores")
                .build();
        return Json.createObjectBuilder(document)
                .add("envio", envioWithoutPackages)
                .build();
    }

    static String enhance(String xml, List<PackageData> packages)
            throws ParserConfigurationException, IOException, SAXException, TransformerException {
        Document document = XmlSignatureHelper.convertStringToXMLDocument(xml);
        migrateItemDescriptions(document);
        NodeList shipments = document.getElementsByTagNameNS(CAC, "Shipment");
        if (shipments.getLength() == 0) {
            return xml;
        }

        Element shipment = (Element) shipments.item(0);
        normalizeShipmentId(shipment);
        if (packages != null && !packages.isEmpty()) {
            removeDirectChildren(shipment, CAC, "TransportHandlingUnit");
            for (PackageData packageData : packages) {
                shipment.insertBefore(createHandlingUnit(document, packageData), originAddress(shipment));
            }
        } else {
            migrateLegacyTransportEquipment(document, shipment);
        }
        return serialize(document);
    }

    private static void normalizeShipmentId(Element shipment) {
        for (Element id : directChildren(shipment, CBC, "ID")) {
            id.setTextContent("SUNAT_Envio");
        }
    }

    private static void migrateItemDescriptions(Document document) {
        NodeList despatchLines = document.getElementsByTagNameNS(CAC, "DespatchLine");
        for (int index = 0; index < despatchLines.getLength(); index++) {
            Element despatchLine = (Element) despatchLines.item(index);
            for (Element item : directChildren(despatchLine, CAC, "Item")) {
                for (Element name : directChildren(item, CBC, "Name")) {
                    Element description = document.createElementNS(CBC, "cbc:Description");
                    description.setTextContent(name.getTextContent());
                    item.replaceChild(description, name);
                }
            }
        }
    }

    private static void migrateLegacyTransportEquipment(Document document, Element shipment) {
        for (Element handlingUnit : directChildren(shipment, CAC, "TransportHandlingUnit")) {
            List<Element> equipment = directChildren(handlingUnit, CAC, "TransportEquipment");
            for (Element oldEquipment : equipment) {
                Element packageElement = document.createElementNS(CAC, "cac:Package");
                for (Element id : directChildren(oldEquipment, CBC, "ID")) {
                    Element packageId = document.createElementNS(CBC, "cbc:ID");
                    packageId.setTextContent(id.getTextContent());
                    packageElement.appendChild(packageId);
                }
                handlingUnit.replaceChild(packageElement, oldEquipment);
            }
        }
    }

    private static Element createHandlingUnit(Document document, PackageData packageData) {
        Element handlingUnit = document.createElementNS(CAC, "cac:TransportHandlingUnit");
        Element packageElement = document.createElementNS(CAC, "cac:Package");
        if (packageData.numero() != null) {
            Element id = document.createElementNS(CBC, "cbc:ID");
            id.setTextContent(packageData.numero());
            packageElement.appendChild(id);
        }
        if (packageData.precinto() != null) {
            Element traceId = document.createElementNS(CBC, "cbc:TraceID");
            traceId.setTextContent(packageData.precinto());
            packageElement.appendChild(traceId);
        }
        handlingUnit.appendChild(packageElement);
        return handlingUnit;
    }

    private static Node originAddress(Element shipment) {
        List<Element> originAddresses = directChildren(shipment, CAC, "OriginAddress");
        return originAddresses.isEmpty() ? null : originAddresses.get(0);
    }

    private static void removeDirectChildren(Element parent, String namespace, String localName) {
        for (Element child : directChildren(parent, namespace, localName)) {
            parent.removeChild(child);
        }
    }

    private static List<Element> directChildren(Element parent, String namespace, String localName) {
        List<Element> result = new ArrayList<>();
        NodeList children = parent.getChildNodes();
        for (int index = 0; index < children.getLength(); index++) {
            Node child = children.item(index);
            if (child instanceof Element element
                    && namespace.equals(element.getNamespaceURI())
                    && localName.equals(element.getLocalName())) {
                result.add(element);
            }
        }
        return result;
    }

    private static String serialize(Document document) throws TransformerException {
        var transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        StringWriter output = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(output));
        return output.toString();
    }

    private static String trimmed(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
