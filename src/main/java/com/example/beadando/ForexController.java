package com.example.beadando;

import com.oanda.v20.Context;
import com.oanda.v20.account.AccountSummary;
import com.oanda.v20.instrument.InstrumentCandlesRequest;
import com.oanda.v20.instrument.InstrumentCandlesResponse;
import com.oanda.v20.instrument.CandlestickGranularity;
import com.oanda.v20.order.MarketOrderRequest;
import com.oanda.v20.order.OrderCreateRequest;
import com.oanda.v20.order.OrderCreateResponse;
import com.oanda.v20.pricing.PricingGetRequest;
import com.oanda.v20.pricing.PricingGetResponse;
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
    @GetMapping("/forex-actpr")
    public String actualPriceForm(Model model) {
        model.addAttribute("messageActPrice", new MessageActPrice());
        return "forex_actpr";
    }

    @PostMapping("/forex-actpr")
    public String actualPriceSubmit(@ModelAttribute MessageActPrice messageActPrice, Model model) {
        try {
            Context ctx = new Context(Config.URL, Config.TOKEN);
            List<String> instruments = new ArrayList<>();
            instruments.add(messageActPrice.getInstrument());

            PricingGetRequest request = new PricingGetRequest(Config.ACCOUNTID, instruments);
            PricingGetResponse resp = ctx.pricing.get(request);

            if (resp.getPrices() != null && !resp.getPrices().isEmpty()) {
                model.addAttribute("prices", resp.getPrices());
            } else {
                model.addAttribute("error", "Nem érkezett árfolyam adat.");
            }
            model.addAttribute("selectedInstrument", messageActPrice.getInstrument());

        } catch (Throwable e) {
            e.printStackTrace();
            model.addAttribute("error", "Hiba az árfolyam lekérésekor: " + e.getMessage());
        }
        return "forex_actpr";
    }

    // --- 3. FELADAT: HISTORIKUS ÁRAK LEKÉRDEZÉSE ---
    @GetMapping("/forex-histpr")
    public String histPriceForm(Model model) {
        model.addAttribute("messageHistPrice", new MessageHistPrice());
        return "forex_histpr";
    }

    @PostMapping("/forex-histpr")
    public String histPriceSubmit(@ModelAttribute MessageHistPrice messageHistPrice, Model model) {
        try {
            Context ctx = new Context(Config.URL, Config.TOKEN);
            InstrumentCandlesRequest request = new InstrumentCandlesRequest(new InstrumentName(messageHistPrice.getInstrument()));
            request.setGranularity(CandlestickGranularity.valueOf(messageHistPrice.getGranularity()));
            request.setCount(10L);

            InstrumentCandlesResponse resp = ctx.instrument.candles(request);

            model.addAttribute("candles", resp.getCandles());
            model.addAttribute("selectedInstrument", messageHistPrice.getInstrument());
            model.addAttribute("selectedGranularity", messageHistPrice.getGranularity());

        } catch (Throwable e) {
            e.printStackTrace();
            model.addAttribute("error", "Hiba a historikus adatok lekérésekor: " + e.getMessage());
        }
        return "forex_histpr";
    }

    // --- 4. FELADAT: POZÍCIÓ NYITÁS ---
    @GetMapping("/forex-openpos")
    public String openPositionForm(Model model) {
        // Üres űrlap objektum létrehozása
        model.addAttribute("messageOpenPosition", new MessageOpenPosition());
        return "forex_openpos";
    }

    @PostMapping("/forex-openpos")
    public String openPositionSubmit(@ModelAttribute MessageOpenPosition messageOpenPosition, Model model) {
        try {
            Context ctx = new Context(Config.URL, Config.TOKEN);

            // 1. Fő kérés objektum (Melyik számlára?)
            OrderCreateRequest request = new OrderCreateRequest(Config.ACCOUNTID);

            // 2. Részletes megbízás objektum (Mit és mennyit?)
            MarketOrderRequest marketOrderRequest = new MarketOrderRequest();
            marketOrderRequest.setInstrument(new InstrumentName(messageOpenPosition.getInstrument()));
            marketOrderRequest.setUnits(messageOpenPosition.getUnits());

            // 3. Megbízás csatolása a kéréshez
            request.setOrder(marketOrderRequest);

            // 4. Kérés elküldése az OANDA-nak
            OrderCreateResponse response = ctx.order.create(request);

            // 5. Eredmény (Trade ID) kinyerése és megjelenítése
            // A response.getOrderFillTransaction() tartalmazza a sikeres tranzakció adatait
            if (response.getOrderFillTransaction() != null) {
                String tradeId = response.getOrderFillTransaction().getId().toString();
                String price = response.getOrderFillTransaction().getPrice().toString();
                model.addAttribute("tradeId", tradeId);
                model.addAttribute("openedPrice", price);
                model.addAttribute("openedInstrument", messageOpenPosition.getInstrument());
                model.addAttribute("openedUnits", messageOpenPosition.getUnits());
            } else {
                // Ha nincs Fill tranzakció, akkor valamiért nem teljesült azonnal (pl. Pending) vagy hiba volt
                model.addAttribute("error", "A megbízás elküldve, de nem teljesült azonnal (lehet, hogy várakozó).");
            }

        } catch (Throwable e) {
            e.printStackTrace();
            model.addAttribute("error", "Hiba a pozíció nyitásakor: " + e.getMessage());
        }
        return "forex_openpos";
    }
}