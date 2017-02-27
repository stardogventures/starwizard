package io.stardog.starwizard.mongodb.swagger;

import com.fasterxml.jackson.databind.JavaType;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.models.Model;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.StringProperty;
import io.swagger.util.Json;
import org.bson.types.ObjectId;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;

/**
 * A Swagger ModelConverter for ObjectIds. Forces ObjectIds to be treated as Strings.
 */
public class ObjectIdConverter implements ModelConverter {
    @Override
    public Property resolveProperty(Type type, ModelConverterContext context, Annotation[] annotations, Iterator<ModelConverter> chain) {
        final JavaType jType = Json.mapper().constructType(type);
        if (jType != null) {
            final Class<?> cls = jType.getRawClass();
            if (cls.equals(ObjectId.class)) {
                StringProperty property = new StringProperty();
                property.setExample("588f7ee98f138b19220041a7");
                return property;
            }
        }
        if (chain.hasNext()) {
            return chain.next().resolveProperty(type, context, annotations, chain);
        } else {
            return null;
        }
    }

    @Override
    public Model resolve(Type type, ModelConverterContext context, Iterator<ModelConverter> chain) {
        if (chain.hasNext()) {
            return chain.next().resolve(type, context, chain);
        } else  {
            return null;
        }
    }
}
