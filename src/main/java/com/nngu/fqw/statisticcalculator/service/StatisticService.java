package com.nngu.fqw.statisticcalculator.service;

import com.nngu.fqw.statisticcalculator.model.TimeSeriesStatistic;
import com.nngu.fqw.statisticcalculator.model.enums.StatisticType;
import com.nngu.fqw.statisticcalculator.repo.FrameDataRepo;
import com.nngu.fqw.statisticcalculator.util.StatisticCalculator;
import com.nngu.fqw.statisticcalculator.util.TimeHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.*;

@Service
public class StatisticService {

    private static final int THREAD_COUNT = 8;
    private static final int TIMEOUT_IN_HOUR = 16;

    private FrameDataRepo repo;
    private StatisticCalculator calculator;
    private TimeHelper timeHelper;

    @Autowired
    public StatisticService(FrameDataRepo repo, StatisticCalculator calculator, TimeHelper timeHelper) {
        this.repo = repo;
        this.calculator = calculator;
        this.timeHelper = timeHelper;
    }

    public Map<LocalDateTime, Double> getPacketCountStatistic(String protocol, Duration frameSize) {
        LocalDateTime start = repo.findMinTimeOfPacket();
        LocalDateTime end = repo.findMaxTimeOfPacket();

        return getTimeSeries(start, end, protocol, frameSize, StatisticType.COUNT);
    }

    public Map<LocalDateTime, Double> getPacketAverageStatistic(String protocol, Duration frameSize) {
        LocalDateTime start = repo.findMinTimeOfPacket();
        LocalDateTime end = repo.findMaxTimeOfPacket();

        return getTimeSeries(start, end, protocol, frameSize, StatisticType.AVG);
    }

    public TimeSeriesStatistic getWindowStatisticCount(String protocol) throws InterruptedException {
        LocalDateTime start = repo.findMinTimeOfPacket();
        LocalDateTime end = repo.findMaxTimeOfPacket();

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);

        Map<Double, TimeSeriesStatistic> timeSeriesStatistics = new ConcurrentHashMap<>();
        Duration duration = timeHelper.getNextDuration(Duration.between(start, end));
        while (duration != null) {
            Duration currentDuration = duration;
            CompletableFuture
                    .supplyAsync(() -> getTimeSeriesStatistic(start, end, currentDuration, protocol, StatisticType.COUNT), executorService)
                    .thenApply(statistic -> {
                        System.out.println(currentDuration.toString() + " is completed");
                        return timeSeriesStatistics.put(statistic.getMinSigmaDeviation(), statistic);
                    });
            duration = timeHelper.getNextDuration(duration);
        }
        executorService.shutdown();
        executorService.awaitTermination(TIMEOUT_IN_HOUR, TimeUnit.HOURS);
        TimeSeriesStatistic bestStatistic = null;
        for (TimeSeriesStatistic info : timeSeriesStatistics.values()) {
            if (bestStatistic == null) {
                bestStatistic = info;
            } else {
                if (info != null && bestStatistic.getMinSigmaDeviation() > info.getMinSigmaDeviation()) {
                    bestStatistic = info;
                }
            }
        }

        return bestStatistic;
    }

    public TimeSeriesStatistic getWindowStatisticAvg(String protocol) throws InterruptedException {
        LocalDateTime start = repo.findMinTimeOfPacket();
        LocalDateTime end = repo.findMaxTimeOfPacket();

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);

        Map<Double, TimeSeriesStatistic> timeSeriesStatistics = new ConcurrentHashMap<>();
        Duration duration = timeHelper.getNextDuration(Duration.between(start, end));
        while (duration != null) {
            Duration currentDuration = duration;
            CompletableFuture
                    .supplyAsync(() -> getTimeSeriesStatistic(start, end, currentDuration, protocol, StatisticType.AVG), executorService)
                    .thenApply(statistic -> {
                        System.out.println(currentDuration.toString() + " is completed");
                        return timeSeriesStatistics.put(statistic.getMinSigmaDeviation(), statistic);
                    });
            duration = timeHelper.getNextDuration(duration);
        }
        executorService.shutdown();
        executorService.awaitTermination(TIMEOUT_IN_HOUR, TimeUnit.HOURS);
        TimeSeriesStatistic bestStatistic = null;
        for (TimeSeriesStatistic info : timeSeriesStatistics.values()) {
            if (bestStatistic == null) {
                bestStatistic = info;
            } else {
                if (info != null && bestStatistic.getMinSigmaDeviation() > info.getMinSigmaDeviation()) {
                    bestStatistic = info;
                }
            }
        }

        return bestStatistic;
    }

    private TimeSeriesStatistic getTimeSeriesStatistic(LocalDateTime start, LocalDateTime end, Duration duration,
                                                       String protocol, StatisticType type) {
        Map<LocalDateTime, Double> timeSeries = getTimeSeries(start, end, protocol, duration, type);
        Map<Integer, Double> statisticByWindowSize = calculator.getTimeSeriesInfoByWindowSize(timeSeries.values().toArray(new Double[0]));
        return createTimeSeriesInfo(duration, statisticByWindowSize, timeSeries);
    }

    private Map<LocalDateTime, Double> getTimeSeries(LocalDateTime start, LocalDateTime end, String protocol,
                                                     Duration frameSize, StatisticType type) {
        Map<LocalDateTime, Double> statistic = new TreeMap<>();
        LocalDateTime currentStart = start;
        LocalDateTime currentEnd;
        while (currentStart.isBefore(end)) {
            currentEnd = currentStart.plus(frameSize);

            Double count;
            switch (type) {
                case AVG:
                    count = findAverageStatistic(currentStart, currentEnd, protocol);
                    break;
                case COUNT:
                    count = findCountStatistic(currentStart, currentEnd, protocol).doubleValue();
                    break;
                default:
                    count = 0.0;
            }
            statistic.put(currentEnd, count);

            currentStart = currentEnd;
        }

        return statistic;
    }

    private TimeSeriesStatistic createTimeSeriesInfo(Duration duration, Map<Integer, Double> statisticByWindowSize,
                                                     Map<LocalDateTime, Double> timeSeries) {
        if (statisticByWindowSize.isEmpty()) {
            return null;
        }

        Map.Entry<Integer, Double> bestWindowSize = null;
        for (Map.Entry<Integer, Double> entry : statisticByWindowSize.entrySet()) {
            if (bestWindowSize == null) {
                bestWindowSize = entry;
            } else {
                if (bestWindowSize.getValue() > entry.getValue()) {
                    bestWindowSize = entry;
                }
            }
        }
        if (bestWindowSize == null) {
            return null;
        }

        TimeSeriesStatistic info = new TimeSeriesStatistic();

        info.setDuration(duration);
        info.setTimeSeries(timeSeries);
        info.setWindowSize(bestWindowSize.getKey());
        info.setMinSigmaDeviation(bestWindowSize.getValue());

        return info;
    }

    private Integer findCountStatistic(LocalDateTime currentStart, LocalDateTime currentEnd, String protocol) {
        if (StringUtils.isEmpty(protocol)) {
            return repo.findCountStatistic(currentStart, currentEnd);
        } else {
            return repo.findCountStatisticByProtocol(currentStart, currentEnd, protocol);
        }
    }

    private Double findAverageStatistic(LocalDateTime currentStart, LocalDateTime currentEnd, String protocol) {
        if (StringUtils.isEmpty(protocol)) {
            return repo.findAvgStatistic(currentStart, currentEnd);
        } else {
            return repo.findAvgStatisticByProtocol(currentStart, currentEnd, protocol);
        }
    }

    private LocalDateTime getEndOfInterval(final Map<LocalDateTime, Double> statistic) {
//        int arrSize = statistic.size();
//        Double[] values = statistic.values().toArray(new Double[arrSize]);
//
//        Double[] acfs = new Double[arrSize];
//        for (int lag = 1; lag < arrSize - 2; lag++) {
//            BigDecimal sumX = BigDecimal.ZERO;
//            BigDecimal sumXplusLag = BigDecimal.ZERO;
//            BigDecimal sumOfXonXplusLag = BigDecimal.ZERO;
//
//            for (int i = 0; i < arrSize - lag; i++) {
//                sumX = sumX.add(BigDecimal.valueOf(values[i]));
//                sumXplusLag = sumXplusLag.add(BigDecimal.valueOf(values[i + lag]));
//                sumOfXonXplusLag = sumOfXonXplusLag.add(BigDecimal.valueOf(values[i]).multiply(BigDecimal.valueOf(values[i + lag])));
//            }
//
//            BigDecimal avgX = sumX.divide(BigDecimal.valueOf(arrSize - lag), SCALE, ROUND_HALF_UP);
//            BigDecimal avgXplusLag = sumXplusLag.divide(BigDecimal.valueOf(arrSize - lag), SCALE, ROUND_HALF_UP);
//
//            BigDecimal DX = BigDecimal.ZERO;
//            BigDecimal DXplusLag = BigDecimal.ZERO;
//
//            for (int i = 0; i < arrSize - lag; i++) {
//                DX = DX.add((avgX.subtract(BigDecimal.valueOf(values[i]))).pow(2));
//                DXplusLag = DXplusLag.add((avgXplusLag.subtract(BigDecimal.valueOf(values[i + lag]))).pow(2));
//            }
//
//            DX = DX.divide(BigDecimal.valueOf(arrSize - lag), SCALE, ROUND_HALF_UP);
//            DXplusLag = DXplusLag.divide(BigDecimal.valueOf(arrSize - lag), SCALE, ROUND_HALF_UP);
//
//            BigDecimal sigmaX = sqrt(DX);
//            BigDecimal sigmaXplusLag = sqrt(DXplusLag);
//            BigDecimal avgOfXonXplusLag = sumOfXonXplusLag.divide(BigDecimal.valueOf(arrSize - lag), SCALE, ROUND_HALF_UP);
//            BigDecimal cov = avgOfXonXplusLag.subtract(avgX.multiply(avgXplusLag));
//            BigDecimal acf = cov.divide(sigmaX.multiply(sigmaXplusLag), SCALE, ROUND_HALF_UP);
//
//            acfs[lag] = acf.abs().doubleValue();
//        }
//
//        int indOfMax = 0;
//        double maxAcf = 0.7;
//        for (int i = 1; i < arrSize - 2; i++) {
//            if (maxAcf < acfs[i]) {
//                maxAcf = acfs[i];
//                indOfMax = i;
//            }
//        }
//
//        LocalDateTime result = null;
//        if (indOfMax > 0) {
//            result = (LocalDateTime) statistic.keySet().toArray()[indOfMax];
//        }
//
//        return result;

        return null;
    }
}
