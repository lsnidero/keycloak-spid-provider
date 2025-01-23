package org.keycloak.broker.spid.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.DefaultKeycloakSession;
import org.keycloak.services.DefaultKeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.mockito.Mockito;

class SpidSpMetadataResourceProviderFactoryTest {

    private final SpidSpMetadataResourceProviderFactory factory = new SpidSpMetadataResourceProviderFactory();

    @Test
    void testCreate() {
        RealmResourceProvider actualRealmResourceProvider = factory.create(keycloakSession());
        assertInstanceOf(SpidSpMetadataResourceProvider.class, actualRealmResourceProvider);
    }

    @Test
    void testGetId() {
        String actual = factory.getId();
        assertEquals(SpidSpMetadataResourceProviderFactory.ID, actual);
    }

    private KeycloakSession keycloakSession() {
        return Mockito.mock(KeycloakSession.class);
    }
}
