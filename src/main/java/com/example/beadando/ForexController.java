package com.example.beadando;

import com.oanda.v20.Context;
import com.oanda.v20.account.AccountSummary;
import com.oanda.v20.instrument.CandlestickGranularity;
import com.oanda.v20.instrument.InstrumentCandlesRequest;
import com.oanda.v20.instrument.InstrumentCandlesResponse;
import com.oanda.v20.pricing.PricingGetRequest;
import com.oanda.v20.pricing.PricingGetResponse;
import com.oanda.v20.pricing.ClientPrice;
import com.oanda.v20.primitives.InstrumentName;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ForexController {

    // --- 1. FELADAT: SZÁMLAINFORMÁCIÓK ---
    @GetMapping("/forex-account")
    public String accountInfo(Model model) {
        try {
            Context ctx = new Context(Config.URL, Config.TOKEN);
            AccountSummary summary = ctx.account.summary(Config.ACCOUNTID).getAccount();
            model.addAttribute("summary", summary);
        } catch (Throwable e) {
            e.printStackTrace();
            model.addAttribute("error", "Hiba történt: " + e.getMessage());
        }
        return "forex_account";
    }

    // --- 2. FELADAT: AKTUÁLIS ÁR LEKÉRDEZÉSE ---

    // Az űrlap megjelenítése (GET)
    @GetMapping("/forex-actpr")
    public String actualPriceForm(Model model) {
        // Üres objektum küldése az űrlapnak
        model.addAttribute("messageActPrice", new MessageActPrice());
        return "forex_actpr";
    }

    // Az űrlap feldolgozása (POST)
    @PostMapping("/forex-actpr")
    public String actualPriceSubmit(@ModelAttribute MessageActPrice messageActPrice, Model model) {
        try {
            Context ctx = new Context(Config.URL, Config.TOKEN);

            // Lista készítése a kiválasztott instrumentumból
            List<String> instruments = new ArrayList<>();
            instruments.add(messageActPrice.getInstrument());

            // Lekérés összeállítása és küldése
            PricingGetRequest request = new PricingGetRequest(Config.ACCOUNTID, instruments);
            PricingGetResponse resp = ctx.pricing.get(request);

            // Az eredmények átadása a nézetnek (Lista)
            if (resp.getPrices() != null && !resp.getPrices().isEmpty()) {
                model.addAttribute("prices", resp.getPrices());
            } else {
                model.addAttribute("error", "Nem érkezett árfolyam adat.");
            }

            // Visszaküldjük a kiválasztott instrumentumot is, hogy látsszon mit választott
            model.addAttribute("selectedInstrument", messageActPrice.getInstrument());

        } catch (Throwable e) {
            e.printStackTrace();
            model.addAttribute("error", "Hiba az árfolyam lekérésekor: " + e.getMessage());
        }
        return "forex_actpr";
    }
    @GetMapping("/forex-histpr")
    public String histPriceForm(Model model) {
        // Üres objektum küldése az űrlapnak
        model.addAttribute("messageHistPrice", new MessageHistPrice());
        return "forex_histpr";
    }

    @PostMapping("/forex-histpr")
    public String histPriceSubmit(@ModelAttribute MessageHistPrice messageHistPrice, Model model) {
        try {
            Context ctx = new Context(Config.URL, Config.TOKEN);

            // Kérés összeállítása: Instrumentum neve + Granularity + utolsó 10 adat
            InstrumentCandlesRequest request = new InstrumentCandlesRequest(new InstrumentName(messageHistPrice.getInstrument()));
            request.setGranularity(CandlestickGranularity.valueOf(messageHistPrice.getGranularity()));
            request.setCount(10L); // 10 adat kérése

            // Lekérés végrehajtása
            InstrumentCandlesResponse resp = ctx.instrument.candles(request);

            // Eredmények átadása a nézetnek
            model.addAttribute("candles", resp.getCandles());
            model.addAttribute("selectedInstrument", messageHistPrice.getInstrument());
            model.addAttribute("selectedGranularity", messageHistPrice.getGranularity());

        } catch (Throwable e) {
            e.printStackTrace();
            model.addAttribute("error", "Hiba a historikus adatok lekérésekor: " + e.getMessage());
        }
        return "forex_histpr";
    }
}