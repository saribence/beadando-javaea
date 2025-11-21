package com.example.beadando;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class MnbController {

    @Autowired
    private MnbService mnbService;

    @GetMapping("/mnb")
    public String showMnbPage(Model model) {
        model.addAttribute("rates", new ArrayList<ExchangeRate>());
        model.addAttribute("chartDates", new ArrayList<String>());
        model.addAttribute("chartValues", new ArrayList<Double>());
        return "mnb";
    }

    @PostMapping("/mnb")
    public String getRates(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam("currency") String currency,
            Model model) {
        List<ExchangeRate> rates = mnbService.getRates(startDate, endDate, currency);
        model.addAttribute("rates", rates);
        model.addAttribute("selectedCurrency", currency);
        List<String> dates = rates.stream().map(ExchangeRate::getDate).collect(Collectors.toList());
        List<Double> values = rates.stream().map(ExchangeRate::getValue).collect(Collectors.toList());
        model.addAttribute("chartDates", dates);
        model.addAttribute("chartValues", values);
        return "mnb";
    }
}