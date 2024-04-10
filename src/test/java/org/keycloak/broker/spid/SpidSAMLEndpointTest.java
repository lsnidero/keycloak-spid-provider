package org.keycloak.broker.spid;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.keycloak.common.ClientConnection;
import org.keycloak.common.util.Resteasy;
import org.keycloak.common.util.ResteasyProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.saml.validators.DestinationValidator;
import org.keycloak.services.DefaultKeycloakSession;
import org.keycloak.services.DefaultKeycloakSessionFactory;
import org.keycloak.services.resources.IdentityBrokerService;
import org.mockito.Mockito;

import javax.inject.Inject;
import java.util.ServiceLoader;

//@QuarkusTest
@Disabled
class SpidSAMLEndpointTest {

    //@Inject
    //KeycloakSession keycloakSession;

    //@Test
    /*
    void testGetSPDescriptor() {
        given()
                .when().get("/descriptor")
                .then()
                .statusCode(200);

    }*/


    //SpidSAMLEndpoint endpoint = Mockito.mock(SpidSAMLEndpoint.class);


    @Test
    void testSpidChecks() {
        /*
        ServiceLoader.load(ResteasyProvider.class);
        ServiceLoader.load(org.keycloak.common.util.Resteasy.class);

        ServiceLoader.load(org.keycloak.broker.spid.SpidIdentityProviderFactory.class);

        ServiceLoader.load(org.keycloak.broker.spid.mappers.SpidUsernameTemplateMapper.class);
        ServiceLoader.load(org.keycloak.broker.spid.mappers.SpidUserAttributeMapper.class);
        ServiceLoader.load(org.keycloak.broker.spid.metadata.SpidSpMetadataResourceProviderFactory.class);

        DefaultKeycloakSessionFactory defaultKeycloakSessionFactory = new DefaultKeycloakSessionFactory();

        KeycloakSession keycloakSession = new DefaultKeycloakSession(defaultKeycloakSessionFactory);


        KeycloakSession keycloakSession = Mockito.mock(KeycloakSession.class);

        SpidIdentityProviderConfig spidIdentityProviderConfig = new SpidIdentityProviderConfig();
        SpidIdentityProvider spidIdentityProvider = new SpidIdentityProviderFactory().create(keycloakSession, spidIdentityProviderConfig);


        IdentityBrokerService callback = new IdentityBrokerService(keycloakSession);
        SpidSAMLEndpoint endpoint = new SpidSAMLEndpoint(keycloakSession, spidIdentityProvider, spidIdentityProviderConfig, callback, DestinationValidator.forProtocolMap(new String[]{"protocol"}));

         */
        //KeycloakSession keycloakSession = Mockito.mock(KeycloakSession.class);

        //var postBinding = new SpidSAMLEndpoint(null,null,null,null,null).new PostBinding();

    }

 /*
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
