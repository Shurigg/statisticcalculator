package com.nngu.fqw.statisticcalculator.util;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ResponseHelper {

    public List<Map<String, Object>> getChartData(Map<?, Double> data) {
        List<Map<String, Object>> resultList = new ArrayList<>();

        data.forEach((key, value) -> {
            Map<String, Object> metric = new HashMap<>();
            metric.put("label", key);
            metric.put("value", value);
            resultList.add(metric);
        });

        return resultList;
    }
}
