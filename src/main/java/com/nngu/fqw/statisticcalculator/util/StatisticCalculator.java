package com.nngu.fqw.statisticcalculator.util;

import com.nngu.fqw.statisticcalculator.model.WindowInfo;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

import static java.math.BigDecimal.ROUND_HALF_UP;

@Component
public class StatisticCalculator {

    private static final int SCALE = 10;

    public Map<Integer, Double> getTimeSeriesInfoByWindowSize(Double[] timeSeries) {
        Map<Integer, Double> result = new TreeMap<>();
        BigDecimal sigma = getSigma(timeSeries);
        for (int windowSize = 2; windowSize < timeSeries.length; windowSize++) {
            List<WindowInfo> windowInfos = new ArrayList<>();
            for (int startWindow = 0, endWindow = startWindow + windowSize; endWindow < timeSeries.length; startWindow++, endWindow++) {
                WindowInfo wInfo = getWindowInfo(Arrays.copyOfRange(timeSeries, startWindow, endWindow), sigma);
                windowInfos.add(wInfo);
                if (wInfo.getHasAttack()) {
                    break;
                }
            }
            if (!windowInfos.get(windowInfos.size() - 1).getHasAttack()) {
                double min = Double.MAX_VALUE;
                for (WindowInfo wInfo : windowInfos) {
                    min = Math.min(min, wInfo.getMinSigmaDeviation());
                }
                result.put(windowSize, min);
            }
        }
        return result;
    }

    private BigDecimal getSigma(Double[] values) {
        Double sum = 0.0;
        for (Double value : values) {
            sum += value;
        }
        BigDecimal avg = BigDecimal.valueOf(sum).divide(BigDecimal.valueOf(values.length), SCALE, ROUND_HALF_UP);
        BigDecimal sumOfSqrDeviation = BigDecimal.ZERO;
        for (Double value : values) {
            BigDecimal sqrDeviation = BigDecimal.valueOf(value).subtract(avg).pow(2);
            sumOfSqrDeviation = sumOfSqrDeviation.add(sqrDeviation);
        }
        BigDecimal avgOfSqrDeviation = sumOfSqrDeviation.divide(BigDecimal.valueOf(values.length), SCALE, ROUND_HALF_UP);
        return sqrt(avgOfSqrDeviation);
    }

    private WindowInfo getWindowInfo(Double[] values, final BigDecimal sigma) {
        Double sum = 0.0;
        for (Double value : values) {
            sum += value;
        }
        BigDecimal avg = BigDecimal.valueOf(sum).divide(BigDecimal.valueOf(values.length), SCALE, ROUND_HALF_UP);
        boolean hasAttack = false;
        BigDecimal min = sigma;
        for (Double value : values) {
            BigDecimal deviation = BigDecimal.valueOf(value).subtract(avg);
            if (deviation.abs().compareTo(sigma.abs()) <= 0) {
                BigDecimal sigmaDeviation = sigma.abs().subtract(deviation.abs());
                if (!hasAttack) {
                    min = min(min, sigmaDeviation);
                }
            } else {
                BigDecimal sigmaDeviation = deviation.abs().subtract(sigma.abs());
                if (hasAttack) {
                    min = min(min, sigmaDeviation);
                } else {
                    hasAttack = true;
                    min = sigmaDeviation;
                }
            }
        }
        return new WindowInfo(hasAttack, min.doubleValue());
    }

    private BigDecimal min(BigDecimal first, BigDecimal second) {
        if (first.compareTo(second) <= 0) {
            return first;
        }
        return second;
    }

    private BigDecimal sqrt(BigDecimal A) {
        final BigDecimal TWO = BigDecimal.valueOf(2);
        BigDecimal x0 = BigDecimal.ZERO;
        BigDecimal x1 = new BigDecimal(Math.sqrt(A.doubleValue()));
        while (!x0.equals(x1)) {
            x0 = x1;
            x1 = A.divide(x0, SCALE, ROUND_HALF_UP);
            x1 = x1.add(x0);
            x1 = x1.divide(TWO, SCALE, ROUND_HALF_UP);

        }
        return x1;
    }
}
