package com.nngu.fqw.statisticcalculator.util;

import com.nngu.fqw.statisticcalculator.model.WindowInfo;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    public LocalDateTime getEndOfInterval(final Map<LocalDateTime, Double> statistic) {
        int arrSize = statistic.size();
        Double[] values = statistic.values().toArray(new Double[arrSize]);

        Double[] acfs = new Double[arrSize];
        for (int lag = 1; lag < arrSize - 2; lag++) {
            BigDecimal sumX = BigDecimal.ZERO;
            BigDecimal sumXplusLag = BigDecimal.ZERO;
            BigDecimal sumOfXonXplusLag = BigDecimal.ZERO;

            for (int i = 0; i < arrSize - lag; i++) {
                sumX = sumX.add(BigDecimal.valueOf(values[i]));
                sumXplusLag = sumXplusLag.add(BigDecimal.valueOf(values[i + lag]));
                sumOfXonXplusLag = sumOfXonXplusLag.add(BigDecimal.valueOf(values[i]).multiply(BigDecimal.valueOf(values[i + lag])));
            }

            BigDecimal avgX = sumX.divide(BigDecimal.valueOf(arrSize - lag), SCALE, ROUND_HALF_UP);
            BigDecimal avgXplusLag = sumXplusLag.divide(BigDecimal.valueOf(arrSize - lag), SCALE, ROUND_HALF_UP);

            BigDecimal DX = BigDecimal.ZERO;
            BigDecimal DXplusLag = BigDecimal.ZERO;

            for (int i = 0; i < arrSize - lag; i++) {
                DX = DX.add((avgX.subtract(BigDecimal.valueOf(values[i]))).pow(2));
                DXplusLag = DXplusLag.add((avgXplusLag.subtract(BigDecimal.valueOf(values[i + lag]))).pow(2));
            }

            DX = DX.divide(BigDecimal.valueOf(arrSize - lag), SCALE, ROUND_HALF_UP);
            DXplusLag = DXplusLag.divide(BigDecimal.valueOf(arrSize - lag), SCALE, ROUND_HALF_UP);

            BigDecimal sigmaX = sqrt(DX);
            BigDecimal sigmaXplusLag = sqrt(DXplusLag);
            BigDecimal avgOfXonXplusLag = sumOfXonXplusLag.divide(BigDecimal.valueOf(arrSize - lag), SCALE, ROUND_HALF_UP);
            BigDecimal cov = avgOfXonXplusLag.subtract(avgX.multiply(avgXplusLag));
            BigDecimal acf = cov.divide(sigmaX.multiply(sigmaXplusLag), SCALE, ROUND_HALF_UP);

            acfs[lag] = acf.abs().doubleValue();
        }

        int indOfMax = 0;
        double maxAcf = 0.7;
        for (int i = 1; i < arrSize - 2; i++) {
            if (maxAcf < acfs[i]) {
                maxAcf = acfs[i];
                indOfMax = i;
            }
        }

        LocalDateTime result = null;
        if (indOfMax > 0) {
            result = (LocalDateTime) statistic.keySet().toArray()[indOfMax];
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
