package io.stardog.starwizard.mongodb.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.bson.types.ObjectId;
import org.junit.Test;

import static org.junit.Assert.*;

public class ObjectIdSerializerTest {
    @Test
    public void serialize() throws Exception {
        ObjectId objectId = new ObjectId("1234567890abcdef12345678");
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(ObjectId.class, new ObjectIdSerializer());
        mapper.registerModule(module);
        assertEquals("\"1234567890abcdef12345678\"", mapper.writeValueAsString(objectId));
    }
}
