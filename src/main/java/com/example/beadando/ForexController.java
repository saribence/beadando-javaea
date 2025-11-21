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
        // Kapcsolódás az OANDA-hoz a Config adatokkal
        Context ctx = new Context(Config.URL, Config.TOKEN);

        try {
            // Számlainformációk lekérése
            AccountSummary summary = ctx.account.summary(Config.ACCOUNTID).getAccount();

            // Az adatok átadása a nézetnek (HTML)
            model.addAttribute("summary", summary);

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Hiba történt az OANDA elérésekor: " + e.getMessage());
        }

        return "forex_account";
    }
}