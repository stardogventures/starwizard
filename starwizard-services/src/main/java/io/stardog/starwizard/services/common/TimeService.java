package io.stardog.starwizard.services.common;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Injectable service for getting the current time and date - can override the concept of "now" for test purposes.
 */
@Singleton
public class TimeService {
    private final ZoneId timezone;
    private Instant now = null;

    @Inject
    public TimeService(@Named("timezone") ZoneId timezone) {
        this.timezone = timezone;
    }

    public void setToday(LocalDate today) {
        setNow(today.atStartOfDay(timezone).toInstant());
    }

    public void setNow(Instant now) {
        this.now = now;
    }

    public Instant getNow() {
        return now != null ? now : Instant.now();
    }

    public LocalDate getToday() {
        return toDate(getNow());
    }

    public LocalDate toDate(Instant at) {
        return at.atZone(timezone).toLocalDate();
    }

    public Instant toInstant(LocalDate date) { return date.atStartOfDay(timezone).toInstant(); }
}
