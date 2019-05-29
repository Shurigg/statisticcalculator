package com.nngu.fqw.statisticcalculator.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

public class TimeSeriesStatistic {
    private Duration duration;
    private Integer windowSize;
    private Double minSigmaDeviation;
    private Map<LocalDateTime, Double> timeSeries;

    public TimeSeriesStatistic() {
    }

    public TimeSeriesStatistic(Duration duration, Integer windowSize, Double minSigmaDeviation, Map<LocalDateTime, Double> timeSeries) {
        this.duration = duration;
        this.windowSize = windowSize;
        this.minSigmaDeviation = minSigmaDeviation;
        this.timeSeries = timeSeries;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public Integer getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(Integer windowSize) {
        this.windowSize = windowSize;
    }

    public Double getMinSigmaDeviation() {
        return minSigmaDeviation;
    }

    public void setMinSigmaDeviation(Double minSigmaDeviation) {
        this.minSigmaDeviation = minSigmaDeviation;
    }

    public Map<LocalDateTime, Double> getTimeSeries() {
        return timeSeries;
    }

    public void setTimeSeries(Map<LocalDateTime, Double> timeSeries) {
        this.timeSeries = timeSeries;
    }

    @Override
    public String toString() {
        return "TimeSeriesStatistic{" +
                "duration=" + duration +
                ", windowSize=" + windowSize +
                ", minSigmaDeviation=" + minSigmaDeviation +
                '}';
    }
}
