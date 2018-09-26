package io.stardog.starwizard.services.geocode;

import io.stardog.starwizard.services.geocode.data.Geopoint;

import java.util.Optional;

public interface GeocodeService {
    Optional<Geopoint> geocode(String string);
}
