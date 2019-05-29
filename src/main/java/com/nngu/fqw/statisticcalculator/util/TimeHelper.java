package com.nngu.fqw.statisticcalculator.util;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.temporal.TemporalUnit;

import static java.time.temporal.ChronoUnit.*;

@Component
public class TimeHelper {
    public Duration getNextDuration(Duration currentDuration) {
        if (currentDuration.compareTo(YEARS.getDuration()) > 0) {
            return MONTHS.getDuration().multipliedBy(getCountOfUnitMinusSecond(currentDuration, MONTHS));
        }
        if (currentDuration.compareTo(MONTHS.getDuration()) > 0) {
            return WEEKS.getDuration().multipliedBy(getCountOfUnitMinusSecond(currentDuration, WEEKS));
        }
        if (currentDuration.compareTo(WEEKS.getDuration()) > 0) {
            return DAYS.getDuration().multipliedBy(getCountOfUnitMinusSecond(currentDuration, DAYS));
        }
        if (currentDuration.compareTo(DAYS.getDuration()) > 0) {
            return HOURS.getDuration().multipliedBy(getCountOfUnitMinusSecond(currentDuration, HOURS));
        }
        if (currentDuration.compareTo(HOURS.getDuration()) > 0) {
            return MINUTES.getDuration().multipliedBy(getCountOfUnitMinusSecond(currentDuration, MINUTES));
        }
        if (currentDuration.compareTo(MINUTES.getDuration()) > 0) {
            return SECONDS.getDuration().multipliedBy(getCountOfUnitMinusSecond(currentDuration, SECONDS));
        }
        return null;
    }

    private long getCountOfUnitMinusSecond(Duration currentDuration, TemporalUnit unit) {
        return currentDuration.minusSeconds(1).getSeconds() / unit.getDuration().getSeconds();
    }
}
