package io.stardog.starwizard.services.geocode.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GeopointTest {
    @Test
    public void testJson() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Geopoint point = Geopoint.of(-73.9857, 40.7484);
        String json = mapper.writeValueAsString(point);

        String expected = "{\"type\":\"Point\",\"coordinates\":[-73.9857,40.7484]}";

        assertEquals(expected, json);
        Geopoint back = mapper.readValue(json, Geopoint.class);
        assertEquals(back, point);
    }
}