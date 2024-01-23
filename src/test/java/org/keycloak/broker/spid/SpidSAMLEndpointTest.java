package org.keycloak.broker.spid;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.keycloak.broker.provider.IdentityProvider.AuthenticationCallback;
import org.keycloak.common.ClientConnection;
import org.keycloak.http.HttpRequest;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.saml.validators.DestinationValidator;
import org.keycloak.services.DefaultKeycloakSession;
import org.keycloak.services.DefaultKeycloakSessionFactory;
import org.mockito.Mockito;

import static io.restassured.RestAssured.given;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.inject.Produces;

@QuarkusTest
@Disabled
class SpidSAMLEndpointTest {

    @Test
    void testGetSPDescriptor() {
        given()
                .when().get("/descriptor")
                .then()
                .statusCode(200);

    }

    @Produces
    HttpRequest httpRequest() {
        return Mockito.mock(HttpRequest.class);
    }

    @Produces
    DestinationValidator destinationValidator() {
        return DestinationValidator.forProtocolMap(new String[] { "protocol1", "protocol2" });
    }

    @Produces
    KeycloakSession keycloakSession() {

        KeycloakSession session = new DefaultKeycloakSession(new DefaultKeycloakSessionFactory());
        session.setAttribute("TestAttribute", "only4Testing");

        return session;
    }

    @Produces
    SpidIdentityProviderConfig spidIdentityProviderConfig() {
        return new SpidIdentityProviderConfig();
    }

    @Produces
    SpidIdentityProvider spidIdentityProvider() {
        return new SpidIdentityProvider(keycloakSession(), spidIdentityProviderConfig(), destinationValidator());
    }

    @Produces
    RealmModel realmModel() {
        return Mockito.mock(RealmModel.class);
    }

    @Produces
    AuthenticationCallback authenticationCallback() {
        return Mockito.mock(AuthenticationCallback.class);
    }

    @Produces
    ClientConnection clientConnection() {
        return Mockito.mock(ClientConnection.class);
    }

    /*
     * @Test
     * void testPostBinding() {
     * 
     * }
     * 
     * @Test
     * void testPostBinding2() {
     * 
     * }
     * 
     * @Test
     * void testRedirectBinding() {
     * 
     * }
     * 
     * @Test
     * void testRedirectBinding2() {
     * 
     * }
     */
}
