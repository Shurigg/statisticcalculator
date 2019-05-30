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

import static com.nngu.fqw.statisticcalculator.service.StatisticService.FRAME_SIZE;

@Controller
@RequestMapping("/statistic")
public class StatisticController {

    private static final Duration DEFAULT_FRAME_SIZE = Duration.ofMinutes(1);

    private StatisticService statisticService;
    private ResponseHelper responseHelper;

    @Autowired
    public StatisticController(StatisticService statisticService, ResponseHelper responseHelper) {
        this.statisticService = statisticService;
        this.responseHelper = responseHelper;
    }

    @RequestMapping(value = "/count", method = RequestMethod.GET)
    public String packetCountStatistic(@RequestParam(value = "protocol", required = false) String protocol, ModelMap modelMap) {
        Map<LocalDateTime, Double> chartData = statisticService.getPacketCountStatistic(protocol, DEFAULT_FRAME_SIZE);

        modelMap.addAttribute("protocol", protocol != null ? protocol : "all");
        modelMap.addAttribute("frameSize", DEFAULT_FRAME_SIZE.toString());
        modelMap.addAttribute("chartData", responseHelper.getChartData(chartData));
        return "chart-count";
    }

    @RequestMapping(value = "/average", method = RequestMethod.GET)
    public String packetAverageStatistic(@RequestParam(value = "protocol", required = false) String protocol, ModelMap modelMap) {
        Map<LocalDateTime, Double> chartData = statisticService.getPacketAverageStatistic(protocol, DEFAULT_FRAME_SIZE);

        modelMap.addAttribute("protocol", protocol != null ? protocol : "all");
        modelMap.addAttribute("frameSize", DEFAULT_FRAME_SIZE.toString());
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

    @RequestMapping(value = "/interval/count", method = RequestMethod.GET)
    public String getMinIntervalCount(@RequestParam(value = "protocol", required = false) String protocol, ModelMap modelMap) throws InterruptedException {
        Map<LocalDateTime, Double> chartData = statisticService.getMinIntervalCount(protocol);
            modelMap.addAttribute("protocol", protocol != null ? protocol : "all");
            modelMap.addAttribute("frameSize", FRAME_SIZE.toString());
            modelMap.addAttribute("chartData", responseHelper.getChartData(chartData));
        return "chart-count";
    }

    @RequestMapping(value = "/interval/avg", method = RequestMethod.GET)
    public String getMinIntervalAvg(@RequestParam(value = "protocol", required = false) String protocol, ModelMap modelMap) throws InterruptedException {
        Map<LocalDateTime, Double> chartData = statisticService.getMinIntervalAvg(protocol);
            modelMap.addAttribute("protocol", protocol != null ? protocol : "all");
            modelMap.addAttribute("frameSize", FRAME_SIZE.toString());
            modelMap.addAttribute("chartData", responseHelper.getChartData(chartData));
        return "chart-avg";
    }
}
