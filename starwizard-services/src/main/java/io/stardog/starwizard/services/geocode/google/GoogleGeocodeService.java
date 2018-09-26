package io.stardog.starwizard.services.geocode.google;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.GeocodingApiRequest;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import io.stardog.starwizard.services.geocode.GeocodeService;
import io.stardog.starwizard.services.geocode.data.Geopoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Optional;

@Singleton
public class GoogleGeocodeService implements GeocodeService {
    private final GeoApiContext geoApiContext;
    private final Logger LOGGER = LoggerFactory.getLogger(GoogleGeocodeService.class);

    @Inject
    public GoogleGeocodeService(GeoApiContext geoApiContext) {
        this.geoApiContext = geoApiContext;
    }

    public Optional<Geopoint> geocode(String addressString) {
        GeocodingApiRequest request = GeocodingApi.geocode(geoApiContext, addressString);
        try {
            GeocodingResult[] result = request.await();
            if (result.length != 0) {
                double lat = result[0].geometry.location.lat;
                double lng = result[0].geometry.location.lng;
                return Optional.of(Geopoint.of(lng, lat));
            } else {
                return Optional.empty();
            }

        } catch (ApiException | InterruptedException | IOException e) {
            LOGGER.warn("Unable to geocode address: " + addressString, e);
            throw new RuntimeException(e);
        }
    }
}
