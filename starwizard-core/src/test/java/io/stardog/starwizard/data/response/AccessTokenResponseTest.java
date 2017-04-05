package io.stardog.starwizard.data.response;

import org.junit.Test;

import static org.junit.Assert.*;

public class AccessTokenResponseTest {
    @Test
    public void of() throws Exception {
        AccessTokenResponse response = AccessTokenResponse.of("token", "bearer", 3600);
        assertEquals("token", response.getAccessToken());
        assertEquals("bearer", response.getTokenType());
        assertEquals(3600, response.getExpiresIn());
        assertFalse(response.getRefreshToken().isPresent());
        assertFalse(response.getScope().isPresent());
    }

    @Test
    public void of1() throws Exception {
        AccessTokenResponse response = AccessTokenResponse.of("token", "bearer", 3600, "refresh", "default");
        assertEquals("token", response.getAccessToken());
        assertEquals("bearer", response.getTokenType());
        assertEquals(3600, response.getExpiresIn());
        assertEquals("refresh", response.getRefreshToken().get());
        assertEquals("default", response.getScope().get());
    }

}
