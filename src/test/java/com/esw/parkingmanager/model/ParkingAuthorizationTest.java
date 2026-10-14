package com.esw.parkingmanager.model;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class ParkingAuthorizationTest {

    private static final LocalDate DEC_31 = LocalDate.of(2026, 12, 31);

    @Test
    void noExpiryIsAlwaysActive() {
        ParkingAuthorization auth = new ParkingAuthorization();
        assertThat(auth.isActiveOn(DEC_31)).isTrue();
    }

    @Test
    void activeBeforeExpiry() {
        ParkingAuthorization auth = new ParkingAuthorization();
        auth.setValidUntil(DEC_31);
        assertThat(auth.isActiveOn(DEC_31.minusDays(1))).isTrue();
    }

    @Test
    void activeOnItsLastDay() {
        ParkingAuthorization auth = new ParkingAuthorization();
        auth.setValidUntil(DEC_31);
        assertThat(auth.isActiveOn(DEC_31)).isTrue();
    }

    @Test
    void inactiveTheDayAfterExpiry() {
        ParkingAuthorization auth = new ParkingAuthorization();
        auth.setValidUntil(DEC_31);
        assertThat(auth.isActiveOn(DEC_31.plusDays(1))).isFalse();
    }

    @Test
    void stillActiveAt9pmInHoustonEvenThoughItIsAlreadyJan1InUtc() {
        // 03:00 UTC on Jan 1 = 21:00 on Dec 31 in Houston (CST, UTC-6)
        Instant moment = Instant.parse("2027-01-01T03:00:00Z");

        LocalDate houstonToday = LocalDate.now(Clock.fixed(moment, ZoneId.of("America/Chicago")));
        LocalDate utcToday = LocalDate.now(Clock.fixed(moment, ZoneId.of("UTC")));

        ParkingAuthorization auth = new ParkingAuthorization();
        auth.setValidUntil(DEC_31);

        assertThat(auth.isActiveOn(houstonToday)).isTrue();  // correct: still Dec 31 in Houston
        assertThat(auth.isActiveOn(utcToday)).isFalse();     // the bug a UTC server would cause
    }
}