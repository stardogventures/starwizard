package io.stardog.starwizard.mongodb.swagger;

import io.swagger.converter.ModelConverter;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.StringProperty;
import org.bson.types.ObjectId;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ObjectIdConverterTest {
    @Test
    public void resolveProperty() throws Exception {
        ObjectIdConverter converter = new ObjectIdConverter();

        List<ModelConverter> others = new ArrayList<>();
        Annotation[] annotations = new Annotation[]{};
        Property property = converter.resolveProperty(ObjectId.class, null, annotations, others.iterator());
        assertTrue(property instanceof StringProperty);
        assertEquals("588f7ee98f138b19220041a7", property.getExample());
    }
}
