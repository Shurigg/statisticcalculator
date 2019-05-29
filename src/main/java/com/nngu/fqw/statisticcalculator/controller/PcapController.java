package com.nngu.fqw.statisticcalculator.controller;

import com.nngu.fqw.statisticcalculator.service.PcapService;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapNativeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.concurrent.TimeoutException;

@Controller
@RequestMapping("/pcap")
public class PcapController {

    @Autowired
    private PcapService pcapService;

    @RequestMapping(value = "/read", method = RequestMethod.GET)
    public String packetCountStatistic(@RequestParam(value = "filePath") String filePath, ModelMap modelMap) throws PcapNativeException, NotOpenException, TimeoutException {
        if (StringUtils.isEmpty(filePath)) {
            modelMap.addAttribute("exception", "Path to file cannot be null or empty");
            return "file";
        }

        pcapService.readFile(filePath);
        return "file";
    }
}
