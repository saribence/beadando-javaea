package com.example.beadando;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class MnbService {
    public List<ExchangeRate> getRates(String startDate, String endDate, String currency) {
        try {
            String soapRequest =
                    "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"http://www.mnb.hu/webservices/\">" +
                            "   <soapenv:Body>" +
                            "      <web:GetExchangeRates>" +
                            "         <web:startDate>" + startDate + "</web:startDate>" +
                            "         <web:endDate>" + endDate + "</web:endDate>" +
                            "         <web:currencyNames>" + currency + "</web:currencyNames>" +
                            "      </web:GetExchangeRates>" +
                            "   </soapenv:Body>" +
                            "</soapenv:Envelope>";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://www.mnb.hu/arfolyamok.asmx"))
                    .header("Content-Type", "text/xml; charset=utf-8")
                    .header("SOAPAction", "http://www.mnb.hu/webservices/GetExchangeRates")
                    .POST(HttpRequest.BodyPublishers.ofString(soapRequest))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return parseMnbResponse(response.body(), currency);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private List<ExchangeRate> parseMnbResponse(String soapResponse, String currency) throws Exception {
        List<ExchangeRate> rates = new ArrayList<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document soapDoc = builder.parse(new InputSource(new StringReader(soapResponse)));
        String innerXml = soapDoc.getElementsByTagName("GetExchangeRatesResult").item(0).getTextContent();
        Document innerDoc = builder.parse(new InputSource(new StringReader(innerXml)));
        NodeList days = innerDoc.getElementsByTagName("Day");

        for (int i = 0; i < days.getLength(); i++) {
            Element dayElement = (Element) days.item(i);
            String date = dayElement.getAttribute("date");
            NodeList rateNodes = dayElement.getElementsByTagName("Rate");
            for (int j = 0; j < rateNodes.getLength(); j++) {
                Element rateElement = (Element) rateNodes.item(j);
                if (rateElement.getAttribute("curr").equals(currency)) {
                    String valueStr = rateElement.getTextContent().replace(",", ".");
                    double value = Double.parseDouble(valueStr);
                    rates.add(new ExchangeRate(date, value));
                }
            }
        }
        Collections.reverse(rates);
        return rates;
    }
}