package com.nngu.fqw.statisticcalculator.controller;

import com.nngu.fqw.statisticcalculator.model.TimeSeriesStatistic;
import com.nngu.fqw.statisticcalculator.service.StatisticService;
import com.nngu.fqw.statisticcalculator.util.ResponseHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

@Controller
@RequestMapping("/statistic")
public class StatisticController {

    private static final Duration defaultFrameSize = Duration.ofMinutes(10);

    private StatisticService statisticService;
    private ResponseHelper responseHelper;

    @Autowired
    public StatisticController(StatisticService statisticService, ResponseHelper responseHelper) {
        this.statisticService = statisticService;
        this.responseHelper = responseHelper;
    }

    @RequestMapping(value = "/count", method = RequestMethod.GET)
    public String packetCountStatistic(@RequestParam(value = "protocol", required = false) String protocol, ModelMap modelMap) {
        Map<LocalDateTime, Double> chartData = statisticService.getPacketCountStatistic(protocol, defaultFrameSize);

        modelMap.addAttribute("protocol", protocol != null ? protocol : "all");
        modelMap.addAttribute("frameSize", defaultFrameSize.toString());
        modelMap.addAttribute("chartData", responseHelper.getChartData(chartData));
        return "chart-count";
    }

    @RequestMapping(value = "/average", method = RequestMethod.GET)
    public String packetAverageStatistic(@RequestParam(value = "protocol", required = false) String protocol, ModelMap modelMap) {
        Map<LocalDateTime, Double> chartData = statisticService.getPacketAverageStatistic(protocol, defaultFrameSize);

        modelMap.addAttribute("protocol", protocol != null ? protocol : "all");
        modelMap.addAttribute("frameSize", defaultFrameSize.toString());
        modelMap.addAttribute("chartData", responseHelper.getChartData(chartData));
        return "chart-avg";
    }

    @RequestMapping(value = "/windows/count", method = RequestMethod.GET)
    public String getWindowStatisticCount(@RequestParam(value = "protocol", required = false) String protocol, ModelMap modelMap) throws InterruptedException {
        TimeSeriesStatistic statistic = statisticService.getWindowStatisticCount(protocol);
        if (statistic != null) {
            modelMap.addAttribute("protocol", protocol != null ? protocol : "all");
            modelMap.addAttribute("frameSize", statistic.getDuration().toString());
            modelMap.addAttribute("bestWindowSize", statistic.getWindowSize());
            modelMap.addAttribute("chartData", responseHelper.getChartData(statistic.getTimeSeries()));
        }
        return "window-chart-count";
    }

    @RequestMapping(value = "/windows/average", method = RequestMethod.GET)
    public String getWindowStatisticAvg(@RequestParam(value = "protocol", required = false) String protocol, ModelMap modelMap) throws InterruptedException {
        TimeSeriesStatistic statistic = statisticService.getWindowStatisticAvg(protocol);
        if (statistic != null) {
            modelMap.addAttribute("protocol", protocol != null ? protocol : "all");
            modelMap.addAttribute("frameSize", statistic.getDuration().toString());
            modelMap.addAttribute("bestWindowSize", statistic.getWindowSize());
            modelMap.addAttribute("chartData", responseHelper.getChartData(statistic.getTimeSeries()));
        }
        return "window-chart-avg";
    }
}
