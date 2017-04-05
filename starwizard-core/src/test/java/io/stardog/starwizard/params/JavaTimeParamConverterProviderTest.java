package io.stardog.starwizard.params;

import org.junit.Test;
import static org.junit.Assert.*;

import javax.ws.rs.ext.ParamConverter;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;

public class JavaTimeParamConverterProviderTest {
    @Test
    public void getLocalDateConverter() throws Exception {
        JavaTimeParamConverterProvider provider = new JavaTimeParamConverterProvider();
        ParamConverter<LocalDate> converter = provider.getConverter(LocalDate.class, null, null);
        assertNull(converter.fromString(null));
        assertEquals(LocalDate.of(2017, 5, 12), converter.fromString("2017-05-12"));
        assertEquals("2017-05-12", converter.toString(LocalDate.of(2017, 5, 12)));
    }

    @Test
    public void getInstantConverter() throws Exception {
        JavaTimeParamConverterProvider provider = new JavaTimeParamConverterProvider();
        ParamConverter<Instant> converter = provider.getConverter(Instant.class, null, null);
        assertNull(converter.fromString(null));

        Instant instant = Instant.parse("2017-04-05T14:44:16.677Z"); // epoch milli 1491403456677
        assertEquals(instant, converter.fromString("2017-04-05T14:44:16.677Z"));
        assertEquals(instant, converter.fromString("1491403456677"));
        assertEquals("2017-04-05T14:44:16.677Z", converter.toString(instant));
    }

    @Test
    public void getOther() throws Exception {
        JavaTimeParamConverterProvider provider = new JavaTimeParamConverterProvider();
        assertNull(provider.getConverter(Date.class, null, null));
    }
}
