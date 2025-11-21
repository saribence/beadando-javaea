package com.example.beadando;

import com.oanda.v20.Context;
import com.oanda.v20.account.AccountSummary;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ForexController {

    @GetMapping("/forex-account")
    public String accountInfo(Model model) {
        try {
            // MOZGATVA: A try blokkon belülre került, hogy elkapjuk a hibát, ha nem sikerül
            Context ctx = new Context(Config.URL, Config.TOKEN);

            // Számlainformációk lekérése
            AccountSummary summary = ctx.account.summary(Config.ACCOUNTID).getAccount();

            // Az adatok átadása a nézetnek (HTML)
            model.addAttribute("summary", summary);

        } catch (Throwable e) { // Exception helyett Throwable, hogy a ClassNotFound hibákat is lássuk
            e.printStackTrace();
            model.addAttribute("error", "Hiba történt: " + e.getClass().getName() + ": " + e.getMessage());
        }

        return "forex_account";
    }
}