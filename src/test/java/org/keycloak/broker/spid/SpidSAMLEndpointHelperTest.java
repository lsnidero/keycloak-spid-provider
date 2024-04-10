package org.keycloak.broker.spid;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.dom.saml.v2.assertion.AssertionType;
import org.keycloak.dom.saml.v2.assertion.NameIDType;
import org.keycloak.dom.saml.v2.protocol.ResponseType;
import org.keycloak.dom.saml.v2.protocol.StatusCodeType;
import org.keycloak.dom.saml.v2.protocol.StatusDetailType;
import org.keycloak.dom.saml.v2.protocol.StatusType;
import org.keycloak.saml.common.exceptions.ConfigurationException;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.exceptions.ProcessingException;
import org.keycloak.saml.common.util.DocumentUtil;
import org.keycloak.saml.common.util.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpidSAMLEndpointHelperTest {

    SpidSAMLEndpointHelper helper;

    Document samlResponse;
    Element samlAssertion;

    static XMLGregorianCalendar refTime;

    @BeforeAll
    static void loadSamlResponse() throws DatatypeConfigurationException {

        refTime = DatatypeFactory.newInstance().newXMLGregorianCalendar("2024-04-10T09:22:28.065Z");

    }

    @BeforeEach
    void init() throws IOException, ConfigurationException, ParsingException, ProcessingException {
        SpidIdentityProviderConfig config = new SpidIdentityProviderConfig();
        config.setIdpEntityId("https://id.lepida.it/idp/shibboleth");
        helper = new SpidSAMLEndpointHelper(config);

        samlResponse = DocumentUtil.getDocument(Files.newInputStream(Path.of("src/test/resources/saml_response.xml")));
        samlAssertion = DocumentUtil.getElement(samlResponse, new QName("Assertion"));

    }


    @Test
    void raiseSpidSamlCheck02() throws DatatypeConfigurationException {
        // Given
        String expected = "SpidSamlCheck_02";
        ResponseType responseType = new ResponseType("id", DatatypeFactory.newInstance().newXMLGregorianCalendar());
        responseType.setSignature(null);
        // When
        String actual = helper.verifySpidResponse(null, null, null, responseType, null, null);
        // Then
        assertEquals(expected, actual);
    }

    @Test
    void raiseSpidSamlCheck03() throws DatatypeConfigurationException, ConfigurationException, ProcessingException, IOException, ParsingException {

        // Given
        String expectedError = "SpidSamlCheck_03";
        ResponseType responseType = new ResponseType("id", DatatypeFactory.newInstance().newXMLGregorianCalendar());
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", DatatypeFactory.newInstance().newXMLGregorianCalendar())));
        responseType.getAssertions().get(0).getAssertion().setSignature(null);

        // When
        String actualError = helper.verifySpidResponse(null, null, null, responseType, null, null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck08() throws DatatypeConfigurationException {
        String expectedError = "SpidSamlCheck_08";

        ResponseType responseType = new ResponseType("", refTime);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", refTime)));

        Element assertionSignature = DocumentUtil.getChildElement(samlAssertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);

        // When
        String actualError = helper.verifySpidResponse(null, null, null, responseType, null, null);

        // Then
        assertEquals(expectedError, actualError);
    }


    @Test
    void raiseSpidSamlCheck14() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_14";

        ResponseType responseType = new ResponseType("id", refTime);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", refTime)));

        Element assertionSignature = DocumentUtil.getChildElement(samlAssertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);

        // When
        String actualError = helper.verifySpidResponse(null, null, null, responseType, "2024-04-10T09:22:28.065Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck15() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_15";
        String requestMoreThan3MinutesBefore = "2024-04-10T09:18:28.065Z";

        ResponseType responseType = new ResponseType("id", refTime);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", refTime)));

        Element assertionSignature = DocumentUtil.getChildElement(samlAssertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);

        // When
        String actualError = helper.verifySpidResponse(null, null, null, responseType, requestMoreThan3MinutesBefore, null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck15Future() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_15";
        String requestTimeInRange = "2124-04-10T09:20:28.065Z";

        XMLGregorianCalendar responseTimeInFuture = DatatypeFactory.newInstance().newXMLGregorianCalendar("2124-04-10T09:22:28.065Z");

        ResponseType responseType = new ResponseType("id", responseTimeInFuture);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", responseTimeInFuture)));


        Element assertionSignature = DocumentUtil.getChildElement(samlAssertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);


        // When
        String actualError = helper.verifySpidResponse(null, null, null, responseType, requestTimeInRange, null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck110() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_110";

        XMLGregorianCalendar responseTimeWithMs = DatatypeFactory.newInstance().newXMLGregorianCalendar("2024-04-10T09:22:28.065Z");
        responseTimeWithMs.setMillisecond(999);


        ResponseType responseType = new ResponseType("id", responseTimeWithMs);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", responseTimeWithMs)));

        Element assertion = DocumentUtil.getElement(samlResponse, new QName("Assertion"));
        Element assertionSignature = DocumentUtil.getChildElement(assertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);


        // When
        String actualError = helper.verifySpidResponse(null, null, null, responseType, "2024-04-10T09:22:28.065Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }


    @Test
    void raiseSpidSamlCheck17() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_nr17";

        XMLGregorianCalendar responseTimeWithoutMs = DatatypeFactory.newInstance().newXMLGregorianCalendar("2024-04-10T09:23:28.000Z");
        responseTimeWithoutMs.setMillisecond(0);


        ResponseType responseType = new ResponseType("id", responseTimeWithoutMs);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", responseTimeWithoutMs)));

        Element assertionSignature = DocumentUtil.getChildElement(samlAssertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);

        samlResponse.getDocumentElement().removeAttribute("InResponseTo");

        // When
        String actualError = helper.verifySpidResponse(samlResponse.getDocumentElement(), null, null, responseType, "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck16() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_nr16";

        XMLGregorianCalendar responseTimeWithoutMs = DatatypeFactory.newInstance().newXMLGregorianCalendar("2024-04-10T09:23:28.000Z");
        responseTimeWithoutMs.setMillisecond(0);


        ResponseType responseType = new ResponseType("id", responseTimeWithoutMs);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", responseTimeWithoutMs)));

        Element assertionSignature = DocumentUtil.getChildElement(samlAssertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);

        samlResponse.getDocumentElement().setAttribute("InResponseTo", "");

        // When
        String actualError = helper.verifySpidResponse(samlResponse.getDocumentElement(), null, null, responseType, "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck18() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_nr18";

        XMLGregorianCalendar responseTimeWithoutMs = DatatypeFactory.newInstance().newXMLGregorianCalendar("2024-04-10T09:23:28.000Z");
        responseTimeWithoutMs.setMillisecond(0);


        ResponseType responseType = new ResponseType("id", responseTimeWithoutMs);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", responseTimeWithoutMs)));

        Element assertionSignature = DocumentUtil.getChildElement(samlAssertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);

        // When
        String actualError = helper.verifySpidResponse(samlResponse.getDocumentElement(), null, "non-expected-in-response-to", responseType, "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck22() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_22";

        XMLGregorianCalendar responseTimeWithoutMs = DatatypeFactory.newInstance().newXMLGregorianCalendar("2024-04-10T09:23:28.000Z");
        responseTimeWithoutMs.setMillisecond(0);


        ResponseType responseType = new ResponseType("id", responseTimeWithoutMs);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", responseTimeWithoutMs)));
        responseType.setStatus(new StatusType());

        Element assertionSignature = DocumentUtil.getChildElement(samlAssertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);

        // When
        String actualError = helper.verifySpidResponse(samlResponse.getDocumentElement(), null, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck23() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_23";

        XMLGregorianCalendar responseTimeWithoutMs = DatatypeFactory.newInstance().newXMLGregorianCalendar("2024-04-10T09:23:28.000Z");
        responseTimeWithoutMs.setMillisecond(0);


        ResponseType responseType = new ResponseType("id", responseTimeWithoutMs);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", responseTimeWithoutMs)));
        responseType.setStatus(null);

        Element assertionSignature = DocumentUtil.getChildElement(samlAssertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);

        // When
        String actualError = helper.verifySpidResponse(samlResponse.getDocumentElement(), null, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck24() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_24";

        XMLGregorianCalendar responseTimeWithoutMs = DatatypeFactory.newInstance().newXMLGregorianCalendar("2024-04-10T09:23:28.000Z");
        responseTimeWithoutMs.setMillisecond(0);


        ResponseType responseType = new ResponseType("id", responseTimeWithoutMs);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", responseTimeWithoutMs)));
        StatusType statusType = new StatusType();
        StatusCodeType statusCodeType = new StatusCodeType();
        statusCodeType.setValue(URI.create(""));
        statusType.setStatusCode(statusCodeType);
        responseType.setStatus(statusType);

        Element assertionSignature = DocumentUtil.getChildElement(samlAssertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);

        // When
        String actualError = helper.verifySpidResponse(samlResponse.getDocumentElement(), null, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck25() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_25";

        XMLGregorianCalendar responseTimeWithoutMs = DatatypeFactory.newInstance().newXMLGregorianCalendar("2024-04-10T09:23:28.000Z");
        responseTimeWithoutMs.setMillisecond(0);


        ResponseType responseType = new ResponseType("id", responseTimeWithoutMs);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", responseTimeWithoutMs)));
        StatusType statusType = new StatusType();
        statusType.setStatusCode(null);
        statusType.setStatusDetail(new StatusDetailType());
        statusType.setStatusMessage("not null");
        responseType.setStatus(statusType);

        Element assertionSignature = DocumentUtil.getChildElement(samlAssertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);

        // When
        String actualError = helper.verifySpidResponse(samlResponse.getDocumentElement(), null, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck26() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_26";

        XMLGregorianCalendar responseTimeWithoutMs = DatatypeFactory.newInstance().newXMLGregorianCalendar("2024-04-10T09:23:28.000Z");
        responseTimeWithoutMs.setMillisecond(0);


        ResponseType responseType = new ResponseType("id", responseTimeWithoutMs);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", responseTimeWithoutMs)));
        StatusType statusType = new StatusType();
        StatusCodeType statusCodeType = new StatusCodeType();
        statusCodeType.setValue(URI.create("uri:not:valid"));
        statusType.setStatusCode(statusCodeType);
        responseType.setStatus(statusType);

        Element assertionSignature = DocumentUtil.getChildElement(samlAssertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);

        // When
        String actualError = helper.verifySpidResponse(samlResponse.getDocumentElement(), null, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck27() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_27";

        XMLGregorianCalendar responseTimeWithoutMs = DatatypeFactory.newInstance().newXMLGregorianCalendar("2024-04-10T09:23:28.000Z");
        responseTimeWithoutMs.setMillisecond(0);


        ResponseType responseType = new ResponseType("id", responseTimeWithoutMs);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", responseTimeWithoutMs)));
        StatusType statusType = new StatusType();
        StatusCodeType statusCodeType = new StatusCodeType();
        statusCodeType.setValue(URI.create("urn:oasis:names:tc:SAML:2.0:status:Success"));
        statusType.setStatusCode(statusCodeType);
        responseType.setStatus(statusType);
        responseType.setIssuer(new NameIDType());

        Element assertionSignature = DocumentUtil.getChildElement(samlAssertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);

        // When
        String actualError = helper.verifySpidResponse(samlResponse.getDocumentElement(), null, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck28() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_28";

        XMLGregorianCalendar responseTimeWithoutMs = DatatypeFactory.newInstance().newXMLGregorianCalendar("2024-04-10T09:23:28.000Z");
        responseTimeWithoutMs.setMillisecond(0);


        ResponseType responseType = new ResponseType("id", responseTimeWithoutMs);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", responseTimeWithoutMs)));
        StatusType statusType = new StatusType();
        StatusCodeType statusCodeType = new StatusCodeType();
        statusCodeType.setValue(URI.create("urn:oasis:names:tc:SAML:2.0:status:Success"));
        statusType.setStatusCode(statusCodeType);
        responseType.setStatus(statusType);
        responseType.setIssuer(null);

        Element assertionSignature = DocumentUtil.getChildElement(samlAssertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);

        // When
        String actualError = helper.verifySpidResponse(samlResponse.getDocumentElement(), null, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck29() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_29";

        XMLGregorianCalendar responseTimeWithoutMs = DatatypeFactory.newInstance().newXMLGregorianCalendar("2024-04-10T09:23:28.000Z");
        responseTimeWithoutMs.setMillisecond(0);


        ResponseType responseType = new ResponseType("id", responseTimeWithoutMs);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", responseTimeWithoutMs)));
        StatusType statusType = new StatusType();
        StatusCodeType statusCodeType = new StatusCodeType();
        statusCodeType.setValue(URI.create("urn:oasis:names:tc:SAML:2.0:status:Success"));
        statusType.setStatusCode(statusCodeType);
        responseType.setStatus(statusType);
        NameIDType issuer = new NameIDType();
        issuer.setValue("wrong issuer");
        responseType.setIssuer(issuer);

        Element assertionSignature = DocumentUtil.getChildElement(samlAssertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);

        // When
        String actualError = helper.verifySpidResponse(samlResponse.getDocumentElement(), null, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck30() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_30";

        XMLGregorianCalendar responseTimeWithoutMs = DatatypeFactory.newInstance().newXMLGregorianCalendar("2024-04-10T09:23:28.000Z");
        responseTimeWithoutMs.setMillisecond(0);


        ResponseType responseType = new ResponseType("id", responseTimeWithoutMs);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", responseTimeWithoutMs)));
        StatusType statusType = new StatusType();
        StatusCodeType statusCodeType = new StatusCodeType();
        statusCodeType.setValue(URI.create("urn:oasis:names:tc:SAML:2.0:status:Success"));
        statusType.setStatusCode(statusCodeType);
        responseType.setStatus(statusType);
        NameIDType issuer = new NameIDType();
        issuer.setValue("https://id.lepida.it/idp/shibboleth");
        issuer.setFormat(URI.create("uri:not:correct"));
        responseType.setIssuer(issuer);

        Element assertionSignature = DocumentUtil.getChildElement(samlAssertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);

        // When
        String actualError = helper.verifySpidResponse(samlResponse.getDocumentElement(), null, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck33() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_33";

        XMLGregorianCalendar responseTimeWithoutMs = DatatypeFactory.newInstance().newXMLGregorianCalendar("2024-04-10T09:23:28.000Z");
        responseTimeWithoutMs.setMillisecond(0);


        ResponseType responseType = new ResponseType("id", responseTimeWithoutMs);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", responseTimeWithoutMs)));
        StatusType statusType = new StatusType();
        StatusCodeType statusCodeType = new StatusCodeType();
        statusCodeType.setValue(URI.create("urn:oasis:names:tc:SAML:2.0:status:Success"));
        statusType.setStatusCode(statusCodeType);
        responseType.setStatus(statusType);
        NameIDType issuer = new NameIDType();
        issuer.setValue("https://id.lepida.it/idp/shibboleth");
        issuer.setFormat(URI.create(SpidSAMLEndpoint.ISSUER_FORMAT));
        responseType.setIssuer(issuer);

        Element assertionSignature = DocumentUtil.getChildElement(samlAssertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);

        samlAssertion.setAttribute("ID", "");

        // When
        String actualError = helper.verifySpidResponse(samlResponse.getDocumentElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck39() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_39";

        XMLGregorianCalendar responseTimeWithoutMs = DatatypeFactory.newInstance().newXMLGregorianCalendar("2024-04-10T09:23:28.000Z");
        responseTimeWithoutMs.setMillisecond(0);


        ResponseType responseType = new ResponseType("id", responseTimeWithoutMs);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", responseTimeWithoutMs)));
        StatusType statusType = new StatusType();
        StatusCodeType statusCodeType = new StatusCodeType();
        statusCodeType.setValue(URI.create("urn:oasis:names:tc:SAML:2.0:status:Success"));
        statusType.setStatusCode(statusCodeType);
        responseType.setStatus(statusType);
        NameIDType issuer = new NameIDType();
        issuer.setValue("https://id.lepida.it/idp/shibboleth");
        issuer.setFormat(URI.create(SpidSAMLEndpoint.ISSUER_FORMAT));
        responseType.setIssuer(issuer);

        Element assertionSignature = DocumentUtil.getChildElement(samlAssertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);

        samlAssertion.setAttribute("IssueInstant", "2024-04-10T08:22:28.065Z");

        // When
        String actualError = helper.verifySpidResponse(samlResponse.getDocumentElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck40() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_40";

        XMLGregorianCalendar responseTimeWithoutMs = DatatypeFactory.newInstance().newXMLGregorianCalendar("2024-04-10T09:23:28.000Z");
        responseTimeWithoutMs.setMillisecond(0);


        ResponseType responseType = new ResponseType("id", responseTimeWithoutMs);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", responseTimeWithoutMs)));
        StatusType statusType = new StatusType();
        StatusCodeType statusCodeType = new StatusCodeType();
        statusCodeType.setValue(URI.create("urn:oasis:names:tc:SAML:2.0:status:Success"));
        statusType.setStatusCode(statusCodeType);
        responseType.setStatus(statusType);
        NameIDType issuer = new NameIDType();
        issuer.setValue("https://id.lepida.it/idp/shibboleth");
        issuer.setFormat(URI.create(SpidSAMLEndpoint.ISSUER_FORMAT));
        responseType.setIssuer(issuer);

        Element assertionSignature = DocumentUtil.getChildElement(samlAssertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);
        // more than 3 minutes
        samlAssertion.setAttribute("IssueInstant", "2024-04-10T09:27:28.065Z");

        // When
        String actualError = helper.verifySpidResponse(samlResponse.getDocumentElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck110FromAssertion() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_110";

        XMLGregorianCalendar responseTimeWithoutMs = DatatypeFactory.newInstance().newXMLGregorianCalendar("2024-04-10T09:23:28.000Z");
        responseTimeWithoutMs.setMillisecond(0);


        ResponseType responseType = new ResponseType("id", responseTimeWithoutMs);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", responseTimeWithoutMs)));
        StatusType statusType = new StatusType();
        StatusCodeType statusCodeType = new StatusCodeType();
        statusCodeType.setValue(URI.create("urn:oasis:names:tc:SAML:2.0:status:Success"));
        statusType.setStatusCode(statusCodeType);
        responseType.setStatus(statusType);
        NameIDType issuer = new NameIDType();
        issuer.setValue("https://id.lepida.it/idp/shibboleth");
        issuer.setFormat(URI.create(SpidSAMLEndpoint.ISSUER_FORMAT));
        responseType.setIssuer(issuer);

        Element assertionSignature = DocumentUtil.getChildElement(samlAssertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);

        // When
        String actualError = helper.verifySpidResponse(samlResponse.getDocumentElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }


    @Test
    void raiseSpidSamlCheck42() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_nr42";

        XMLGregorianCalendar responseTimeWithoutMs = DatatypeFactory.newInstance().newXMLGregorianCalendar("2024-04-10T09:23:28.000Z");
        responseTimeWithoutMs.setMillisecond(0);


        ResponseType responseType = new ResponseType("id", responseTimeWithoutMs);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", responseTimeWithoutMs)));
        StatusType statusType = new StatusType();
        StatusCodeType statusCodeType = new StatusCodeType();
        statusCodeType.setValue(URI.create("urn:oasis:names:tc:SAML:2.0:status:Success"));
        statusType.setStatusCode(statusCodeType);
        responseType.setStatus(statusType);
        NameIDType issuer = new NameIDType();
        issuer.setValue("https://id.lepida.it/idp/shibboleth");
        issuer.setFormat(URI.create(SpidSAMLEndpoint.ISSUER_FORMAT));
        responseType.setIssuer(issuer);

        Element assertionSignature = DocumentUtil.getChildElement(samlAssertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);

        samlAssertion.setAttribute("IssueInstant", "2024-04-10T09:22:28.000Z");
        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        samlAssertion.removeChild(subject);

        // When
        String actualError = helper.verifySpidResponse(samlResponse.getDocumentElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck41() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_nr41";

        XMLGregorianCalendar responseTimeWithoutMs = DatatypeFactory.newInstance().newXMLGregorianCalendar("2024-04-10T09:23:28.000Z");
        responseTimeWithoutMs.setMillisecond(0);


        ResponseType responseType = new ResponseType("id", responseTimeWithoutMs);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", responseTimeWithoutMs)));
        StatusType statusType = new StatusType();
        StatusCodeType statusCodeType = new StatusCodeType();
        statusCodeType.setValue(URI.create("urn:oasis:names:tc:SAML:2.0:status:Success"));
        statusType.setStatusCode(statusCodeType);
        responseType.setStatus(statusType);
        NameIDType issuer = new NameIDType();
        issuer.setValue("https://id.lepida.it/idp/shibboleth");
        issuer.setFormat(URI.create(SpidSAMLEndpoint.ISSUER_FORMAT));
        responseType.setIssuer(issuer);

        Element assertionSignature = DocumentUtil.getChildElement(samlAssertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);

        samlAssertion.setAttribute("IssueInstant", "2024-04-10T09:22:28.000Z");

        // Remove Subject
        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        subject.getParentNode().removeChild(subject);
        // Add empty Subject
        samlAssertion.appendChild(samlAssertion.getOwnerDocument().createElementNS("saml2", "Subject"));

        // When
        String actualError = helper.verifySpidResponse(samlResponse.getDocumentElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck44() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_44";

        XMLGregorianCalendar responseTimeWithoutMs = DatatypeFactory.newInstance().newXMLGregorianCalendar("2024-04-10T09:23:28.000Z");
        responseTimeWithoutMs.setMillisecond(0);


        ResponseType responseType = new ResponseType("id", responseTimeWithoutMs);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", responseTimeWithoutMs)));
        StatusType statusType = new StatusType();
        StatusCodeType statusCodeType = new StatusCodeType();
        statusCodeType.setValue(URI.create("urn:oasis:names:tc:SAML:2.0:status:Success"));
        statusType.setStatusCode(statusCodeType);
        responseType.setStatus(statusType);
        NameIDType issuer = new NameIDType();
        issuer.setValue("https://id.lepida.it/idp/shibboleth");
        issuer.setFormat(URI.create(SpidSAMLEndpoint.ISSUER_FORMAT));
        responseType.setIssuer(issuer);

        Element assertionSignature = DocumentUtil.getChildElement(samlAssertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);

        samlAssertion.setAttribute("IssueInstant", "2024-04-10T09:22:28.000Z");

        // Remove NameID
        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        for (int i = 0; i < subject.getChildNodes().getLength(); i++) {
            Node node = subject.getChildNodes().item(i);
            if (node.getNodeName().equals("saml2:NameID")) {
                node.getParentNode().removeChild(node);
            }
        }

        // When
        String actualError = helper.verifySpidResponse(samlResponse.getDocumentElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck43() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_43";

        XMLGregorianCalendar responseTimeWithoutMs = DatatypeFactory.newInstance().newXMLGregorianCalendar("2024-04-10T09:23:28.000Z");
        responseTimeWithoutMs.setMillisecond(0);


        ResponseType responseType = new ResponseType("id", responseTimeWithoutMs);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", responseTimeWithoutMs)));
        StatusType statusType = new StatusType();
        StatusCodeType statusCodeType = new StatusCodeType();
        statusCodeType.setValue(URI.create("urn:oasis:names:tc:SAML:2.0:status:Success"));
        statusType.setStatusCode(statusCodeType);
        responseType.setStatus(statusType);
        NameIDType issuer = new NameIDType();
        issuer.setValue("https://id.lepida.it/idp/shibboleth");
        issuer.setFormat(URI.create(SpidSAMLEndpoint.ISSUER_FORMAT));
        responseType.setIssuer(issuer);

        Element assertionSignature = DocumentUtil.getChildElement(samlAssertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);

        samlAssertion.setAttribute("IssueInstant", "2024-04-10T09:22:28.000Z");

        // Remove NameID
        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        for (int i = 0; i < subject.getChildNodes().getLength(); i++) {
            Node node = subject.getChildNodes().item(i);
            if (node.getNodeName().equals("saml2:NameID")) {
                node.getFirstChild().setNodeValue(" ");
            }
        }

        // When
        String actualError = helper.verifySpidResponse(samlResponse.getDocumentElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck4546() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_4546";

        XMLGregorianCalendar responseTimeWithoutMs = DatatypeFactory.newInstance().newXMLGregorianCalendar("2024-04-10T09:23:28.000Z");
        responseTimeWithoutMs.setMillisecond(0);


        ResponseType responseType = new ResponseType("id", responseTimeWithoutMs);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", responseTimeWithoutMs)));
        StatusType statusType = new StatusType();
        StatusCodeType statusCodeType = new StatusCodeType();
        statusCodeType.setValue(URI.create("urn:oasis:names:tc:SAML:2.0:status:Success"));
        statusType.setStatusCode(statusCodeType);
        responseType.setStatus(statusType);
        NameIDType issuer = new NameIDType();
        issuer.setValue("https://id.lepida.it/idp/shibboleth");
        issuer.setFormat(URI.create(SpidSAMLEndpoint.ISSUER_FORMAT));
        responseType.setIssuer(issuer);

        Element assertionSignature = DocumentUtil.getChildElement(samlAssertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);

        samlAssertion.setAttribute("IssueInstant", "2024-04-10T09:22:28.000Z");

        // Remove NameID
        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element nameID = DocumentUtil.getChildElement(subject, new QName("NameID"));
        nameID.setAttribute("Format", "");

        // When
        String actualError = helper.verifySpidResponse(samlResponse.getDocumentElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck47() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_47";

        XMLGregorianCalendar responseTimeWithoutMs = DatatypeFactory.newInstance().newXMLGregorianCalendar("2024-04-10T09:23:28.000Z");
        responseTimeWithoutMs.setMillisecond(0);


        ResponseType responseType = new ResponseType("id", responseTimeWithoutMs);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", responseTimeWithoutMs)));
        StatusType statusType = new StatusType();
        StatusCodeType statusCodeType = new StatusCodeType();
        statusCodeType.setValue(URI.create("urn:oasis:names:tc:SAML:2.0:status:Success"));
        statusType.setStatusCode(statusCodeType);
        responseType.setStatus(statusType);
        NameIDType issuer = new NameIDType();
        issuer.setValue("https://id.lepida.it/idp/shibboleth");
        issuer.setFormat(URI.create(SpidSAMLEndpoint.ISSUER_FORMAT));
        responseType.setIssuer(issuer);

        Element assertionSignature = DocumentUtil.getChildElement(samlAssertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);

        samlAssertion.setAttribute("IssueInstant", "2024-04-10T09:22:28.000Z");

        // Remove NameID
        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element nameID = DocumentUtil.getChildElement(subject, new QName("NameID"));
        nameID.setAttribute("Format", "uri:wrong:format");

        // When
        String actualError = helper.verifySpidResponse(samlResponse.getDocumentElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck4849() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_4849";

        XMLGregorianCalendar responseTimeWithoutMs = DatatypeFactory.newInstance().newXMLGregorianCalendar("2024-04-10T09:23:28.000Z");
        responseTimeWithoutMs.setMillisecond(0);


        ResponseType responseType = new ResponseType("id", responseTimeWithoutMs);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", responseTimeWithoutMs)));
        StatusType statusType = new StatusType();
        StatusCodeType statusCodeType = new StatusCodeType();
        statusCodeType.setValue(URI.create("urn:oasis:names:tc:SAML:2.0:status:Success"));
        statusType.setStatusCode(statusCodeType);
        responseType.setStatus(statusType);
        NameIDType issuer = new NameIDType();
        issuer.setValue("https://id.lepida.it/idp/shibboleth");
        issuer.setFormat(URI.create(SpidSAMLEndpoint.ISSUER_FORMAT));
        responseType.setIssuer(issuer);

        Element assertionSignature = DocumentUtil.getChildElement(samlAssertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);

        samlAssertion.setAttribute("IssueInstant", "2024-04-10T09:22:28.000Z");

        // Remove NameID
        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element nameID = DocumentUtil.getChildElement(subject, new QName("NameID"));
        nameID.setAttribute("NameQualifier", "");

        // When
        String actualError = helper.verifySpidResponse(samlResponse.getDocumentElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck52() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_nr52";

        XMLGregorianCalendar responseTimeWithoutMs = DatatypeFactory.newInstance().newXMLGregorianCalendar("2024-04-10T09:23:28.000Z");
        responseTimeWithoutMs.setMillisecond(0);


        ResponseType responseType = new ResponseType("id", responseTimeWithoutMs);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", responseTimeWithoutMs)));
        StatusType statusType = new StatusType();
        StatusCodeType statusCodeType = new StatusCodeType();
        statusCodeType.setValue(URI.create("urn:oasis:names:tc:SAML:2.0:status:Success"));
        statusType.setStatusCode(statusCodeType);
        responseType.setStatus(statusType);
        NameIDType issuer = new NameIDType();
        issuer.setValue("https://id.lepida.it/idp/shibboleth");
        issuer.setFormat(URI.create(SpidSAMLEndpoint.ISSUER_FORMAT));
        responseType.setIssuer(issuer);

        Element assertionSignature = DocumentUtil.getChildElement(samlAssertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);

        samlAssertion.setAttribute("IssueInstant", "2024-04-10T09:22:28.000Z");

        // Remove SubjectConfirmation
        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        for (int i = 0; i < subject.getChildNodes().getLength(); i++) {
            Node node = subject.getChildNodes().item(i);
            if (node.getNodeName().equals("saml2:SubjectConfirmation")) {
                node.getParentNode().removeChild(node);
            }
        }

        // When
        String actualError = helper.verifySpidResponse(samlResponse.getDocumentElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck51() throws DatatypeConfigurationException, ConfigurationException, ProcessingException {
        // Given
        String expectedError = "SpidSamlCheck_nr51";

        XMLGregorianCalendar responseTimeWithoutMs = DatatypeFactory.newInstance().newXMLGregorianCalendar("2024-04-10T09:23:28.000Z");
        responseTimeWithoutMs.setMillisecond(0);


        ResponseType responseType = new ResponseType("id", responseTimeWithoutMs);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", responseTimeWithoutMs)));
        StatusType statusType = new StatusType();
        StatusCodeType statusCodeType = new StatusCodeType();
        statusCodeType.setValue(URI.create("urn:oasis:names:tc:SAML:2.0:status:Success"));
        statusType.setStatusCode(statusCodeType);
        responseType.setStatus(statusType);
        NameIDType issuer = new NameIDType();
        issuer.setValue("https://id.lepida.it/idp/shibboleth");
        issuer.setFormat(URI.create(SpidSAMLEndpoint.ISSUER_FORMAT));
        responseType.setIssuer(issuer);

        Element assertionSignature = DocumentUtil.getChildElement(samlAssertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);

        samlAssertion.setAttribute("IssueInstant", "2024-04-10T09:22:28.000Z");

        // Remove SubjectConfirmation children
        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        subjectConfirmation.getParentNode().removeChild(subjectConfirmation);
        // Add empty SubjectConfirmation
        subject.appendChild(samlAssertion.getOwnerDocument().createElementNS("saml2", "SubjectConfirmation"));

        // When
        String actualError = helper.verifySpidResponse(samlResponse.getDocumentElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck53() throws DatatypeConfigurationException, ConfigurationException, ProcessingException {
        // Given
        String expectedError = "SpidSamlCheck_nr53";

        XMLGregorianCalendar responseTimeWithoutMs = DatatypeFactory.newInstance().newXMLGregorianCalendar("2024-04-10T09:23:28.000Z");
        responseTimeWithoutMs.setMillisecond(0);


        ResponseType responseType = new ResponseType("id", responseTimeWithoutMs);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", responseTimeWithoutMs)));
        StatusType statusType = new StatusType();
        StatusCodeType statusCodeType = new StatusCodeType();
        statusCodeType.setValue(URI.create("urn:oasis:names:tc:SAML:2.0:status:Success"));
        statusType.setStatusCode(statusCodeType);
        responseType.setStatus(statusType);
        NameIDType issuer = new NameIDType();
        issuer.setValue("https://id.lepida.it/idp/shibboleth");
        issuer.setFormat(URI.create(SpidSAMLEndpoint.ISSUER_FORMAT));
        responseType.setIssuer(issuer);

        Element assertionSignature = DocumentUtil.getChildElement(samlAssertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);

        samlAssertion.setAttribute("IssueInstant", "2024-04-10T09:22:28.000Z");

        // Remove SubjectConfirmation children
        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        subjectConfirmation.removeAttribute("Method");

        // When
        String actualError = helper.verifySpidResponse(samlResponse.getDocumentElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }


    @Test
    void raiseSpidSamlCheck54() throws DatatypeConfigurationException, ConfigurationException, ProcessingException {
        // Given
        String expectedError = "SpidSamlCheck_nr54";

        XMLGregorianCalendar responseTimeWithoutMs = DatatypeFactory.newInstance().newXMLGregorianCalendar("2024-04-10T09:23:28.000Z");
        responseTimeWithoutMs.setMillisecond(0);


        ResponseType responseType = new ResponseType("id", responseTimeWithoutMs);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", responseTimeWithoutMs)));
        StatusType statusType = new StatusType();
        StatusCodeType statusCodeType = new StatusCodeType();
        statusCodeType.setValue(URI.create("urn:oasis:names:tc:SAML:2.0:status:Success"));
        statusType.setStatusCode(statusCodeType);
        responseType.setStatus(statusType);
        NameIDType issuer = new NameIDType();
        issuer.setValue("https://id.lepida.it/idp/shibboleth");
        issuer.setFormat(URI.create(SpidSAMLEndpoint.ISSUER_FORMAT));
        responseType.setIssuer(issuer);

        Element assertionSignature = DocumentUtil.getChildElement(samlAssertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);

        samlAssertion.setAttribute("IssueInstant", "2024-04-10T09:22:28.000Z");

        // Remove SubjectConfirmation children
        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        subjectConfirmation.setAttribute("Method","");

        // When
        String actualError = helper.verifySpidResponse(samlResponse.getDocumentElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck55() throws DatatypeConfigurationException, ConfigurationException, ProcessingException {
        // Given
        String expectedError = "SpidSamlCheck_nr55";

        XMLGregorianCalendar responseTimeWithoutMs = DatatypeFactory.newInstance().newXMLGregorianCalendar("2024-04-10T09:23:28.000Z");
        responseTimeWithoutMs.setMillisecond(0);


        ResponseType responseType = new ResponseType("id", responseTimeWithoutMs);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", responseTimeWithoutMs)));
        StatusType statusType = new StatusType();
        StatusCodeType statusCodeType = new StatusCodeType();
        statusCodeType.setValue(URI.create("urn:oasis:names:tc:SAML:2.0:status:Success"));
        statusType.setStatusCode(statusCodeType);
        responseType.setStatus(statusType);
        NameIDType issuer = new NameIDType();
        issuer.setValue("https://id.lepida.it/idp/shibboleth");
        issuer.setFormat(URI.create(SpidSAMLEndpoint.ISSUER_FORMAT));
        responseType.setIssuer(issuer);

        Element assertionSignature = DocumentUtil.getChildElement(samlAssertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);

        samlAssertion.setAttribute("IssueInstant", "2024-04-10T09:22:28.000Z");

        // Remove SubjectConfirmation children
        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        subjectConfirmation.setAttribute("Method","wrong value");

        // When
        String actualError = helper.verifySpidResponse(samlResponse.getDocumentElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck56() throws DatatypeConfigurationException, ConfigurationException, ProcessingException {
        // Given
        String expectedError = "SpidSamlCheck_nr56";

        XMLGregorianCalendar responseTimeWithoutMs = DatatypeFactory.newInstance().newXMLGregorianCalendar("2024-04-10T09:23:28.000Z");
        responseTimeWithoutMs.setMillisecond(0);


        ResponseType responseType = new ResponseType("id", responseTimeWithoutMs);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", responseTimeWithoutMs)));
        StatusType statusType = new StatusType();
        StatusCodeType statusCodeType = new StatusCodeType();
        statusCodeType.setValue(URI.create("urn:oasis:names:tc:SAML:2.0:status:Success"));
        statusType.setStatusCode(statusCodeType);
        responseType.setStatus(statusType);
        NameIDType issuer = new NameIDType();
        issuer.setValue("https://id.lepida.it/idp/shibboleth");
        issuer.setFormat(URI.create(SpidSAMLEndpoint.ISSUER_FORMAT));
        responseType.setIssuer(issuer);

        Element assertionSignature = DocumentUtil.getChildElement(samlAssertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);

        samlAssertion.setAttribute("IssueInstant", "2024-04-10T09:22:28.000Z");

        // Remove SubjectConfirmation children
        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        Element subjectConfirmationData = DocumentUtil.getChildElement(subjectConfirmation, new QName("SubjectConfirmationData"));
        subjectConfirmation.removeChild(subjectConfirmationData);
        subjectConfirmation.appendChild(samlResponse.createElement("PlaceHolder"));

        // When
        String actualError = helper.verifySpidResponse(samlResponse.getDocumentElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck58() throws DatatypeConfigurationException, ConfigurationException, ProcessingException {
        // Given
        String expectedError = "SpidSamlCheck_nr58";

        XMLGregorianCalendar responseTimeWithoutMs = DatatypeFactory.newInstance().newXMLGregorianCalendar("2024-04-10T09:23:28.000Z");
        responseTimeWithoutMs.setMillisecond(0);


        ResponseType responseType = new ResponseType("id", responseTimeWithoutMs);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", responseTimeWithoutMs)));
        StatusType statusType = new StatusType();
        StatusCodeType statusCodeType = new StatusCodeType();
        statusCodeType.setValue(URI.create("urn:oasis:names:tc:SAML:2.0:status:Success"));
        statusType.setStatusCode(statusCodeType);
        responseType.setStatus(statusType);
        NameIDType issuer = new NameIDType();
        issuer.setValue("https://id.lepida.it/idp/shibboleth");
        issuer.setFormat(URI.create(SpidSAMLEndpoint.ISSUER_FORMAT));
        responseType.setIssuer(issuer);

        Element assertionSignature = DocumentUtil.getChildElement(samlAssertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);

        samlAssertion.setAttribute("IssueInstant", "2024-04-10T09:22:28.000Z");

        // Remove SubjectConfirmation children
        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        Element subjectConfirmationData = DocumentUtil.getChildElement(subjectConfirmation, new QName("SubjectConfirmationData"));
        subjectConfirmationData.removeAttribute("Recipient");

        // When
        String actualError = helper.verifySpidResponse(samlResponse.getDocumentElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck59() throws DatatypeConfigurationException, ConfigurationException, ProcessingException {
        // Given
        String expectedError = "SpidSamlCheck_59";

        XMLGregorianCalendar responseTimeWithoutMs = DatatypeFactory.newInstance().newXMLGregorianCalendar("2024-04-10T09:23:28.000Z");
        responseTimeWithoutMs.setMillisecond(0);


        ResponseType responseType = new ResponseType("id", responseTimeWithoutMs);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", responseTimeWithoutMs)));
        StatusType statusType = new StatusType();
        StatusCodeType statusCodeType = new StatusCodeType();
        statusCodeType.setValue(URI.create("urn:oasis:names:tc:SAML:2.0:status:Success"));
        statusType.setStatusCode(statusCodeType);
        responseType.setStatus(statusType);
        NameIDType issuer = new NameIDType();
        issuer.setValue("https://id.lepida.it/idp/shibboleth");
        issuer.setFormat(URI.create(SpidSAMLEndpoint.ISSUER_FORMAT));
        responseType.setIssuer(issuer);

        Element assertionSignature = DocumentUtil.getChildElement(samlAssertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);

        samlAssertion.setAttribute("IssueInstant", "2024-04-10T09:22:28.000Z");

        // Remove SubjectConfirmation children
        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        Element subjectConfirmationData = DocumentUtil.getChildElement(subjectConfirmation, new QName("SubjectConfirmationData"));
        subjectConfirmationData.setAttribute("Recipient","wrong-recipient");

        // When
        String actualError = helper.verifySpidResponse(samlResponse.getDocumentElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", "https://login.agid.gov.it/saml/module.php/saml/sp/saml2-acs.php/service");

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck57() throws DatatypeConfigurationException, ConfigurationException, ProcessingException {
        // Given
        String expectedError = "SpidSamlCheck_nr57";

        XMLGregorianCalendar responseTimeWithoutMs = DatatypeFactory.newInstance().newXMLGregorianCalendar("2024-04-10T09:23:28.000Z");
        responseTimeWithoutMs.setMillisecond(0);


        ResponseType responseType = new ResponseType("id", responseTimeWithoutMs);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", responseTimeWithoutMs)));
        StatusType statusType = new StatusType();
        StatusCodeType statusCodeType = new StatusCodeType();
        statusCodeType.setValue(URI.create("urn:oasis:names:tc:SAML:2.0:status:Success"));
        statusType.setStatusCode(statusCodeType);
        responseType.setStatus(statusType);
        NameIDType issuer = new NameIDType();
        issuer.setValue("https://id.lepida.it/idp/shibboleth");
        issuer.setFormat(URI.create(SpidSAMLEndpoint.ISSUER_FORMAT));
        responseType.setIssuer(issuer);

        Element assertionSignature = DocumentUtil.getChildElement(samlAssertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);

        samlAssertion.setAttribute("IssueInstant", "2024-04-10T09:22:28.000Z");

        // Remove SubjectConfirmation children
        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        Element subjectConfirmationData = DocumentUtil.getChildElement(subjectConfirmation, new QName("SubjectConfirmationData"));
        subjectConfirmationData.setAttribute("Recipient","");

        // When
        String actualError = helper.verifySpidResponse(samlResponse.getDocumentElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", "https://login.agid.gov.it/saml/module.php/saml/sp/saml2-acs.php/service");

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck61() throws DatatypeConfigurationException, ConfigurationException, ProcessingException {
        // Given
        String expectedError = "SpidSamlCheck_nr61";

        XMLGregorianCalendar responseTimeWithoutMs = DatatypeFactory.newInstance().newXMLGregorianCalendar("2024-04-10T09:23:28.000Z");
        responseTimeWithoutMs.setMillisecond(0);


        ResponseType responseType = new ResponseType("id", responseTimeWithoutMs);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", responseTimeWithoutMs)));
        StatusType statusType = new StatusType();
        StatusCodeType statusCodeType = new StatusCodeType();
        statusCodeType.setValue(URI.create("urn:oasis:names:tc:SAML:2.0:status:Success"));
        statusType.setStatusCode(statusCodeType);
        responseType.setStatus(statusType);
        NameIDType issuer = new NameIDType();
        issuer.setValue("https://id.lepida.it/idp/shibboleth");
        issuer.setFormat(URI.create(SpidSAMLEndpoint.ISSUER_FORMAT));
        responseType.setIssuer(issuer);

        Element assertionSignature = DocumentUtil.getChildElement(samlAssertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);

        samlAssertion.setAttribute("IssueInstant", "2024-04-10T09:22:28.000Z");

        // Remove SubjectConfirmation children
        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        Element subjectConfirmationData = DocumentUtil.getChildElement(subjectConfirmation, new QName("SubjectConfirmationData"));
        subjectConfirmationData.removeAttribute("InResponseTo");

        // When
        String actualError = helper.verifySpidResponse(samlResponse.getDocumentElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", "https://login.agid.gov.it/saml/module.php/saml/sp/saml2-acs.php/service");

        // Then
        assertEquals(expectedError, actualError);
    }


    @Test
    void raiseSpidSamlCheck60() throws DatatypeConfigurationException, ConfigurationException, ProcessingException {
        // Given
        String expectedError = "SpidSamlCheck_nr60";

        XMLGregorianCalendar responseTimeWithoutMs = DatatypeFactory.newInstance().newXMLGregorianCalendar("2024-04-10T09:23:28.000Z");
        responseTimeWithoutMs.setMillisecond(0);


        ResponseType responseType = new ResponseType("id", responseTimeWithoutMs);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", responseTimeWithoutMs)));
        StatusType statusType = new StatusType();
        StatusCodeType statusCodeType = new StatusCodeType();
        statusCodeType.setValue(URI.create("urn:oasis:names:tc:SAML:2.0:status:Success"));
        statusType.setStatusCode(statusCodeType);
        responseType.setStatus(statusType);
        NameIDType issuer = new NameIDType();
        issuer.setValue("https://id.lepida.it/idp/shibboleth");
        issuer.setFormat(URI.create(SpidSAMLEndpoint.ISSUER_FORMAT));
        responseType.setIssuer(issuer);

        Element assertionSignature = DocumentUtil.getChildElement(samlAssertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);

        samlAssertion.setAttribute("IssueInstant", "2024-04-10T09:22:28.000Z");

        // Remove SubjectConfirmation children
        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        Element subjectConfirmationData = DocumentUtil.getChildElement(subjectConfirmation, new QName("SubjectConfirmationData"));
        subjectConfirmationData.setAttribute("InResponseTo","");

        // When
        String actualError = helper.verifySpidResponse(samlResponse.getDocumentElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", "https://login.agid.gov.it/saml/module.php/saml/sp/saml2-acs.php/service");

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck62() throws DatatypeConfigurationException, ConfigurationException, ProcessingException {
        // Given
        String expectedError = "SpidSamlCheck_nr62";

        XMLGregorianCalendar responseTimeWithoutMs = DatatypeFactory.newInstance().newXMLGregorianCalendar("2024-04-10T09:23:28.000Z");
        responseTimeWithoutMs.setMillisecond(0);


        ResponseType responseType = new ResponseType("id", responseTimeWithoutMs);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", responseTimeWithoutMs)));
        StatusType statusType = new StatusType();
        StatusCodeType statusCodeType = new StatusCodeType();
        statusCodeType.setValue(URI.create("urn:oasis:names:tc:SAML:2.0:status:Success"));
        statusType.setStatusCode(statusCodeType);
        responseType.setStatus(statusType);
        NameIDType issuer = new NameIDType();
        issuer.setValue("https://id.lepida.it/idp/shibboleth");
        issuer.setFormat(URI.create(SpidSAMLEndpoint.ISSUER_FORMAT));
        responseType.setIssuer(issuer);

        Element assertionSignature = DocumentUtil.getChildElement(samlAssertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);

        samlAssertion.setAttribute("IssueInstant", "2024-04-10T09:22:28.000Z");

        // Remove SubjectConfirmation children
        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        Element subjectConfirmationData = DocumentUtil.getChildElement(subjectConfirmation, new QName("SubjectConfirmationData"));
        subjectConfirmationData.setAttribute("InResponseTo","wrong-value");

        // When
        String actualError = helper.verifySpidResponse(samlResponse.getDocumentElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", "https://login.agid.gov.it/saml/module.php/saml/sp/saml2-acs.php/service");

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck64() throws DatatypeConfigurationException, ConfigurationException, ProcessingException {
        // Given
        String expectedError = "SpidSamlCheck_64";

        XMLGregorianCalendar responseTimeWithoutMs = DatatypeFactory.newInstance().newXMLGregorianCalendar("2024-04-10T09:23:28.000Z");
        responseTimeWithoutMs.setMillisecond(0);


        ResponseType responseType = new ResponseType("id", responseTimeWithoutMs);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", responseTimeWithoutMs)));
        StatusType statusType = new StatusType();
        StatusCodeType statusCodeType = new StatusCodeType();
        statusCodeType.setValue(URI.create("urn:oasis:names:tc:SAML:2.0:status:Success"));
        statusType.setStatusCode(statusCodeType);
        responseType.setStatus(statusType);
        NameIDType issuer = new NameIDType();
        issuer.setValue("https://id.lepida.it/idp/shibboleth");
        issuer.setFormat(URI.create(SpidSAMLEndpoint.ISSUER_FORMAT));
        responseType.setIssuer(issuer);

        Element assertionSignature = DocumentUtil.getChildElement(samlAssertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);

        samlAssertion.setAttribute("IssueInstant", "2024-04-10T09:22:28.000Z");

        // Remove SubjectConfirmation children
        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        Element subjectConfirmationData = DocumentUtil.getChildElement(subjectConfirmation, new QName("SubjectConfirmationData"));
        subjectConfirmationData.setAttribute("NotOnOrAfter","");

        // When
        String actualError = helper.verifySpidResponse(samlResponse.getDocumentElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", "https://login.agid.gov.it/saml/module.php/saml/sp/saml2-acs.php/service");

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck66() throws DatatypeConfigurationException, ConfigurationException, ProcessingException {
        // Given
        String expectedError = "SpidSamlCheck_66";

        XMLGregorianCalendar responseTimeWithoutMs = DatatypeFactory.newInstance().newXMLGregorianCalendar("2024-04-10T09:23:28.000Z");
        responseTimeWithoutMs.setMillisecond(0);


        ResponseType responseType = new ResponseType("id", responseTimeWithoutMs);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", responseTimeWithoutMs)));
        StatusType statusType = new StatusType();
        StatusCodeType statusCodeType = new StatusCodeType();
        statusCodeType.setValue(URI.create("urn:oasis:names:tc:SAML:2.0:status:Success"));
        statusType.setStatusCode(statusCodeType);
        responseType.setStatus(statusType);
        NameIDType issuer = new NameIDType();
        issuer.setValue("https://id.lepida.it/idp/shibboleth");
        issuer.setFormat(URI.create(SpidSAMLEndpoint.ISSUER_FORMAT));
        responseType.setIssuer(issuer);

        Element assertionSignature = DocumentUtil.getChildElement(samlAssertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);

        samlAssertion.setAttribute("IssueInstant", "2024-04-10T09:22:28.000Z");

        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        Element subjectConfirmationData = DocumentUtil.getChildElement(subjectConfirmation, new QName("SubjectConfirmationData"));
        subjectConfirmationData.setAttribute("NotOnOrAfter","2024-04-10T09:22:28.089Z");

        // When
        String actualError = helper.verifySpidResponse(samlResponse.getDocumentElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", "https://login.agid.gov.it/saml/module.php/saml/sp/saml2-acs.php/service");

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck67() throws DatatypeConfigurationException, ConfigurationException, ProcessingException {
        // Given
        String expectedError = "SpidSamlCheck_67";

        XMLGregorianCalendar responseTimeWithoutMs = DatatypeFactory.newInstance().newXMLGregorianCalendar("2024-04-10T09:23:28.000Z");
        responseTimeWithoutMs.setMillisecond(0);


        ResponseType responseType = new ResponseType("id", responseTimeWithoutMs);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", responseTimeWithoutMs)));
        StatusType statusType = new StatusType();
        StatusCodeType statusCodeType = new StatusCodeType();
        statusCodeType.setValue(URI.create("urn:oasis:names:tc:SAML:2.0:status:Success"));
        statusType.setStatusCode(statusCodeType);
        responseType.setStatus(statusType);
        NameIDType issuer = new NameIDType();
        issuer.setValue("https://id.lepida.it/idp/shibboleth");
        issuer.setFormat(URI.create(SpidSAMLEndpoint.ISSUER_FORMAT));
        responseType.setIssuer(issuer);

        Element assertionSignature = DocumentUtil.getChildElement(samlAssertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);

        samlAssertion.setAttribute("IssueInstant", "2024-04-10T09:22:28.000Z");

        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        Element subjectConfirmationData = DocumentUtil.getChildElement(subjectConfirmation, new QName("SubjectConfirmationData"));
        ZonedDateTime to = ZonedDateTime.now().plus(Duration.of(3, TimeUnit.MINUTES.toChronoUnit()));
        String someMinutesFromNow = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(to);
        subjectConfirmationData.setAttribute("NotOnOrAfter",someMinutesFromNow);

        Element issuerElement = DocumentUtil.getChildElement(samlAssertion, new QName("Issuer"));
        issuerElement.getFirstChild().setNodeValue(null);

        // When
        String actualError = helper.verifySpidResponse(samlResponse.getDocumentElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", "https://login.agid.gov.it/saml/module.php/saml/sp/saml2-acs.php/service");

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck68() throws DatatypeConfigurationException, ConfigurationException, ProcessingException {
        // Given
        String expectedError = "SpidSamlCheck_68";

        XMLGregorianCalendar responseTimeWithoutMs = DatatypeFactory.newInstance().newXMLGregorianCalendar("2024-04-10T09:23:28.000Z");
        responseTimeWithoutMs.setMillisecond(0);


        ResponseType responseType = new ResponseType("id", responseTimeWithoutMs);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", responseTimeWithoutMs)));
        StatusType statusType = new StatusType();
        StatusCodeType statusCodeType = new StatusCodeType();
        statusCodeType.setValue(URI.create("urn:oasis:names:tc:SAML:2.0:status:Success"));
        statusType.setStatusCode(statusCodeType);
        responseType.setStatus(statusType);
        NameIDType issuer = new NameIDType();
        issuer.setValue("https://id.lepida.it/idp/shibboleth");
        issuer.setFormat(URI.create(SpidSAMLEndpoint.ISSUER_FORMAT));
        responseType.setIssuer(issuer);

        Element assertionSignature = DocumentUtil.getChildElement(samlAssertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);

        samlAssertion.setAttribute("IssueInstant", "2024-04-10T09:22:28.000Z");

        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        Element subjectConfirmationData = DocumentUtil.getChildElement(subjectConfirmation, new QName("SubjectConfirmationData"));
        ZonedDateTime to = ZonedDateTime.now().plus(Duration.of(3, TimeUnit.MINUTES.toChronoUnit()));
        String someMinutesFromNow = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(to);
        subjectConfirmationData.setAttribute("NotOnOrAfter",someMinutesFromNow);

        Element issuerElement = DocumentUtil.getChildElement(samlAssertion, new QName("Issuer"));
        issuerElement.getParentNode().removeChild(issuerElement);

        // When
        String actualError = helper.verifySpidResponse(samlResponse.getDocumentElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", "https://login.agid.gov.it/saml/module.php/saml/sp/saml2-acs.php/service");

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck69() throws DatatypeConfigurationException, ConfigurationException, ProcessingException {
        // Given
        String expectedError = "SpidSamlCheck_69";

        XMLGregorianCalendar responseTimeWithoutMs = DatatypeFactory.newInstance().newXMLGregorianCalendar("2024-04-10T09:23:28.000Z");
        responseTimeWithoutMs.setMillisecond(0);


        ResponseType responseType = new ResponseType("id", responseTimeWithoutMs);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", responseTimeWithoutMs)));
        StatusType statusType = new StatusType();
        StatusCodeType statusCodeType = new StatusCodeType();
        statusCodeType.setValue(URI.create("urn:oasis:names:tc:SAML:2.0:status:Success"));
        statusType.setStatusCode(statusCodeType);
        responseType.setStatus(statusType);
        NameIDType issuer = new NameIDType();
        issuer.setValue("https://id.lepida.it/idp/shibboleth");
        issuer.setFormat(URI.create(SpidSAMLEndpoint.ISSUER_FORMAT));
        responseType.setIssuer(issuer);

        Element assertionSignature = DocumentUtil.getChildElement(samlAssertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);

        samlAssertion.setAttribute("IssueInstant", "2024-04-10T09:22:28.000Z");

        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        Element subjectConfirmationData = DocumentUtil.getChildElement(subjectConfirmation, new QName("SubjectConfirmationData"));
        ZonedDateTime to = ZonedDateTime.now().plus(Duration.of(3, TimeUnit.MINUTES.toChronoUnit()));
        String someMinutesFromNow = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(to);
        subjectConfirmationData.setAttribute("NotOnOrAfter",someMinutesFromNow);

        Element issuerElement = DocumentUtil.getChildElement(samlAssertion, new QName("Issuer"));
        issuerElement.getFirstChild().setNodeValue("wrong-value");

        // When
        String actualError = helper.verifySpidResponse(samlResponse.getDocumentElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", "https://login.agid.gov.it/saml/module.php/saml/sp/saml2-acs.php/service");

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck7071() throws DatatypeConfigurationException, ConfigurationException, ProcessingException {
        // Given
        String expectedError = "SpidSamlCheck_7071";

        XMLGregorianCalendar responseTimeWithoutMs = DatatypeFactory.newInstance().newXMLGregorianCalendar("2024-04-10T09:23:28.000Z");
        responseTimeWithoutMs.setMillisecond(0);


        ResponseType responseType = new ResponseType("id", responseTimeWithoutMs);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", responseTimeWithoutMs)));
        StatusType statusType = new StatusType();
        StatusCodeType statusCodeType = new StatusCodeType();
        statusCodeType.setValue(URI.create("urn:oasis:names:tc:SAML:2.0:status:Success"));
        statusType.setStatusCode(statusCodeType);
        responseType.setStatus(statusType);
        NameIDType issuer = new NameIDType();
        issuer.setValue("https://id.lepida.it/idp/shibboleth");
        issuer.setFormat(URI.create(SpidSAMLEndpoint.ISSUER_FORMAT));
        responseType.setIssuer(issuer);

        Element assertionSignature = DocumentUtil.getChildElement(samlAssertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);

        samlAssertion.setAttribute("IssueInstant", "2024-04-10T09:22:28.000Z");

        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        Element subjectConfirmationData = DocumentUtil.getChildElement(subjectConfirmation, new QName("SubjectConfirmationData"));
        ZonedDateTime to = ZonedDateTime.now().plus(Duration.of(3, TimeUnit.MINUTES.toChronoUnit()));
        String someMinutesFromNow = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(to);
        subjectConfirmationData.setAttribute("NotOnOrAfter",someMinutesFromNow);

        Element issuerElement = DocumentUtil.getChildElement(samlAssertion, new QName("Issuer"));
        issuerElement.setAttribute("Format","");

        // When
        String actualError = helper.verifySpidResponse(samlResponse.getDocumentElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", "https://login.agid.gov.it/saml/module.php/saml/sp/saml2-acs.php/service");

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck72() throws DatatypeConfigurationException, ConfigurationException, ProcessingException {
        // Given
        String expectedError = "SpidSamlCheck_72";

        XMLGregorianCalendar responseTimeWithoutMs = DatatypeFactory.newInstance().newXMLGregorianCalendar("2024-04-10T09:23:28.000Z");
        responseTimeWithoutMs.setMillisecond(0);


        ResponseType responseType = new ResponseType("id", responseTimeWithoutMs);
        responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
        responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", responseTimeWithoutMs)));
        StatusType statusType = new StatusType();
        StatusCodeType statusCodeType = new StatusCodeType();
        statusCodeType.setValue(URI.create("urn:oasis:names:tc:SAML:2.0:status:Success"));
        statusType.setStatusCode(statusCodeType);
        responseType.setStatus(statusType);
        NameIDType issuer = new NameIDType();
        issuer.setValue("https://id.lepida.it/idp/shibboleth");
        issuer.setFormat(URI.create(SpidSAMLEndpoint.ISSUER_FORMAT));
        responseType.setIssuer(issuer);

        Element assertionSignature = DocumentUtil.getChildElement(samlAssertion, new QName("Signature"));
        responseType.getAssertions().get(0).getAssertion().setSignature(assertionSignature);

        samlAssertion.setAttribute("IssueInstant", "2024-04-10T09:22:28.000Z");

        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        Element subjectConfirmationData = DocumentUtil.getChildElement(subjectConfirmation, new QName("SubjectConfirmationData"));
        ZonedDateTime to = ZonedDateTime.now().plus(Duration.of(3, TimeUnit.MINUTES.toChronoUnit()));
        String someMinutesFromNow = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(to);
        subjectConfirmationData.setAttribute("NotOnOrAfter",someMinutesFromNow);

        Element issuerElement = DocumentUtil.getChildElement(samlAssertion, new QName("Issuer"));
        issuerElement.setAttribute("Format","wrong-format");

        // When
        String actualError = helper.verifySpidResponse(samlResponse.getDocumentElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", "https://login.agid.gov.it/saml/module.php/saml/sp/saml2-acs.php/service");

        // Then
        assertEquals(expectedError, actualError);
    }





    //System.out.println("Document:\n" + DocumentUtil.getDocumentAsString(subjectConfirmation.getOwnerDocument()));

}