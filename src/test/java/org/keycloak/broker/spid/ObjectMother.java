package org.keycloak.broker.spid;

import org.keycloak.dom.saml.v2.assertion.AssertionType;
import org.keycloak.dom.saml.v2.assertion.NameIDType;
import org.keycloak.dom.saml.v2.protocol.ResponseType;
import org.keycloak.dom.saml.v2.protocol.StatusCodeType;
import org.keycloak.dom.saml.v2.protocol.StatusType;
import org.keycloak.saml.common.exceptions.ConfigurationException;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.exceptions.ProcessingException;
import org.keycloak.saml.common.util.DocumentUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

public class ObjectMother {

    public static class SamlResponseTypes {

        public static Element samlResponseElement(){
            return samlResponse().getDocumentElement();
        }

        private static Document samlResponse() {
            try {
                return DocumentUtil.getDocument(Files.newInputStream(Path.of("src/test/resources/saml_response.xml")));
            } catch (ParsingException | IOException | ConfigurationException | ProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        public static Element samlAssertion() {
            return DocumentUtil.getElement(samlResponse(), new QName("Assertion"));
        }

        public static ResponseType CompleteResponseType(String id, String responseTime) {
            try {
                Document samlResponse = samlResponse();
                Element samlAssertion = samlAssertion();

                XMLGregorianCalendar responseTimeCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(responseTime);
                ResponseType responseType = new ResponseType(id, responseTimeCalendar);
                responseType.setSignature(DocumentUtil.getElement(samlResponse, new QName("Signature")));
                responseType.addAssertion(new ResponseType.RTChoiceType(new AssertionType("id", responseTimeCalendar)));
                responseType.getAssertions().get(0).getAssertion().setSignature(DocumentUtil.getChildElement(samlAssertion, new QName("Signature")));
                StatusType statusType = new StatusType();
                StatusCodeType statusCodeType = new StatusCodeType();
                statusCodeType.setValue(URI.create("urn:oasis:names:tc:SAML:2.0:status:Success"));
                statusType.setStatusCode(statusCodeType);
                responseType.setStatus(statusType);
                NameIDType issuer = new NameIDType();
                issuer.setValue("https://id.lepida.it/idp/shibboleth");
                issuer.setFormat(URI.create(SpidSAMLEndpoint.ISSUER_FORMAT));
                responseType.setIssuer(issuer);
                return responseType;
            } catch (DatatypeConfigurationException e) {
                throw new RuntimeException(e);
            }
        }

        public static ResponseType CompleteResponseType() {
            return CompleteResponseType("id", "2024-04-10T09:23:28.000Z");
        }
    }
}
