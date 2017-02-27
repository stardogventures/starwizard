package io.stardog.starwizard.params;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;

public class JavaTimeParamConverterProvider implements ParamConverterProvider {
    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        if (rawType.equals(LocalDate.class)) {
            return new ParamConverter<T>() {
                @Override
                public T fromString(String s) {
                    if (s == null) {
                        return null;
                    }
                    return rawType.cast(LocalDate.parse(s));
                }

                @Override
                public String toString(T t) {
                    return t.toString();
                }
            };
        } else if (rawType.equals(Instant.class)) {
            return new ParamConverter<T>() {
                @Override
                public T fromString(String s) {
                    if (s == null) {
                        return null;
                    }
                    try {
                        return rawType.cast(Instant.ofEpochMilli(Long.parseLong(s)));
                    } catch (NumberFormatException e) {
                        return rawType.cast(Instant.parse(s));
                    }
                }

                @Override
                public String toString(T t) {
                    return t.toString();
                }
            };
        }
        return null;
    }
}
