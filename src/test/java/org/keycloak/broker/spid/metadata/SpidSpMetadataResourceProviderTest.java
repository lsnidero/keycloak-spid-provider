package org.keycloak.broker.spid.metadata;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.DefaultKeycloakSession;
import org.keycloak.services.DefaultKeycloakSessionFactory;

import jakarta.ws.rs.core.Response;
import org.mockito.Mockito;

class SpidSpMetadataResourceProviderTest {

    private SpidSpMetadataResourceProvider resourceProvider;

    @BeforeEach
    void init() {

        resourceProvider = new SpidSpMetadataResourceProvider(Mockito.mock(KeycloakSession.class));

    }

    @Test
    void testGetResource() {
        Object actual = resourceProvider.getResource();
        assertInstanceOf(SpidSpMetadataResourceProvider.class, actual);
    }

}