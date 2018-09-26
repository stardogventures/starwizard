package io.stardog.starwizard.services.geocode.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * Geopoint is a data object that represents a particular point in space. It Jackson-serializes to/from the GeoJSON format.
 */
@AutoValue
@JsonPropertyOrder({"type", "coordinates"})
public abstract class Geopoint {
    public String getType() {
        return "Point";
    }

    public abstract List<Double> getCoordinates();

    @JsonIgnore
    public double getLng() {
        return getCoordinates().get(0);
    }

    @JsonIgnore
    public double getLat() {
        return getCoordinates().get(1);
    }

    /**
     * Return the distance, in meters, from this point to another Geopoint.
     * From: http://stackoverflow.com/questions/3694380/calculating-distance-between-two-points-using-latitude-longitude-what-am-i-doi
     * @param other another geopoint
     * @return  distance in meters
     */
    @JsonIgnore
    public double getDistanceTo(Geopoint other) {
        double lat1 = getLat();
        double lon1 = getLng();
        double lat2 = other.getLat();
        double lon2 = other.getLng();
        double el1 = 0;
        double el2 = 0;

        final int R = 6371; // Radius of the earth

        Double latDistance = Math.toRadians(lat2 - lat1);
        Double lonDistance = Math.toRadians(lon2 - lon1);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }

    public static Geopoint of(double lng, double lat) {
        if (Math.abs(lng) > 180) {
            throw new IllegalArgumentException("invalid longitude: " + lng);
        }
        if (Math.abs(lat) > 90) {
            throw new IllegalArgumentException("invalid latitude: " + lat);
        }
        return new AutoValue_Geopoint(ImmutableList.of(lng, lat));
    }

    @JsonCreator
    public static Geopoint of(@JsonProperty("type") String type, @JsonProperty("coordinates") List<Double> coordinates) {
        if (!"Point".equals(type)) {
            throw new IllegalArgumentException("Must be type 'Point'");
        }
        if (coordinates.size() != 2) {
            throw new IllegalArgumentException("Must have two coordinates");
        }
        return Geopoint.of(coordinates.get(0), coordinates.get(1));
    }
}
