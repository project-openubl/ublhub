package io.github.project.openubl.xsender.sender;

import io.github.project.openubl.xmlsenderws.webservices.providers.BillServiceModel;
import io.github.project.openubl.xmlsenderws.webservices.xml.XmlContentModel;
import io.github.project.openubl.xsender.exceptions.NoCompanyWithRucException;
import io.github.project.openubl.xsender.exceptions.ReadFileException;
import io.github.project.openubl.xsender.exceptions.SendFileToSUNATException;
import io.github.project.openubl.xsender.models.jpa.entities.SunatCredentialsEntity;
import io.github.project.openubl.xsender.models.jpa.entities.SunatUrlsEntity;
import io.github.project.openubl.xsender.resources.config.ArtemisServer;
import io.github.project.openubl.xsender.resources.config.KeycloakServer;
import io.github.project.openubl.xsender.resources.config.MinioServer;
import io.github.project.openubl.xsender.resources.config.PostgreSQLServer;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(KeycloakServer.class)
@QuarkusTestResource(MinioServer.class)
@QuarkusTestResource(ArtemisServer.class)
@QuarkusTestResource(PostgreSQLServer.class)
public class XSenderMutinyTest {

    @Inject
    XSenderMutiny xSenderMutiny;

    @Test
    public void getFileContentOf_nullFile() {
        // Given
        byte[] file = null;

        // When
        UniAssertSubscriber<XmlContentModel> subscriber = xSenderMutiny.getFileContent(file).subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertFailedWith(ReadFileException.class, null);
    }

    @Test
    public void getFileContentOf_invalidXMLFile() throws URISyntaxException, IOException {
        // Given
        URI uri = XSenderMutinyTest.class.getClassLoader().getResource("xml/maven.xml").toURI();
        byte[] file = Files.readAllBytes(Paths.get(uri));

        // When
        UniAssertSubscriber<XmlContentModel> subscriber = xSenderMutiny.getFileContent(file).subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertFailedWith(ReadFileException.class, null);
    }

    @Test
    public void getFileContentOf_validXMLFile() throws URISyntaxException, IOException {
        // Given
        URI uri = XSenderMutinyTest.class.getClassLoader().getResource("xml/invoice_11111111111.xml").toURI();
        byte[] file = Files.readAllBytes(Paths.get(uri));

        // When
        UniAssertSubscriber<XmlContentModel> subscriber = xSenderMutiny.getFileContent(file).subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted();
    }

//    @Test
//    public void getXSenderRequiredData_namespaceNotFound() {
//        // Given
//        String namespaceId = "9";
//        String ruc = "1111111111";
//
//        // When
//        UniAssertSubscriber<XSenderRequiredData> subscriber = xSenderMutiny.getXSenderRequiredData(namespaceId, ruc).subscribe().withSubscriber(UniAssertSubscriber.create());
//
//        // Then
//        subscriber.assertFailedWith(NoCompanyWithRucException.class, "No company with ruc found");
//    }

    @Test
    public void sendFile_invalidUrls() throws URISyntaxException, IOException {
        // Given
        URI uri = XSenderMutinyTest.class.getClassLoader().getResource("xml/invoice_11111111111.xml").toURI();
        byte[] file = Files.readAllBytes(Paths.get(uri));

        SunatUrlsEntity urls = new SunatUrlsEntity();
        urls.sunatUrlFactura = "http://url1.com";
        urls.sunatUrlGuiaRemision = "http://url1.com";
        urls.sunatUrlPercepcionRetencion = "http://url1.com";

        SunatCredentialsEntity credentials = new SunatCredentialsEntity();
        credentials.sunatUsername = "MODDATOS11111111111";
        credentials.sunatPassword = "MODDATOS";

        XSenderRequiredData xsenderConfig = new XSenderRequiredData(urls, credentials);

        // When
        UniAssertSubscriber<BillServiceModel> subscriber = xSenderMutiny.sendFile(file, xsenderConfig).subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertFailedWith(SendFileToSUNATException.class, "Could not send file");
    }

    @Test
    public void sendFile_validUrls() throws URISyntaxException, IOException {
        // Given
        URI uri = XSenderMutinyTest.class.getClassLoader().getResource("xml/invoice_alterado_12345678912.xml").toURI();
        byte[] file = Files.readAllBytes(Paths.get(uri));

        SunatUrlsEntity urls = new SunatUrlsEntity();
        urls.sunatUrlFactura = "https://e-beta.sunat.gob.pe/ol-ti-itcpfegem-beta/billService";
        urls.sunatUrlGuiaRemision = "https://e-beta.sunat.gob.pe/ol-ti-itemision-otroscpe-gem-beta/billService";
        urls.sunatUrlPercepcionRetencion = "https://e-beta.sunat.gob.pe/ol-ti-itemision-guia-gem-beta/billService";

        SunatCredentialsEntity credentials = new SunatCredentialsEntity();
        credentials.sunatUsername = "MODDATOS11111111111";
        credentials.sunatPassword = "MODDATOS";

        XSenderRequiredData xsenderConfig = new XSenderRequiredData(urls, credentials);

        // When
        UniAssertSubscriber<BillServiceModel> subscriber = xSenderMutiny.sendFile(file, xsenderConfig).subscribe().withSubscriber(UniAssertSubscriber.create());

        // Then
        subscriber.assertCompleted();
    }

    @Test
    public void verifyTicket() {
    }
}
