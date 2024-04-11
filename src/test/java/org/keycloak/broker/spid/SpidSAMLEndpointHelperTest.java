package org.keycloak.broker.spid;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.dom.saml.v2.assertion.NameIDType;
import org.keycloak.dom.saml.v2.protocol.ResponseType;
import org.keycloak.dom.saml.v2.protocol.StatusType;
import org.keycloak.saml.common.exceptions.ConfigurationException;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.exceptions.ProcessingException;
import org.keycloak.saml.common.util.DocumentUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.keycloak.broker.spid.ObjectMother.SamlResponseTypes.*;

class SpidSAMLEndpointHelperTest {

    SpidSAMLEndpointHelper helper;

    @BeforeEach
    void init() {
        SpidIdentityProviderConfig config = new SpidIdentityProviderConfig();
        config.setIdpEntityId("https://id.lepida.it/idp/shibboleth");
        config.setEntityId("https://spid.agid.gov.it");
        config.setAuthnContextClassRefs("https://www.spid.gov.it/SpidL2");
        helper = new SpidSAMLEndpointHelper(config);
    }

    @Test
    void raiseSpidSamlCheck02() throws DatatypeConfigurationException {
        // Given
        String expected = "SpidSamlCheck_02";
        ResponseType responseType = CompleteResponseType();
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

        ResponseType responseType = CompleteResponseType();
        responseType.getAssertions().get(0).getAssertion().setSignature(null);

        // When
        String actualError = helper.verifySpidResponse(null, null, null, responseType, null, null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck08() throws DatatypeConfigurationException {
        String expectedError = "SpidSamlCheck_08";

        ResponseType responseType = CompleteResponseType("", "2024-04-10T09:22:28.065Z");

        // When
        String actualError = helper.verifySpidResponse(null, null, null, responseType, null, null);

        // Then
        assertEquals(expectedError, actualError);
    }


    @Test
    void raiseSpidSamlCheck14() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_14";
        String requestAfterResponse = "2024-04-10T09:24:27.065Z";

        // When
        String actualError = helper.verifySpidResponse(null, null, null, CompleteResponseType(), requestAfterResponse, null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck15() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_15";
        String requestMoreThan3MinutesBefore = "2024-04-10T09:18:28.065Z";

        // When
        String actualError = helper.verifySpidResponse(null, null, null, CompleteResponseType(), requestMoreThan3MinutesBefore, null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck15Future() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_15";
        String requestTimeInRange = "2124-04-10T09:20:28.065Z";
        String responseTimeInFuture = "2124-04-10T09:22:28.065Z";

        ResponseType responseType = CompleteResponseType("responseTimeInFuture", responseTimeInFuture);

        // When
        String actualError = helper.verifySpidResponse(null, null, null, responseType, requestTimeInRange, null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck110() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_110";

        ResponseType responseType = CompleteResponseType("responseTimeInFuture", "2024-04-10T09:22:28.999Z");

        // When
        String actualError = helper.verifySpidResponse(null, null, null, responseType, "2024-04-10T09:22:28.065Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }


    @Test
    void raiseSpidSamlCheck17() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_nr17";

        Element samlResponse = samlResponseElement();
        samlResponse.removeAttribute("InResponseTo");

        // When
        String actualError = helper.verifySpidResponse(samlResponse, null, null, CompleteResponseType(), "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck16() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_nr16";

        Element samlResponse = samlResponseElement();
        samlResponse.setAttribute("InResponseTo", "");

        // When
        String actualError = helper.verifySpidResponse(samlResponse, null, null, CompleteResponseType(), "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck18() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_nr18";

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), null, "not-expected-in-response-to", CompleteResponseType(), "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck22() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_22";

        ResponseType responseType = ObjectMother.SamlResponseTypes.CompleteResponseType();
        responseType.setStatus(new StatusType());

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), null, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck23() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_23";

        ResponseType responseType = CompleteResponseType();
        responseType.setStatus(null);

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), null, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck24() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_24";

        ResponseType responseType = CompleteResponseType();
        responseType.getStatus().getStatusCode().setValue(URI.create(""));

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), null, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck25() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_25";

        ResponseType responseType = CompleteResponseType();
        responseType.getStatus().setStatusCode(null);
        responseType.getStatus().setStatusMessage("not null");

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), null, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck26() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_26";

        ResponseType responseType = CompleteResponseType();
        responseType.getStatus().getStatusCode().setValue(URI.create("uri:not:valid"));

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), null, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck27() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_27";

        ResponseType responseType = CompleteResponseType();
        responseType.setIssuer(new NameIDType());

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), null, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck28() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_28";

        ResponseType responseType = CompleteResponseType();
        responseType.setIssuer(null);

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), null, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck29() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_29";

        ResponseType responseType = CompleteResponseType();
        responseType.getIssuer().setValue("wrong issuer");

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), null, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck30() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_30";

        ResponseType responseType = CompleteResponseType();
        responseType.getIssuer().setFormat(URI.create("uri:not:correct"));

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), null, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", responseType, "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck33() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_33";

        Element samlAssertion = samlAssertion();
        samlAssertion.setAttribute("ID", "");

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck39() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_39";

        Element samlAssertion = samlAssertion();
        samlAssertion.setAttribute("IssueInstant", "2024-04-10T08:22:28.065Z");

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck40() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_40";

        Element samlAssertion = samlAssertion();
        // more than 3 minutes
        samlAssertion.setAttribute("IssueInstant", "2024-04-10T09:27:28.065Z");

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck110FromAssertion() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_110";

        Element samlAssertion = samlAssertion();
        // with milliseconds
        samlAssertion.setAttribute("IssueInstant", "2024-04-10T09:23:28.065Z");

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }


    @Test
    void raiseSpidSamlCheck42() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_nr42";

        Element samlAssertion = samlAssertion();
        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        samlAssertion.removeChild(subject);

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck41() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_nr41";

        Element samlAssertion = samlAssertion();
        // Remove Subject
        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        subject.getParentNode().removeChild(subject);
        // Add empty Subject
        samlAssertion.appendChild(samlAssertion.getOwnerDocument().createElementNS("saml2", "Subject"));

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck44() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_44";

        Element samlAssertion = samlAssertion();
        // Remove NameID
        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        for (int i = 0; i < subject.getChildNodes().getLength(); i++) {
            Node node = subject.getChildNodes().item(i);
            if (node.getNodeName().equals("saml2:NameID")) {
                node.getParentNode().removeChild(node);
            }
        }

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck43() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_43";

        Element samlAssertion = samlAssertion();

        // Remove NameID
        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        for (int i = 0; i < subject.getChildNodes().getLength(); i++) {
            Node node = subject.getChildNodes().item(i);
            if (node.getNodeName().equals("saml2:NameID")) {
                node.getFirstChild().setNodeValue(" ");
            }
        }

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck4546() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_4546";

        Element samlAssertion = samlAssertion();

        // Remove NameID
        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element nameID = DocumentUtil.getChildElement(subject, new QName("NameID"));
        nameID.setAttribute("Format", "");

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck47() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_47";

        Element samlAssertion = samlAssertion();

        // Remove NameID
        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element nameID = DocumentUtil.getChildElement(subject, new QName("NameID"));
        nameID.setAttribute("Format", "uri:wrong:format");

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck4849() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_4849";

        Element samlAssertion = samlAssertion();

        // Remove NameID
        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element nameID = DocumentUtil.getChildElement(subject, new QName("NameID"));
        nameID.setAttribute("NameQualifier", "");

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck52() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_nr52";

        Element samlAssertion = samlAssertion();

        // Remove SubjectConfirmation
        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        for (int i = 0; i < subject.getChildNodes().getLength(); i++) {
            Node node = subject.getChildNodes().item(i);
            if (node.getNodeName().equals("saml2:SubjectConfirmation")) {
                node.getParentNode().removeChild(node);
            }
        }

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck51() throws DatatypeConfigurationException, ConfigurationException, ProcessingException {
        // Given
        String expectedError = "SpidSamlCheck_nr51";

        Element samlAssertion = samlAssertion();

        // Remove SubjectConfirmation children
        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        subjectConfirmation.getParentNode().removeChild(subjectConfirmation);
        // Add empty SubjectConfirmation
        subject.appendChild(samlAssertion.getOwnerDocument().createElementNS("saml2", "SubjectConfirmation"));

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck53() throws DatatypeConfigurationException, ConfigurationException, ProcessingException {
        // Given
        String expectedError = "SpidSamlCheck_nr53";

        Element samlAssertion = samlAssertion();

        // Remove SubjectConfirmation children
        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        subjectConfirmation.removeAttribute("Method");

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }


    @Test
    void raiseSpidSamlCheck54() throws DatatypeConfigurationException, ConfigurationException, ProcessingException {
        // Given
        String expectedError = "SpidSamlCheck_nr54";

        Element samlAssertion = samlAssertion();

        // Remove SubjectConfirmation children
        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        subjectConfirmation.setAttribute("Method", "");

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck55() throws DatatypeConfigurationException, ConfigurationException, ProcessingException {
        // Given
        String expectedError = "SpidSamlCheck_nr55";

        Element samlAssertion = samlAssertion();


        // Remove SubjectConfirmation children
        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        subjectConfirmation.setAttribute("Method", "wrong value");

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck56() throws DatatypeConfigurationException, ConfigurationException, ProcessingException {
        // Given
        String expectedError = "SpidSamlCheck_nr56";

        Element samlAssertion = samlAssertion();

        // Remove SubjectConfirmation children
        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        Element subjectConfirmationData = DocumentUtil.getChildElement(subjectConfirmation, new QName("SubjectConfirmationData"));
        subjectConfirmation.removeChild(subjectConfirmationData);
        subjectConfirmation.appendChild(samlAssertion.getOwnerDocument().createElement("PlaceHolder"));

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck58() throws DatatypeConfigurationException, ConfigurationException, ProcessingException {
        // Given
        String expectedError = "SpidSamlCheck_nr58";

        Element samlAssertion = samlAssertion();


        // Remove SubjectConfirmation children
        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        Element subjectConfirmationData = DocumentUtil.getChildElement(subjectConfirmation, new QName("SubjectConfirmationData"));
        subjectConfirmationData.removeAttribute("Recipient");

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", null);

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck59() throws DatatypeConfigurationException, ConfigurationException, ProcessingException {
        // Given
        String expectedError = "SpidSamlCheck_59";

        Element samlAssertion = samlAssertion();

        // Remove SubjectConfirmation children
        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        Element subjectConfirmationData = DocumentUtil.getChildElement(subjectConfirmation, new QName("SubjectConfirmationData"));
        subjectConfirmationData.setAttribute("Recipient", "wrong-recipient");

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", "https://login.agid.gov.it/saml/module.php/saml/sp/saml2-acs.php/service");

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck57() throws DatatypeConfigurationException, ConfigurationException, ProcessingException {
        // Given
        String expectedError = "SpidSamlCheck_nr57";

        Element samlAssertion = samlAssertion();

        // Remove SubjectConfirmation children
        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        Element subjectConfirmationData = DocumentUtil.getChildElement(subjectConfirmation, new QName("SubjectConfirmationData"));
        subjectConfirmationData.setAttribute("Recipient", "");

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", "https://login.agid.gov.it/saml/module.php/saml/sp/saml2-acs.php/service");

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck61() throws DatatypeConfigurationException, ConfigurationException, ProcessingException {
        // Given
        String expectedError = "SpidSamlCheck_nr61";

        Element samlAssertion = samlAssertion();

        // Remove InResponseTo
        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        Element subjectConfirmationData = DocumentUtil.getChildElement(subjectConfirmation, new QName("SubjectConfirmationData"));
        subjectConfirmationData.removeAttribute("InResponseTo");

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", "https://login.agid.gov.it/saml/module.php/saml/sp/saml2-acs.php/service");

        // Then
        assertEquals(expectedError, actualError);
    }


    @Test
    void raiseSpidSamlCheck60() throws DatatypeConfigurationException, ConfigurationException, ProcessingException {
        // Given
        String expectedError = "SpidSamlCheck_nr60";

        Element samlAssertion = samlAssertion();

        // Remove SubjectConfirmation children
        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        Element subjectConfirmationData = DocumentUtil.getChildElement(subjectConfirmation, new QName("SubjectConfirmationData"));
        subjectConfirmationData.setAttribute("InResponseTo", "");

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", "https://login.agid.gov.it/saml/module.php/saml/sp/saml2-acs.php/service");

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck62() throws DatatypeConfigurationException, ConfigurationException, ProcessingException {
        // Given
        String expectedError = "SpidSamlCheck_nr62";

        Element samlAssertion = samlAssertion();

        // Remove SubjectConfirmation children
        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        Element subjectConfirmationData = DocumentUtil.getChildElement(subjectConfirmation, new QName("SubjectConfirmationData"));
        subjectConfirmationData.setAttribute("InResponseTo", "wrong-value");

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", "https://login.agid.gov.it/saml/module.php/saml/sp/saml2-acs.php/service");

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck64() throws DatatypeConfigurationException, ConfigurationException, ProcessingException {
        // Given
        String expectedError = "SpidSamlCheck_64";

        Element samlAssertion = samlAssertion();

        // Remove SubjectConfirmation children
        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        Element subjectConfirmationData = DocumentUtil.getChildElement(subjectConfirmation, new QName("SubjectConfirmationData"));
        subjectConfirmationData.setAttribute("NotOnOrAfter", "");

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", "https://login.agid.gov.it/saml/module.php/saml/sp/saml2-acs.php/service");

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck66() throws DatatypeConfigurationException, ConfigurationException, ProcessingException {
        // Given
        String expectedError = "SpidSamlCheck_66";

        Element samlAssertion = samlAssertion();

        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        Element subjectConfirmationData = DocumentUtil.getChildElement(subjectConfirmation, new QName("SubjectConfirmationData"));
        subjectConfirmationData.setAttribute("NotOnOrAfter", "2024-04-10T09:22:28.089Z");

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", "https://login.agid.gov.it/saml/module.php/saml/sp/saml2-acs.php/service");

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck67() throws DatatypeConfigurationException, ConfigurationException, ProcessingException {
        // Given
        String expectedError = "SpidSamlCheck_67";

        Element samlAssertion = samlAssertion();

        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        Element subjectConfirmationData = DocumentUtil.getChildElement(subjectConfirmation, new QName("SubjectConfirmationData"));
        ZonedDateTime to = ZonedDateTime.now().plus(Duration.of(3, TimeUnit.MINUTES.toChronoUnit()));
        String someMinutesFromNow = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(to);
        subjectConfirmationData.setAttribute("NotOnOrAfter", someMinutesFromNow);

        Element issuerElement = DocumentUtil.getChildElement(samlAssertion, new QName("Issuer"));
        issuerElement.getFirstChild().setNodeValue(null);

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", "https://login.agid.gov.it/saml/module.php/saml/sp/saml2-acs.php/service");

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck68() throws DatatypeConfigurationException, ConfigurationException, ProcessingException {
        // Given
        String expectedError = "SpidSamlCheck_68";

        Element samlAssertion = samlAssertion();

        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        Element subjectConfirmationData = DocumentUtil.getChildElement(subjectConfirmation, new QName("SubjectConfirmationData"));
        ZonedDateTime to = ZonedDateTime.now().plus(Duration.of(3, TimeUnit.MINUTES.toChronoUnit()));
        String someMinutesFromNow = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(to);
        subjectConfirmationData.setAttribute("NotOnOrAfter", someMinutesFromNow);

        Element issuerElement = DocumentUtil.getChildElement(samlAssertion, new QName("Issuer"));
        issuerElement.getParentNode().removeChild(issuerElement);

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", "https://login.agid.gov.it/saml/module.php/saml/sp/saml2-acs.php/service");

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck69() throws DatatypeConfigurationException, ConfigurationException, ProcessingException {
        // Given
        String expectedError = "SpidSamlCheck_69";

        Element samlAssertion = samlAssertion();

        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        Element subjectConfirmationData = DocumentUtil.getChildElement(subjectConfirmation, new QName("SubjectConfirmationData"));
        ZonedDateTime to = ZonedDateTime.now().plus(Duration.of(3, TimeUnit.MINUTES.toChronoUnit()));
        String someMinutesFromNow = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(to);
        subjectConfirmationData.setAttribute("NotOnOrAfter", someMinutesFromNow);

        Element issuerElement = DocumentUtil.getChildElement(samlAssertion, new QName("Issuer"));
        issuerElement.getFirstChild().setNodeValue("wrong-value");

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", "https://login.agid.gov.it/saml/module.php/saml/sp/saml2-acs.php/service");

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck7071() throws DatatypeConfigurationException, ConfigurationException, ProcessingException {
        // Given
        String expectedError = "SpidSamlCheck_7071";

        Element samlAssertion = samlAssertion();

        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        Element subjectConfirmationData = DocumentUtil.getChildElement(subjectConfirmation, new QName("SubjectConfirmationData"));
        ZonedDateTime to = ZonedDateTime.now().plus(Duration.of(3, TimeUnit.MINUTES.toChronoUnit()));
        String someMinutesFromNow = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(to);
        subjectConfirmationData.setAttribute("NotOnOrAfter", someMinutesFromNow);

        Element issuerElement = DocumentUtil.getChildElement(samlAssertion, new QName("Issuer"));
        issuerElement.setAttribute("Format", "");

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", "https://login.agid.gov.it/saml/module.php/saml/sp/saml2-acs.php/service");

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck72() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_72";

        Element samlAssertion = samlAssertion();


        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        Element subjectConfirmationData = DocumentUtil.getChildElement(subjectConfirmation, new QName("SubjectConfirmationData"));
        ZonedDateTime to = ZonedDateTime.now().plus(Duration.of(3, TimeUnit.MINUTES.toChronoUnit()));
        String someMinutesFromNow = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(to);
        subjectConfirmationData.setAttribute("NotOnOrAfter", someMinutesFromNow);

        Element issuerElement = DocumentUtil.getChildElement(samlAssertion, new QName("Issuer"));
        issuerElement.setAttribute("Format", "wrong-format");

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", "https://login.agid.gov.it/saml/module.php/saml/sp/saml2-acs.php/service");

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck73() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_73";

        Element samlAssertion = samlAssertion();

        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        Element subjectConfirmationData = DocumentUtil.getChildElement(subjectConfirmation, new QName("SubjectConfirmationData"));
        ZonedDateTime to = ZonedDateTime.now().plus(Duration.of(3, TimeUnit.MINUTES.toChronoUnit()));
        String someMinutesFromNow = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(to);
        subjectConfirmationData.setAttribute("NotOnOrAfter", someMinutesFromNow);

        Element element = DocumentUtil.getChildElement(samlAssertion, new QName("Conditions"));
        element.getParentNode().removeChild(element);
        samlAssertion.appendChild(samlAssertion.getOwnerDocument().createElementNS("saml2", "Conditions"));

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", "https://login.agid.gov.it/saml/module.php/saml/sp/saml2-acs.php/service");

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck74() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_74";

        Element samlAssertion = samlAssertion();


        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        Element subjectConfirmationData = DocumentUtil.getChildElement(subjectConfirmation, new QName("SubjectConfirmationData"));
        ZonedDateTime to = ZonedDateTime.now().plus(Duration.of(3, TimeUnit.MINUTES.toChronoUnit()));
        String someMinutesFromNow = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(to);
        subjectConfirmationData.setAttribute("NotOnOrAfter", someMinutesFromNow);

        Element element = DocumentUtil.getChildElement(samlAssertion, new QName("Conditions"));
        element.getParentNode().removeChild(element);

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", "https://login.agid.gov.it/saml/module.php/saml/sp/saml2-acs.php/service");

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck7576() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_7576";

        Element samlAssertion = samlAssertion();

        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        Element subjectConfirmationData = DocumentUtil.getChildElement(subjectConfirmation, new QName("SubjectConfirmationData"));
        ZonedDateTime to = ZonedDateTime.now().plus(Duration.of(3, TimeUnit.MINUTES.toChronoUnit()));
        String someMinutesFromNow = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(to);
        subjectConfirmationData.setAttribute("NotOnOrAfter", someMinutesFromNow);

        Element element = DocumentUtil.getChildElement(samlAssertion, new QName("Conditions"));
        element.removeAttribute("NotBefore");

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", "https://login.agid.gov.it/saml/module.php/saml/sp/saml2-acs.php/service");

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck78() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_78";

        Element samlAssertion = samlAssertion();

        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        Element subjectConfirmationData = DocumentUtil.getChildElement(subjectConfirmation, new QName("SubjectConfirmationData"));
        ZonedDateTime to = ZonedDateTime.now().plus(Duration.of(3, TimeUnit.MINUTES.toChronoUnit()));
        String someMinutesFromNow = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(to);
        subjectConfirmationData.setAttribute("NotOnOrAfter", someMinutesFromNow);

        Element element = DocumentUtil.getChildElement(samlAssertion, new QName("Conditions"));
        element.setAttribute("NotBefore", "2124-04-10T09:22:28.065Z");

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", "https://login.agid.gov.it/saml/module.php/saml/sp/saml2-acs.php/service");

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck7980() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_7980";

        Element samlAssertion = samlAssertion();

        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        Element subjectConfirmationData = DocumentUtil.getChildElement(subjectConfirmation, new QName("SubjectConfirmationData"));
        ZonedDateTime to = ZonedDateTime.now().plus(Duration.of(3, TimeUnit.MINUTES.toChronoUnit()));
        String someMinutesFromNow = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(to);
        subjectConfirmationData.setAttribute("NotOnOrAfter", someMinutesFromNow);

        Element element = DocumentUtil.getChildElement(samlAssertion, new QName("Conditions"));
        element.removeAttribute("NotOnOrAfter");

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", "https://login.agid.gov.it/saml/module.php/saml/sp/saml2-acs.php/service");

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck82() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_82";

        Element samlAssertion = samlAssertion();

        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        Element subjectConfirmationData = DocumentUtil.getChildElement(subjectConfirmation, new QName("SubjectConfirmationData"));
        ZonedDateTime to = ZonedDateTime.now().plus(Duration.of(3, TimeUnit.MINUTES.toChronoUnit()));
        String someMinutesFromNow = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(to);
        subjectConfirmationData.setAttribute("NotOnOrAfter", someMinutesFromNow);

        Element element = DocumentUtil.getChildElement(samlAssertion, new QName("Conditions"));
        element.setAttribute("NotBefore", "2024-04-10T08:22:28.065Z");

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", "https://login.agid.gov.it/saml/module.php/saml/sp/saml2-acs.php/service");

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck83() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_83";

        Element samlAssertion = samlAssertion();

        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        Element subjectConfirmationData = DocumentUtil.getChildElement(subjectConfirmation, new QName("SubjectConfirmationData"));
        ZonedDateTime to = ZonedDateTime.now().plus(Duration.of(3, TimeUnit.MINUTES.toChronoUnit()));
        String someMinutesFromNow = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(to);
        subjectConfirmationData.setAttribute("NotOnOrAfter", someMinutesFromNow);
        // Confirmation NotOnOrAfter is checked with "now"
        Element element = DocumentUtil.getChildElement(samlAssertion, new QName("Conditions"));
        element.setAttribute("NotOnOrAfter", someMinutesFromNow);

        Element audienceRestriction = DocumentUtil.getChildElement(element, new QName("AudienceRestriction"));
        audienceRestriction.getParentNode().removeChild(audienceRestriction);
        element.appendChild(samlAssertion.getOwnerDocument().createElementNS("saml", "AudienceRestriction"));

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", "https://login.agid.gov.it/saml/module.php/saml/sp/saml2-acs.php/service");

        // Then
        assertEquals(expectedError, actualError);
    }


    @Test
    void raiseSpidSamlCheck8586() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_8586";

        Element samlAssertion = samlAssertion();

        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        Element subjectConfirmationData = DocumentUtil.getChildElement(subjectConfirmation, new QName("SubjectConfirmationData"));
        ZonedDateTime to = ZonedDateTime.now().plus(Duration.of(3, TimeUnit.MINUTES.toChronoUnit()));
        String someMinutesFromNow = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(to);
        subjectConfirmationData.setAttribute("NotOnOrAfter", someMinutesFromNow);
        // Confirmation NotOnOrAfter is checked with "now"
        Element element = DocumentUtil.getChildElement(samlAssertion, new QName("Conditions"));
        element.setAttribute("NotOnOrAfter", someMinutesFromNow);

        Element audienceRestriction = DocumentUtil.getChildElement(element, new QName("AudienceRestriction"));
        Element audience = DocumentUtil.getChildElement(audienceRestriction, new QName("Audience"));
        audience.getParentNode().removeChild(audience);
        audienceRestriction.appendChild(samlAssertion.getOwnerDocument().createElement("DummyElement"));

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", "https://login.agid.gov.it/saml/module.php/saml/sp/saml2-acs.php/service");

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck87() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_87";

        Element samlAssertion = samlAssertion();

        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        Element subjectConfirmationData = DocumentUtil.getChildElement(subjectConfirmation, new QName("SubjectConfirmationData"));
        ZonedDateTime to = ZonedDateTime.now().plus(Duration.of(3, TimeUnit.MINUTES.toChronoUnit()));
        String someMinutesFromNow = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(to);
        subjectConfirmationData.setAttribute("NotOnOrAfter", someMinutesFromNow);
        // Confirmation NotOnOrAfter is checked with "now"
        Element element = DocumentUtil.getChildElement(samlAssertion, new QName("Conditions"));
        element.setAttribute("NotOnOrAfter", someMinutesFromNow);

        Element audienceRestriction = DocumentUtil.getChildElement(element, new QName("AudienceRestriction"));
        Element audience = DocumentUtil.getChildElement(audienceRestriction, new QName("Audience"));
        audience.getFirstChild().setNodeValue("wrong-value");

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", "https://login.agid.gov.it/saml/module.php/saml/sp/saml2-acs.php/service");

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck88() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_88";

        Element samlAssertion = samlAssertion();

        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        Element subjectConfirmationData = DocumentUtil.getChildElement(subjectConfirmation, new QName("SubjectConfirmationData"));
        ZonedDateTime to = ZonedDateTime.now().plus(Duration.of(3, TimeUnit.MINUTES.toChronoUnit()));
        String someMinutesFromNow = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(to);
        subjectConfirmationData.setAttribute("NotOnOrAfter", someMinutesFromNow);
        // Confirmation NotOnOrAfter is checked with "now"
        Element conditions = DocumentUtil.getChildElement(samlAssertion, new QName("Conditions"));
        conditions.setAttribute("NotOnOrAfter", someMinutesFromNow);

        Element authnStatement = DocumentUtil.getChildElement(samlAssertion, new QName("AuthnStatement"));
        authnStatement.getParentNode().removeChild(authnStatement);
        samlAssertion.appendChild(samlAssertion.getOwnerDocument().createElementNS("saml2", "AuthnStatement"));

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", "https://login.agid.gov.it/saml/module.php/saml/sp/saml2-acs.php/service");

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck89() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_89";

        Element samlAssertion = samlAssertion();

        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        Element subjectConfirmationData = DocumentUtil.getChildElement(subjectConfirmation, new QName("SubjectConfirmationData"));
        ZonedDateTime to = ZonedDateTime.now().plus(Duration.of(3, TimeUnit.MINUTES.toChronoUnit()));
        String someMinutesFromNow = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(to);
        subjectConfirmationData.setAttribute("NotOnOrAfter", someMinutesFromNow);
        // Confirmation NotOnOrAfter is checked with "now"
        Element conditions = DocumentUtil.getChildElement(samlAssertion, new QName("Conditions"));
        conditions.setAttribute("NotOnOrAfter", someMinutesFromNow);

        Element authnStatement = DocumentUtil.getChildElement(samlAssertion, new QName("AuthnStatement"));
        authnStatement.getParentNode().removeChild(authnStatement);

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", "https://login.agid.gov.it/saml/module.php/saml/sp/saml2-acs.php/service");

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck90() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_90";

        Element samlAssertion = samlAssertion();


        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        Element subjectConfirmationData = DocumentUtil.getChildElement(subjectConfirmation, new QName("SubjectConfirmationData"));
        ZonedDateTime to = ZonedDateTime.now().plus(Duration.of(3, TimeUnit.MINUTES.toChronoUnit()));
        String someMinutesFromNow = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(to);
        subjectConfirmationData.setAttribute("NotOnOrAfter", someMinutesFromNow);
        // Confirmation NotOnOrAfter is checked with "now"
        Element conditions = DocumentUtil.getChildElement(samlAssertion, new QName("Conditions"));
        conditions.setAttribute("NotOnOrAfter", someMinutesFromNow);

        Element authnStatement = DocumentUtil.getChildElement(samlAssertion, new QName("AuthnStatement"));
        Element authnContext = DocumentUtil.getChildElement(authnStatement, new QName("AuthnContext"));
        authnContext.getParentNode().removeChild(authnContext);
        authnStatement.appendChild(samlAssertion.getOwnerDocument().createElementNS("saml2", "AuthnContext"));

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", "https://login.agid.gov.it/saml/module.php/saml/sp/saml2-acs.php/service");

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck91() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_91";

        Element samlAssertion = samlAssertion();


        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        Element subjectConfirmationData = DocumentUtil.getChildElement(subjectConfirmation, new QName("SubjectConfirmationData"));
        ZonedDateTime to = ZonedDateTime.now().plus(Duration.of(3, TimeUnit.MINUTES.toChronoUnit()));
        String someMinutesFromNow = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(to);
        subjectConfirmationData.setAttribute("NotOnOrAfter", someMinutesFromNow);
        // Confirmation NotOnOrAfter is checked with "now"
        Element conditions = DocumentUtil.getChildElement(samlAssertion, new QName("Conditions"));
        conditions.setAttribute("NotOnOrAfter", someMinutesFromNow);

        Element authnStatement = DocumentUtil.getChildElement(samlAssertion, new QName("AuthnStatement"));
        Element authnContext = DocumentUtil.getChildElement(authnStatement, new QName("AuthnContext"));
        authnContext.getParentNode().removeChild(authnContext);

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", "https://login.agid.gov.it/saml/module.php/saml/sp/saml2-acs.php/service");

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck92() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_92";

        Element samlAssertion = samlAssertion();

        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        Element subjectConfirmationData = DocumentUtil.getChildElement(subjectConfirmation, new QName("SubjectConfirmationData"));
        ZonedDateTime to = ZonedDateTime.now().plus(Duration.of(3, TimeUnit.MINUTES.toChronoUnit()));
        String someMinutesFromNow = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(to);
        subjectConfirmationData.setAttribute("NotOnOrAfter", someMinutesFromNow);
        // Confirmation NotOnOrAfter is checked with "now"
        Element conditions = DocumentUtil.getChildElement(samlAssertion, new QName("Conditions"));
        conditions.setAttribute("NotOnOrAfter", someMinutesFromNow);

        Element authnStatement = DocumentUtil.getChildElement(samlAssertion, new QName("AuthnStatement"));
        Element authnContext = DocumentUtil.getChildElement(authnStatement, new QName("AuthnContext"));
        Element authnContextClassRef = DocumentUtil.getChildElement(authnContext, new QName("AuthnContextClassRef"));
        authnContextClassRef.getFirstChild().setNodeValue("");

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", "https://login.agid.gov.it/saml/module.php/saml/sp/saml2-acs.php/service");

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck93() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_93";

        Element samlAssertion = samlAssertion();

        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        Element subjectConfirmationData = DocumentUtil.getChildElement(subjectConfirmation, new QName("SubjectConfirmationData"));
        ZonedDateTime to = ZonedDateTime.now().plus(Duration.of(3, TimeUnit.MINUTES.toChronoUnit()));
        String someMinutesFromNow = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(to);
        subjectConfirmationData.setAttribute("NotOnOrAfter", someMinutesFromNow);
        // Confirmation NotOnOrAfter is checked with "now"
        Element conditions = DocumentUtil.getChildElement(samlAssertion, new QName("Conditions"));
        conditions.setAttribute("NotOnOrAfter", someMinutesFromNow);

        Element authnStatement = DocumentUtil.getChildElement(samlAssertion, new QName("AuthnStatement"));
        Element authnContext = DocumentUtil.getChildElement(authnStatement, new QName("AuthnContext"));
        Element authnContextClassRef = DocumentUtil.getChildElement(authnContext, new QName("AuthnContextClassRef"));
        authnContextClassRef.getParentNode().removeChild(authnContextClassRef);
        authnContext.appendChild(samlAssertion.getOwnerDocument().createElement("DummyElement"));

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", "https://login.agid.gov.it/saml/module.php/saml/sp/saml2-acs.php/service");

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseSpidSamlCheck94() throws DatatypeConfigurationException {
        // Given
        String expectedError = "SpidSamlCheck_94";

        Element samlAssertion = samlAssertion();

        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        Element subjectConfirmationData = DocumentUtil.getChildElement(subjectConfirmation, new QName("SubjectConfirmationData"));
        ZonedDateTime to = ZonedDateTime.now().plus(Duration.of(3, TimeUnit.MINUTES.toChronoUnit()));
        String someMinutesFromNow = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(to);
        subjectConfirmationData.setAttribute("NotOnOrAfter", someMinutesFromNow);
        // Confirmation NotOnOrAfter is checked with "now"
        Element conditions = DocumentUtil.getChildElement(samlAssertion, new QName("Conditions"));
        conditions.setAttribute("NotOnOrAfter", someMinutesFromNow);

        Element authnStatement = DocumentUtil.getChildElement(samlAssertion, new QName("AuthnStatement"));
        Element authnContext = DocumentUtil.getChildElement(authnStatement, new QName("AuthnContext"));
        Element authnContextClassRef = DocumentUtil.getChildElement(authnContext, new QName("AuthnContextClassRef"));
        authnContextClassRef.getFirstChild().setNodeValue("wrong-value");

        // When
        String actualError = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", "https://login.agid.gov.it/saml/module.php/saml/sp/saml2-acs.php/service");

        // Then
        assertEquals(expectedError, actualError);
    }

    @Test
    void raiseNoError() throws DatatypeConfigurationException {
        // Given
        Element samlAssertion = samlAssertion();

        Element subject = DocumentUtil.getChildElement(samlAssertion, new QName("Subject"));
        Element subjectConfirmation = DocumentUtil.getChildElement(subject, new QName("SubjectConfirmation"));
        Element subjectConfirmationData = DocumentUtil.getChildElement(subjectConfirmation, new QName("SubjectConfirmationData"));
        ZonedDateTime to = ZonedDateTime.now().plus(Duration.of(3, TimeUnit.MINUTES.toChronoUnit()));
        String someMinutesFromNow = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(to);
        subjectConfirmationData.setAttribute("NotOnOrAfter", someMinutesFromNow);
        // Confirmation NotOnOrAfter is checked with "now"
        Element conditions = DocumentUtil.getChildElement(samlAssertion, new QName("Conditions"));
        conditions.setAttribute("NotOnOrAfter", someMinutesFromNow);

        // When
        String actualResult = helper.verifySpidResponse(samlResponseElement(), samlAssertion, "spid-php_4be997744d3fde7b019fcfcae44d295ab3adb82c21", CompleteResponseType(), "2024-04-10T09:22:28.000Z", "https://login.agid.gov.it/saml/module.php/saml/sp/saml2-acs.php/service");

        // Then
        assertNull(actualResult);

    }

    private void printDoc(Document document) {
        try {
            System.out.println("Document:\n" + DocumentUtil.getDocumentAsString(document));
        } catch (ProcessingException | ConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private void printDoc(Element element) {
        printDoc(element.getOwnerDocument());
    }


}